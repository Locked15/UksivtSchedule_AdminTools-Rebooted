package controller.data.reader.excel

import controller.data.reader.excel.searchDaysCoordinates as days
import controller.data.reader.excel.searchTargetColumns as targets
import model.data.parse.schedule.DayColumnInfo
import model.data.parse.schedule.wrapper.BaseIteratorModel
import model.data.parse.schedule.wrapper.InnerIteratorModel
import model.data.parse.schedule.wrapper.OuterIteratorModel
import model.data.schedule.DaySchedule
import model.data.schedule.WeekSchedule
import model.data.schedule.base.Lesson
import model.data.schedule.base.day.Day
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import model.exception.GroupDoesNotExistException
import org.apache.poi.ss.usermodel.Row
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
            val baseData = BaseIteratorModel(mutableListOf(), days(targetSheet), targets(targetSheet, groupName))
            for (column in 0 until 2) {
                parseCurrentPartOfTheDocument(column, targetSheet, baseData)
            }

            // Insert sunday schedule and then return ready value.
            baseData.schedules.add(DaySchedule(Day.SUNDAY, mutableListOf()))
            return WeekSchedule(groupName, baseData.schedules)
        }
        else {
            throw GroupDoesNotExistException("\n\nIn parsing process failure occurred:\n" +
                                                     "Group ($groupName) not found in document."
            )
        }
    }

    /**
     * Parses current part of the [working sheet][targetSheet].
     * Requires [data][base] and current [column] (number of parsing part).
     *
     * Currently, documents sheets divide on two parts:
     * * First — With schedules for monday through wednesday;
     * * Second — With schedules for thursday through saturday.
     */
    private fun parseCurrentPartOfTheDocument(column: Int, targetSheet: Sheet, base: BaseIteratorModel) {
        /** Outer iteration data, placed inside the first cycle. */
        val iterationData = OuterIteratorModel(lessonNameIsAdded = false, lessonPlaceIsAdded = false,
                                               0, 0, 0, Lesson(), null, mutableListOf(),
                                               targetSheet.rowIterator())
        for (row in iterationData.rows) {
            parseCurrentRow(column, row, base, iterationData)
        }

        /* At this point, we get to the end of the day.
           We'll add last lesson (that out-of-iteration), and then generate 'DaySchedule' object. */
        iterationData.lessons.add(iterationData.lesson)
        base.schedules.add(DaySchedule(iterationData.currentDay!!, iterationData.lessons))
    }

    /**
     * Parses current [row].
     *
     * Requires [previous][base] [data-objects][iteration] and current [column]
     * (number of the current parsed part of the sheet).
     */
    private fun parseCurrentRow(column: Int, row: Row, base: BaseIteratorModel, iteration: OuterIteratorModel) {
        /** Inner iteration data, placed inside the second cycle. */
        val localData = InnerIteratorModel(DayColumnInfo.getInfoByDay(Day.getValueByIndex(iteration.dayIterator),
                                                                      base.dayColumnsIndices)?.coordinates?.row,
                                           cells = row.cellIterator())
        if (checkMainConditionsAndUpdateObjects(column, row, base, iteration, localData)) return

        // And now, we will iterate cells inside current row.
        for (cell in localData.cells) {
            when (checkAdditionalConditions(column, cell, base)) {
                1 -> continue
                -1 -> break
            }
            parseCellValue(column, cell, base, iteration)
        }
        iteration.lessonIterator++
    }

    /**
     * Checks main conditions (to the header region, that is skips AND to object update conditions).
     * If second ones are completed, updates target objects.
     *
     * Requires [all][base] [previous][iter] [wrapper-models][local], [current column][column] and [row].
     */
    private fun checkMainConditionsAndUpdateObjects(column: Int, row: Row, base: BaseIteratorModel,
                                                    iter: OuterIteratorModel, local: InnerIteratorModel): Boolean {
        // Skip region with header.
        return if (row.rowNum < base.dayColumnsIndices[0].coordinates.row) true
        else {
            // Check condition to change current day.
            if (row.rowNum == local.dayEndingRow) {
                if (iter.dayIterator > 0) {
                    // Inserts a last lesson:
                    iter.lessons.add(iter.lesson)

                    /* Then we add ready-to-generate 'DaySchedule' to list and clear current lessons list.
                   Clearing process made by creation new object, to prevent pass-by-reference troubles. */
                    base.schedules.add(DaySchedule(iter.currentDay!!, iter.lessons))
                    iter.lessons = mutableListOf()
                }

                /* Then we generate data for another one lesson and write it to properties.
               Especially, I must say about 'currentDay' data-generation: this expression used,
                           cause whole schedule divided and chopped into two parts,
               that are placed horizontally inside a document. */
                iter.lesson = Lesson()
                iter.lessonNumber = 0
                iter.lessonIterator = 0
                iter.lesson.number = iter.lessonNumber
                iter.currentDay = Day.getValueByIndex(iter.dayIterator + column * 3)

                /* At this point, we don't complete condition to change lesson number, and we can meet trouble.
               If lessons of the next day begin in the morning, so variable content doesn't reset,
                  so we must reset it manually.
               Also, at this point we'll increase day iterator, because day WILL change. */
                iter.lessonNameIsAdded = false
                iter.dayIterator++
            }
            // Check condition to change lesson number.
            if (iter.lessonIterator != 0 && iter.lessonIterator % 4 == 0) {
                iter.lessons.add(iter.lesson)
                iter.lessonNumber++

                iter.lesson = Lesson()
                iter.lesson.number = iter.lessonNumber
                iter.lessonNameIsAdded = false
                iter.lessonPlaceIsAdded = false
            }

            false
        }
    }

    /**
     * Checks additional conditions (that define current parser position inside a document).
     *
     * Requires current [column] (parsing part of the sheet), [current cell][cell] and [base data][baseData].
     *
     * Returns code of the next action:
     * * -1 — Break the cycle, because you found the right border of the target column;
     * * 0 — Algorithm places inside the target column, parse it;
     * * 1 — Continue the cycle, you don't meet the left border of the target column.
     */
    private fun checkAdditionalConditions(column: Int, cell: Cell, baseData: BaseIteratorModel): Int {
        // Skip first cells (until we meet target cells):
        return if (baseData.targetBorders[column].leftBorderIndex!! >= cell.columnIndex) {
            1
        }
        // Break cycle when iterator pass out of schedule cells:
        else if (baseData.targetBorders[column].rightBorderIndex!! <= cell.columnIndex) {
            -1
        }
        // In other cases, continue the algorithm:
        else {
            0
        }
    }

    /**
     * Parses [current cell][cell] value.
     * Requires [column] (currently parsing part of the sheet) and [wrapper][baseData] [objects][iteration].
     *
     * If cell values mixed up, you need to update this function,
     * cause it define how cell values will be written to [object][Lesson].
     */
    private fun parseCellValue(column: Int, cell: Cell, baseData: BaseIteratorModel, iteration: OuterIteratorModel) {
        /* Important Note:
           In an original schedule document *lesson place* cells can be assigned to different rows.
           Sometimes, on the row with lesson teacher, sometimes on the row with lesson name, etc. */
        try {
            val cellValue = cell.stringCellValue
            if (cellValue != EMPTY_CELL_VALUE && cellValue != UNITED_CELL_VALUE) {
                if (cell.columnIndex + 1 == baseData.targetBorders[column].rightBorderIndex) {
                    iteration.lessonPlaceIsAdded = true

                    iteration.lesson.place = cellValue
                }
                else if (iteration.lessonNameIsAdded) {
                    iteration.lesson.teacher = cellValue
                }
                else {
                    iteration.lessonNameIsAdded = true

                    iteration.lesson.name = cellValue
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
            if (!iteration.lessonPlaceIsAdded) {
                val temp = cell.numericCellValue.toInt()

                iteration.lesson.place = temp.toString()
                iteration.lessonPlaceIsAdded = true
            }
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
