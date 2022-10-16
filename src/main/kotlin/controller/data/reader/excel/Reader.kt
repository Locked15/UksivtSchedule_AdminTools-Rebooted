package controller.data.reader.excel

import model.document.DayColumnInfo
import model.document.parse.excel.BaseIteratorModel
import model.document.parse.excel.InnerIteratorModel
import model.document.parse.excel.OuterIteratorModel
import model.element.schedule.DaySchedule
import model.element.schedule.WeekSchedule
import model.element.schedule.base.Lesson
import model.element.schedule.base.day.Day
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import model.exception.GroupDoesNotExistException
import java.lang.Exception


/**
 * Reader-Class for an Excel documents with basic schedules.
 */
class Reader(pathToFile: String) {

    /* region Properties */

    /**
     * Contains a document with base schedule.
     *
     * Commonly a document contains a whole schedule for one of the college branches and program logic targets to this case.
     */
    private val document: XSSFWorkbook
    /* endregion */

    /* region Initializers */

    /**
     * Creates a new instance of a document with base schedule.
     *
     * May throw exceptions, if sent an incorrect path.
     */
    init {
        val docStream = FileInputStream(pathToFile)
        document = XSSFWorkbook(docStream)
    }
    /* endregion */

    /* region Functions */

    /**
     * Extracts a full [list][List] of groups, that presented inside [parsing document][document].
     *
     * The returned list may contain 'NULL' values, so if you want, you'll have to filter it.
     */
    fun getGroups(): List<String?> {
        val sheets = document.sheetIterator()
        val groups = mutableListOf<String?>()
        while (sheets.hasNext()) {
            groups.addAll(extractGroupsFromSheet(sheets.next()))
        }

        return groups.distinct()
    }

    /**
     * Extracts a full [list][Collection] of groups, that presented inside [sheet][Sheet].
     *
     * If the supplied sheet is 'NULL', returns an empty list.
     */
    private fun extractGroupsFromSheet(next: Sheet?): Collection<String?> {
        val groups = mutableListOf<String?>()
        if (next != null) {
            val header = next.getRow(0)
            val cells = header.cellIterator()
            while (cells.hasNext()) {
                groups.add(extractGroupFromCell(cells.next()))
            }
        }

        return groups
    }

    /**
     * Extracts group name from sent [cell].
     *
     * If sent value is 'NULL' or incorrect, returns 'NULL' value.
     */
    private fun extractGroupFromCell(cell: Cell?): String? {
        var groupName: String? = null
        if (cell != null && cell.cellType == CellType.STRING) {
            val value = cell.stringCellValue
            if (value != EMPTY_CELL_VALUE) groupName = value
        }

        return groupName
    }

    /**
     * Parse actual document and tries to extract [full-week schedule][WeekSchedule] for [group][groupName].
     *
     * @throws GroupDoesNotExistException If [sent group][groupName] doesn't exist in a current document.
     * @throws NullPointerException If runtime met 'NULL' pointing instruction.
     */
    fun getWeekSchedule(groupName: String): WeekSchedule {
        val targetSheet = searchTargetSheet(document, groupName)
        if (targetSheet != null) {
            /** Base data, placed outside cycles. */
            val baseData = BaseIteratorModel(mutableListOf(), searchDaysCoordinates(targetSheet),
                                             searchTargetColumns(targetSheet, groupName))
            for (column in 0..2) {
                /** Outer iteration data, placed inside the first cycle. */
                val iterationData = OuterIteratorModel(0, 0, 0, Lesson(), null, mutableListOf(),
                                                       targetSheet.rowIterator())
                for (row in iterationData.rows) {
                    /** Inner iteration data, placed inside the second cycle. */
                    val localData = InnerIteratorModel(lessonNameIsAdded = false, lessonPlaceIsAdded = false,
                                                       dayEndingRow = DayColumnInfo.getInfoByDay(
                                                               Day.getValueByIndex(iterationData.dayIterator),
                                                               baseData.dayColumnsIndices)?.coordinates?.row,
                                                       cells = row.cellIterator())
                    // Skip region with header.
                    if (row.rowNum < baseData.dayColumnsIndices[0].coordinates.row) {
                        continue
                    }
                    // Check condition to change current day.
                    if (row.rowNum == localData.dayEndingRow) {
                        if (iterationData.dayIterator > 0) {
                            // Inserts a last lesson:
                            iterationData.lessons.add(iterationData.lesson)

                            /* Then we add ready-to-generate 'DaySchedule' to list and clear current lessons list.
                               Clearing process made by creation new object, to prevent pass-by-reference troubles. */
                            baseData.schedules.add(DaySchedule(iterationData.currentDay!!, iterationData.lessons))
                            iterationData.lessons = mutableListOf()
                        }

                        /* Then we generate data for another one lesson and write it to properties.
                           Especially, I must say about 'currentDay' data-generation: this expression used,
                           cause whole schedule divided and chopped into two parts,
                           that are placed horizontally inside a document. */
                        iterationData.lesson = Lesson()
                        iterationData.lessonNumber = 0
                        iterationData.lessonIterator = 0
                        iterationData.lesson.number = iterationData.lessonNumber
                        iterationData.currentDay = Day.getValueByIndex(iterationData.dayIterator + column * 3)

                        /* At this point, we don't complete condition to change lesson number, and we can meet trouble.
                           If lessons of the next day begin in the morning, so variable content doesn't reset,
                           so we must reset it manually.
                           Also, at this point we'll increase day iterator, because day WILL change. */
                        localData.lessonNameIsAdded = false
                        iterationData.dayIterator++
                    }
                    // Check condition to change lesson number.
                    if (iterationData.lessonIterator != 0 && iterationData.lessonIterator % 4 == 0) {
                        iterationData.lessons.add(iterationData.lesson)
                        iterationData.lessonNumber++

                        iterationData.lesson = Lesson()
                        iterationData.lesson.number = iterationData.lessonNumber
                        localData.lessonNameIsAdded = false
                        localData.lessonPlaceIsAdded = false
                    }

                    // And now, we will iterate cells inside current row.
                    for (cell in localData.cells) {
                        if (baseData.targetBorders[column].leftBorderIndex!! >= cell.columnIndex) {
                            // Skip first cells (until we meet target cells):
                            continue
                        }
                        else if (baseData.targetBorders[column].rightBorderIndex!! <= cell.columnIndex) {
                            // Break cycle when iterator pass out of schedule cells:
                            break
                        }

                        /* Important Note:
                           In an original schedule document *lesson place* cells can be assigned to different rows.
                           Sometimes, on the row with lesson teacher, sometimes on the row with lesson name, etc. */
                        try {
                            val cellValue = cell.stringCellValue
                            if (cellValue != EMPTY_CELL_VALUE && cellValue != UNITED_CELL_VALUE) {
                                if (cell.columnIndex + 1 == baseData.targetBorders[column].rightBorderIndex) {
                                    localData.lessonPlaceIsAdded = true

                                    iterationData.lesson.place = cellValue
                                }
                                else if (localData.lessonNameIsAdded) {
                                    iterationData.lesson.teacher = cellValue
                                }
                                else {
                                    localData.lessonNameIsAdded = true

                                    iterationData.lesson.name = cellValue
                                }
                            }
                        }
                        /* Cell with lesson place often declared as 'Numeric', BUT:
                           Sometimes, when place described by string, it has 'String' value.
                           And, because trying to get cell value with a wrong type throws exception,
                           we'll have to catch it.
                           Cause situation is common, an exception object will be ignored (marked as '_'). */
                        catch (_: Exception) {
                            /* I'm not sure about it, but it's better to leave this condition check
                               ('Anomalies inside documents... anomalies everywhere').
                               What most hilariously — they have a document template, but they violates it (OFTEN). */
                            if (!localData.lessonPlaceIsAdded) {
                                val temp = cell.numericCellValue.toInt()
                                iterationData.lesson.place = temp.toString()

                                localData.lessonPlaceIsAdded = true
                            }
                        }
                    }

                    iterationData.lessonIterator++
                }

                /* At this point, we get to the end of the day.
                   We'll add last lesson (that out-of-iteration), and then generate 'DaySchedule' object. */
                iterationData.lessons.add(iterationData.lesson)
                baseData.schedules.add(DaySchedule(iterationData.currentDay!!, iterationData.lessons))
            }

            return WeekSchedule(groupName, baseData.schedules)
        }
        else {
            throw GroupDoesNotExistException("\n\nIn parsing process failure occurred:\n" +
                                                     "Group ($groupName) not found in document."
            )
        }
    }
    /* endregion */

    /* region Companion */

    companion object {

        /**
         * Value with 'empty' cell content.
         *
         * Truly, empty cells contain 29 spaces.
         */
        internal const val EMPTY_CELL_VALUE = "                             "

        /**
         * Value with united cells content.
         *
         * In difference from [empty cell][EMPTY_CELL_VALUE] it contains just an empty string.
         */
        const val UNITED_CELL_VALUE = ""
    }
    /* endregion */
}
