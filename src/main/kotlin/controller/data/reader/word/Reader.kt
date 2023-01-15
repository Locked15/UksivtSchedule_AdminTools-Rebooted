package controller.data.reader.word

import model.data.parse.changes.wrapper.BaseIteratorModel
import model.data.parse.changes.wrapper.InnerIteratorModel
import model.data.parse.changes.wrapper.OuterIteratorModel
import model.data.schedule.TargetChangesOfDay
import model.data.schedule.DaySchedule
import model.data.schedule.base.Lesson
import model.data.schedule.base.day.Day
import model.exception.WrongDayInDocumentException
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.FileInputStream
import model.data.parse.changes.CellDefineResult
import java.lang.Exception
import java.util.Calendar
import java.util.GregorianCalendar


/**
 * Reader-Class for a Word documents with schedule targetChangesOfDay.
 */
class Reader(pathToFile: String) {

    /* region Properties */

    /**
     * Contains a document with targetChangesOfDay for specified day.
     */
    private val document: XWPFDocument
    /* endregion */

    /* region Initializers */

    /**
     * Initializes current *document* property.
     *
     * May throw exceptions, if sent an incorrect path.
     */
    init {
        val docStream = FileInputStream(pathToFile)
        document = XWPFDocument(docStream)
    }
    /* endregion */

    /* region Functions */

    fun getAvailableGroups(): List<String> {
        val foundGroups = mutableListOf<String>()
        // First row will contain information about cells, so we'll have to skip it.
        for (row in searchTargetTable(document.tables).rows.drop(1)) {
            for ((cellNumber, cell) in row.tableCells.withIndex()) {
                if (checkCellToBeDeclarationSpecificCell(cellNumber) && !cell.text.isNullOrBlank())
                    foundGroups.add(cell.text)
            }
        }

        // I not sure about it, but I think it's better if I will distinct final list.
        return foundGroups.distinct()
    }

    /**
     * Class main function (and one of two, with *public* visibility modifier).
     * Parses [current document][document] and return [object][TargetChangesOfDay],
     * that contains targetChangesOfDay to the [target group][groupName].
     * May return 'NULL' if a document doesn't contain targetChangesOfDay for a target group.
     *
     * If sent [day] isn't 'NULL', function will check target day and day, declared in a document to equality.
     * If they ain't equal, it **throws [exception][WrongDayInDocumentException]**.
     *
     * Info: earlier it contains merging logic (it returns schedule with merged targetChangesOfDay), but I refactored it.
     * Now it returns only targetChangesOfDay.
     */
    fun getChanges(groupName: String, day: Day?): TargetChangesOfDay? {
        val headerInfo = getHeaderAdditionalInfo(day)
        if (checkGroupsToContain(headerInfo.first, groupName)) {
            // We check header, because it can contain info about practise.
            return TargetChangesOfDay.getOnPractiseChanges(headerInfo.second, groupName)
        }
        // Otherwise, we'll parse the main content of the document.
        else {
            /** Base model, that contains information about parsing, including [TargetChangesOfDay] object. */
            val baseData = BaseIteratorModel(cycleStopper = false, listenToChanges = false,
                                             targetChangesOfDay = TargetChangesOfDay(groupName, headerInfo.second))
            for (row in searchTargetTable(document.tables).rows) {
                /** Model with iteration data, including generating lesson.
                 * Begins with -1 cell number, because we increment it right on first iteration. */
                val iterationData = OuterIteratorModel(-1, "", Lesson())
                for (cell in row.tableCells) {
                    /** Local data that uses inside last cycle. */
                    val localData = InnerIteratorModel(cell.text, cell.text.lowercase())
                    if (groupName.lowercase() == localData.lowerText) {
                        // !!! Use this to stop at target document location. !!! \\
                        localData.toString()
                    }

                    iterationData.cellNumber++
                    when (defineCellContent(baseData, iterationData, localData, groupName)) {
                        CellDefineResult.CONTINUE -> continue
                        CellDefineResult.READ -> readCellValueAndUpdateObject(localData.text, iterationData)
                        CellDefineResult.BREAK -> break
                    }
                }

                checkStateAndUpdateChangedLessons(baseData, iterationData)
                if (baseData.cycleStopper) {
                    // If we found all needed information, we can break a parse process, to increase performance.
                    break
                }
            }

            return if (baseData.targetChangesOfDay.changedLessons.isEmpty()) null
            else baseData.targetChangesOfDay
        }
    }

    /* region Work with Document Header */

    /**
     * Extracts a full list of groups, that targets to practise on a current targetChangesOfDay document.
     * If sent [day] isn't 'NULL' it also checks declared day in document and target day.
     *
     * Also, parses header to get information about [targetChangesOfDay date and month][Calendar].
     *
     * [Returned value][Pair] is full list of [groups][String],
     * that schedule must be generated via [special function][DaySchedule.getOnPractiseSchedule]
     * AND [date object][Calendar] with target date and day.
     */
    private fun getHeaderAdditionalInfo(day: Day?): Pair<List<String>, Calendar?> {
        val builder = StringBuilder()
        var foundDate: Calendar? = null
        for (i in document.paragraphs.indices) {
            // First paragraphs contain administration info, so ignore them:
            if (i < DATE_INFO_PARAGRAPH_ID) {
                continue
            }
            // Fifth paragraph contains day name and week number, so we can check data:
            else if (i == DATE_INFO_PARAGRAPH_ID) {
                if (day != null) completeDayCheck(document.paragraphs[i].text, day.russianName)
                foundDate = completeDateParse(document.paragraphs[i].text)
            }
            // Left paragraphs contains information, that we need:
            else {
                builder.append(document.paragraphs[i].text)
            }
        }

        // Last one element contains "On Practise" string ending, not group, so remove it.
        val practiseText = builder.toString().split(ON_PRACTISE_ENDING).dropLast(1)
        return Pair(practiseText, foundDate)
    }

    /**
     * Completes current document day check.
     * If check fails, it throws an [exception][WrongDayInDocumentException].
     */
    private fun completeDayCheck(text: String, targetDayName: String) {
        if (!text.contains(targetDayName, true)) {
            throw WrongDayInDocumentException("Sent day and day, declared in document don't equal.")
        }
    }

    /**
     * Completes date parse by document header value.
     * Initializes
     */
    private fun completeDateParse(text: String): Calendar {
        /**
         * Contains elements with information about target date.
         * All values represented in upper case.
         *
         * IDs:
         * * 0 — Contains in-month day number (1, 19, 24, etc);
         * * 1 — Contains month name on russian language with specific ending ('ДЕКАБРЯ', 'СЕНТЯБРЯ', etc);
         * * 2 — Contains in-week day name ('ПОНЕДЕЛЬНИК', 'СУББОТА', etc).
         *
         * Because we split original string with multiple regexes, we must remove empty elements (that will appear).
         */
        val elements = text.split(" ", "–").drop(1).filterNot { el -> el == EMPTY_WORD_TABLE_CELL_VALUE }
        val monthId = getMonthIndexByName(elements[1])

        var parsedDate = Calendar.getInstance()
        parsedDate = GregorianCalendar(parsedDate.get(Calendar.YEAR), monthId, elements[0].toInt())
        return parsedDate
    }
    /* endregion */

    /**
     * Defines current cell content and updates [iterator][baseData] [model][iterationData] [objects][localData].
     * Sent [target] value contains group name, that must be found.
     *
     * [Returns object][CellDefineResult] with a defined result.
     */
    private fun defineCellContent(baseData: BaseIteratorModel, iterationData: OuterIteratorModel,
                                  localData: InnerIteratorModel, target: String): CellDefineResult {
        // If we have met with target group name, we'll start targetChangesOfDay reading:
        if (checkValueToEquality(localData.lowerText, target)) {
            baseData.listenToChanges = true
            baseData.targetChangesOfDay.isAbsolute = true
        }
        // If we met another group name AND we're reading targetChangesOfDay, so we'll have to break the cycle.
        else if (checkToParsingStopper(baseData, iterationData, localData, target)) {
            baseData.cycleStopper = true
            return CellDefineResult.BREAK
        }
        // In all other cases (and if we're reading targetChangesOfDay), we'll read cell value:
        else if (baseData.listenToChanges) {
            return CellDefineResult.READ
        }

        return CellDefineResult.CONTINUE
    }

    /**
     * Reads sent [cell value][content] and updates current [generating object][storage],
     * depending by current [cell ID][OuterIteratorModel.cellNumber].
     */
    private fun readCellValueAndUpdateObject(content: String, storage: OuterIteratorModel) {
        when (storage.cellNumber) {
            // The second cell contains lesson number:
            1 -> storage.rawLessonNumbers = content
            // The fifth cell contains lesson name:
            4 -> storage.currentLesson.name = content
            // The sixth cell contains teacher name:
            5 -> storage.currentLesson.teacher = content
            // The seventh cell contains lesson place:
            6 -> storage.currentLesson.place = content
        }
    }

    /**
     * Checks a [target group][target] to contain an inside collection of [groups][groups].
     * This function fixes target and practise groups, so it's not affected by data-anomalies.
     *
     * May be used to check "On Practise" groups.
     */
    private fun checkGroupsToContain(groups: List<String>, target: String): Boolean {
        for (group in groups) {
            if (checkValueToEquality(group, target)) {
                return true
            }
        }

        return false
    }

    private fun checkValueToEquality(text: String, target: String): Boolean {
        val fixedText = text.trim().replace("-", "").replace("_", "").replace(".", "")
        val fixedTarget = target.trim().replace("-", "").replace("_", "").replace(".", "")
        if (fixedText.equals(fixedTarget, true)) {
            return true
        }

        return false
    }

    /**
     * Checks current state of [sent][base] [objects][outer] and updates schedule with unwrapped lessons.
     *
     * It's an encapsulated function, moved from [base][getChanges] function.
     * It calls [expandWrappedLesson] with stored data and
     * updates [generating lessons][BaseIteratorModel.targetChangesOfDay] with unwrapped values.
     */
    private fun checkStateAndUpdateChangedLessons(base: BaseIteratorModel, outer: OuterIteratorModel) {
        // In this case we found "Debt Liquidation" and have to update all TargetChangesOfDay Object.
        if (base.listenToChanges && outer.rawLessonNumbers.contains("ликвидация", true)) {
            base.targetChangesOfDay = TargetChangesOfDay.getDebtLiquidationChanges(base.targetChangesOfDay.changesDate, base.targetChangesOfDay.targetGroup)
            base.cycleStopper = true
        }
        else if (base.listenToChanges && outer.rawLessonNumbers != "") {
            base.targetChangesOfDay.changedLessons.addAll(expandWrappedLesson(outer.rawLessonNumbers, outer.currentLesson))
        }
    }

    /**
     * Expands a wrapped-style written lesson inside a document with targetChangesOfDay.
     * It requires [string with short-format lesson numbers][wrappedNumber] and [lesson template][lesson].
     *
     * Numbers expands and generates few lessons, based on [template][lesson].
     *
     * [Returned list][List] easily can be added to schedule objects.
     */
    private fun expandWrappedLesson(wrappedNumber: String, lesson: Lesson): List<Lesson> {
        val splatted = wrappedNumber.split(",", ".").map { e -> e.trim(' ') }
        val toReturn = mutableListOf<Lesson>()
        for (number in splatted) {
            try {
                toReturn.add(Lesson(number.toInt(), lesson.name,
                                    lesson.teacher, lesson.place))
            }
            catch (ex: Exception) {
                // Something bad happened. IDK.
            }
        }

        return toReturn
    }

    /**
     * Class secondary main function (and one of three, with *public* visibility modifier).
     * Returns [base schedule][schedule] with [merged][DaySchedule.mergeWithChanges] [targetChangesOfDay][TargetChangesOfDay].
     *
     * Represents original (".getDayScheduleWithChanges()") function from old *AdminTools*.
     * Because it won't be used often, I moved it down in functions order.
     */
    fun getChangedSchedule(schedule: DaySchedule, groupName: String, day: Day?): DaySchedule {
        val changes = getChanges(groupName, day)
        if (changes != null) {
            println("Automatic merge tool found empty targetChangesOfDay." +
                            "\nBase schedule (new ref) will be return.")
        }

        return schedule.mergeWithChanges(changes)
    }
    /* endregion */

    /* region Companion */

    companion object {

        /**
         * Contains an empty word table cell value.
         *
         * In difference with Excel empty cells, word cells are really empty.
         */
        internal const val EMPTY_WORD_TABLE_CELL_VALUE = ""

        /**
         * Contains ending of "On Practise" string.
         *
         * Uses to split whole string into [List].
         */
        internal const val ON_PRACTISE_ENDING = "— на практике"

        /**
         * Contains ID of the document paragraph, that contains information about target day and month.
         *
         * I.E.:
         * "НА 19 ДЕКАБРЯ – ПОНЕДЕЛЬНИК".
         */
        private const val DATE_INFO_PARAGRAPH_ID = 4
    }
    /* endregion */
}
