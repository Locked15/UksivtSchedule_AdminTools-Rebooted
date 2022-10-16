package controller.data.reader.word

import model.document.parse.word.iterator.BaseIteratorModel
import model.document.parse.word.iterator.InnerIteratorModel
import model.document.parse.word.iterator.OuterIteratorModel
import model.element.schedule.Changes
import model.element.schedule.DaySchedule
import model.element.schedule.base.Lesson
import model.element.schedule.base.day.Day
import model.exception.WrongDayInDocumentException
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.FileInputStream
import model.document.parse.word.CellDefineResult


/**
 * Reader-Class for a Word documents with schedule changes.
 */
class Reader(pathToFile: String) {

    /* region Properties */

    /**
     * Contains a document with changes for specified day.
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

    /**
     * Class main function (and one of two, with *public* visibility modifier).
     * Parses [current document][document] and return [object][Changes],
     * that contains changes to the [target group][groupName].
     * May return 'NULL' if a document doesn't contain changes for a target group.
     *
     * If sent [day] isn't 'NULL', function will check target day and day, declared in a document to equality.
     * If they ain't equal, it **throws [exception][WrongDayInDocumentException]**.
     *
     * Info: earlier it contains merging logic (it returns schedule with merged changes), but I refactored it.
     * Now it returns only changes.
     */
    fun getChanges(groupName: String, day: Day?): Changes? {
        // Checks header, because it can contain info about practise.
        if (checkGroupsToContain(getPractiseGroups(day), groupName)) {
            return DaySchedule.getOnPractiseChanges()
        }
        // Otherwise, we'll parse the main content of the document.
        else {
            /** Base model, that contains information about parsing, including [Changes] object. */
            val baseData = BaseIteratorModel(cycleStopper = false, listenToChanges = false, changes = Changes())
            for (row in searchTargetTable(document.tables).rows) {
                /** Model with iteration data, especially with a generating lesson. */
                val iterationData = OuterIteratorModel(0, "", Lesson())
                for (cell in row.tableCells) {
                    /** Local data that uses inside last cycle. */
                    val localData = InnerIteratorModel(cell.text, cell.text.lowercase())
                    when (defineCellContent(baseData, iterationData, localData, groupName)) {
                        CellDefineResult.CONTINUE -> continue
                        CellDefineResult.READ -> readCellValueAndUpdateObject(localData.text, iterationData)
                        CellDefineResult.BREAK -> break
                    }

                    iterationData.cellNumber++
                }

                checkStateAndUpdateChangedLessons(baseData, iterationData)
                if (baseData.cycleStopper) {
                    // If we found all needed information, we can break a parse process, to increase performance.
                    break
                }
            }

            return if (baseData.changes.changedLessons.isEmpty()) null
            else baseData.changes
        }
    }

    /**
     * Defines current cell content and updates [iterator][baseData] [model][iterationData] [objects][localData].
     * Sent [target] value contains group name, that must be found.
     *
     * [Returns object][CellDefineResult] with a defined result.
     */
    private fun defineCellContent(baseData: BaseIteratorModel, iterationData: OuterIteratorModel,
                                  localData: InnerIteratorModel, target: String): CellDefineResult {
        // Before other checks, we must check cell to empty value:
        if (localData.text == EMPTY_WORD_TABLE_CELL_VALUE) {
            iterationData.cellNumber++
        }
        // If we have met with target group name, we'll start changes reading:
        else if (localData.lowerText == target.lowercase()) {
            baseData.listenToChanges = true
            baseData.changes.absolute = true
        }
        // If we met another group name AND we're reading changes, so we'll have to break the cycle.
        else if (checkToParsingStopper(baseData, iterationData, localData, target)) {
            baseData.cycleStopper = true
            return CellDefineResult.BREAK
        }
        // In all other cases (and if we're reading changes), we'll read cell value:
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
     * Extracts a full list of groups, that targets to practise on a current changes document.
     * If sent [day] isn't 'NULL' it also checks declared day in document and target day.
     *
     * [Returned value][List] is full list of [groups][String],
     * that schedule must be generated via [special function][DaySchedule.getOnPractiseSchedule].
     */
    private fun getPractiseGroups(day: Day?): List<String> {
        val builder = StringBuilder()
        for (i in document.paragraphs.indices) {
            // First paragraphs contain administration info, so ignore them:
            if (i < 5) {
                continue
            }
            // Fifth paragraph contains day name and week number, so we can check data:
            else if (i == 5 && day != null) {
                if (document.paragraphs[i].text.contains(day.russianName, true)) {
                    throw WrongDayInDocumentException("Sent day and day, declared in document don't equal.")
                }
            }
            // Left paragraphs contains information, that we need:
            else {
                builder.append(document.paragraphs[i].text)
            }
        }

        // Last one element contains "On Practise" string ending, not group, so remove it.
        return builder.toString().split(ON_PRACTISE_ENDING, ",").dropLast(1)
    }

    /**
     * Checks a [target group][target] to contain an inside collection of [groups][groups].
     * This function fixes target and practise groups, so it's not affected by data-anomalies.
     *
     * May be used to check "On Practise" groups.
     */
    private fun checkGroupsToContain(groups: List<String>, target: String): Boolean {
        val fixedTarget = target.trim().replace("-", "").replace("_", "").replace(".", "")
        for (group in groups) {
            val fixedGroup = group.trim().replace("-", "").replace("_", "").replace(".", "")
            if (fixedGroup == fixedTarget) {
                return true
            }
        }

        return false
    }

    /**
     * Checks current state of [sent][base] [objects][outer] and updates schedule with unwrapped lessons.
     *
     * It's an encapsulated function, moved from [base][getChanges] function.
     * It calls [expandWrappedLesson] with stored data and
     * updates [generating lessons][BaseIteratorModel.changes] with unwrapped values.
     */
    private fun checkStateAndUpdateChangedLessons(base: BaseIteratorModel, outer: OuterIteratorModel) {
        if (base.listenToChanges && outer.rawLessonNumbers != "") {
            base.changes.changedLessons.addAll(expandWrappedLesson(outer.rawLessonNumbers, outer.currentLesson))
        }
    }

    /**
     * Expands a wrapped-style written lesson inside a document with changes.
     * It requires [string with short-format lesson numbers][wrappedNumber] and [lesson template][lesson].
     *
     * Numbers expands and generates few lessons, based on [template][lesson].
     *
     * [Returned list][List] easily can be added to schedule objects.
     */
    private fun expandWrappedLesson(wrappedNumber: String, lesson: Lesson): List<Lesson> {
        val splatted = wrappedNumber.split(",").map { e -> e.trim(' ') }
        val toReturn = mutableListOf<Lesson>()
        for (number in splatted) {
            toReturn.add(Lesson(number.toInt(), lesson.name,
                                lesson.teacher, lesson.place))
        }

        return toReturn
    }

    /**
     * Class secondary main function (and one of two, with *public* visibility modifier).
     * Returns [base schedule][schedule] with [merged][DaySchedule.mergeWithChanges] [changes][Changes].
     *
     * Represents original (".getDayScheduleWithChanges()") function from old *AdminTools*.
     * Because it won't be used often, I moved it down in functions order.
     */
    fun getChangedSchedule(schedule: DaySchedule, groupName: String, day: Day?): DaySchedule {
        val changes = getChanges(groupName, day)
        if (changes != null) {
            println("Automatic merge tool found empty changes." +
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
    }
    /* endregion */
}
