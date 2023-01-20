package model.data.changes

import model.data.schedule.base.Lesson
import java.util.Calendar


/**
 * Class, that encapsulates TargetChangesOfDay for the schedule.
 *
 * Earlier, it wasn't a class, just a combination of list with targetChangesOfDay and boolean with absolute determination.
 */
class TargetChangesOfDay(var targetGroup: String?, var isAbsolute: Boolean, var changesDate: Calendar?,
                         val changedLessons: MutableList<Lesson>) {

    /* region Constructors */

    /**
     * Additional constructor, that write default values to properties.
     *
     * Info: [changedLessons] set to an empty list, [isAbsolute] to false.
     */
    constructor() : this(null, false, null, mutableListOf<Lesson>())

    /**
     * Additional constructor, that write default values to properties.
     *
     * Info: [changedLessons] set to an empty list, [isAbsolute] to false.
     * Writes [sent date][date] to [changesDate] property.
     */
    constructor(date: Calendar?) : this(null, false, date, mutableListOf<Lesson>())

    /**
     * Additional constructor, that write default values to properties.
     *
     * Info: [changedLessons] set to an empty list, [isAbsolute] to false.
     * Writes [sent date][date] to [changesDate] property.
     * Also writes [target] to the [targetGroup] property.
     */
    constructor(target: String?, date: Calendar?) : this(target, false, date, mutableListOf<Lesson>())
    /* endregion */

    /* region Functions */

    /**
     * Returns string representation of the current object.
     */
    override fun toString(): String {
        val builder = StringBuilder(changedLessons.size)
        for (lesson in changedLessons) {
            builder.append("${lesson.number} — ${lesson.name}.\n")
        }

        return String.format(STRING_BODY_TEMPLATE, targetGroup, isAbsolute, changesDate.toString(), builder.toString())
    }
    /* endregion */

    /* region Companion */

    companion object {

        /**
         * Contains template for string-formatting instances of [TargetChangesOfDay] objects.
         */
        const val STRING_BODY_TEMPLATE =
            """
                Target: %s;
                IsAbsolute: %b;
                Date: %s;
                TargetChangesOfDay:
                {
                    %s.
                }
            """

        /**
         * Generates a template [targetChangesOfDay][TargetChangesOfDay] object with practise value.
         * Returned value can be merged with [base schedule][DaySchedule] to get final "On Practise" schedule.
         */
        fun getOnPractiseChanges(date: Calendar?, target: String?): TargetChangesOfDay {
            val changes = mutableListOf<Lesson>()
            for (i in 0..6) {
                changes.add(Lesson(i, "Практика"))
            }

            return TargetChangesOfDay(target, true, date, changes)
        }

        /**
         * Generates a template [targetChangesOfDay][TargetChangesOfDay] object with 'Ликвидация Задолженностей' values.
         */
        fun getDebtLiquidationChanges(date: Calendar?, target: String?): TargetChangesOfDay {
            val changes = mutableListOf<Lesson>()
            for (i in 0..6) {
                changes.add(Lesson(i, "Ликвидация Задолженностей"))
            }

            return TargetChangesOfDay(target, true, date, changes)
        }
    }
    /* endregion */
}
