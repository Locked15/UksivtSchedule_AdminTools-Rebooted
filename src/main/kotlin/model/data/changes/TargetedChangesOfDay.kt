package model.data.changes

import model.data.schedule.base.Lesson
import model.data.schedule.origin.TargetedDaySchedule
import java.util.Calendar


/**
 * Class, that encapsulates TargetedChangesOfDay for the schedule.
 *
 * Earlier, it wasn't a class, just a combination of list with [Changes][TargetedChangesOfDay]
 * and boolean, that defines is absolute that change or not.
 */
class TargetedChangesOfDay(var targetGroup: String?, var isAbsolute: Boolean, var changesDate: Calendar?,
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
         * Contains template for string-formatting instances of [TargetedChangesOfDay] objects.
         */
        const val STRING_BODY_TEMPLATE =
            """
                Target: %s;
                IsAbsolute: %b;
                Date: %s;
                TargetedChangesOfDay:
                {
                    %s.
                }
            """

        /**
         * Generates a template [changes][TargetedChangesOfDay] object with practise value.
         * Returned value can be merged with [base schedule][TargetedDaySchedule] to get final "On Practise" schedule.
         */
        fun getOnPractiseChanges(date: Calendar?, target: String?): TargetedChangesOfDay {
            val changes = mutableListOf<Lesson>()
            for (i in 0..6) {
                changes.add(Lesson(i, "Практика"))
            }

            return TargetedChangesOfDay(target, true, date, changes)
        }

        /**
         * Generates a template [changes][TargetedChangesOfDay] object with 'Ликвидация Задолженностей' values.
         */
        fun getDebtLiquidationChanges(date: Calendar?, target: String?): TargetedChangesOfDay {
            val changes = mutableListOf<Lesson>()
            for (i in 0..6) {
                changes.add(Lesson(i, "Ликвидация Задолженностей"))
            }

            return TargetedChangesOfDay(target, true, date, changes)
        }
    }
    /* endregion */
}
