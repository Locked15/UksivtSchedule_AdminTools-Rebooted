package model.data.schedule

import model.data.schedule.base.Lesson
import java.util.Calendar


/**
 * Class, that encapsulates Changes for the schedule.
 *
 * Earlier, it wasn't a class, just a combination of list with changes and boolean with absolute determination.
 */
class Changes(val changedLessons: MutableList<Lesson>, var isAbsolute: Boolean, var changesDate: Calendar?,
              var targetGroup: String?) {

    /* region Constructors */

    /**
     * Additional constructor, that write default values to properties.
     *
     * Info: [changedLessons] set to an empty list, [isAbsolute] to false.
     */
    constructor() : this(mutableListOf<Lesson>(), false, null, null)

    /**
     * Additional constructor, that write default values to properties.
     *
     * Info: [changedLessons] set to an empty list, [isAbsolute] to false.
     * Writes [sent date][date] to [changesDate] property.
     */
    constructor(date: Calendar?) : this(mutableListOf<Lesson>(), false, date, null)

    /**
     * Additional constructor, that write default values to properties.
     *
     * Info: [changedLessons] set to an empty list, [isAbsolute] to false.
     * Writes [sent date][date] to [changesDate] property.
     * Also writes [target] to the [targetGroup] property.
     */
    constructor(target: String?, date: Calendar?) : this(mutableListOf<Lesson>(), false, date, target)
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
         * Contains template for string-formatting instances of [Changes] objects.
         */
        const val STRING_BODY_TEMPLATE =
            """
                Target: %s;
                IsAbsolute: %b;
                Date: %s;
                Changes:
                {
                    %s.
                }
            """

        /**
         * Generates a template [changes][Changes] object with practise value.
         * Returned value can be merged with [base schedule][DaySchedule] to get final "On Practise" schedule.
         */
        fun getOnPractiseChanges(date: Calendar?, target: String?): Changes {
            val changes = mutableListOf<Lesson>()
            for (i in 0..6) {
                changes.add(Lesson(i, "Практика"))
            }

            return Changes(changes, true, date, target)
        }

        /**
         * Generates a template [changes][Changes] object with 'Ликвидация Задолженностей' values.
         */
        fun getDebtLiquidationChanges(date: Calendar?, target: String?): Changes {
            val changes = mutableListOf<Lesson>()
            for (i in 0..6) {
                changes.add(Lesson(i, "Ликвидация Задолженностей"))
            }

            return Changes(changes, true, date, target)
        }
    }
    /* endregion */
}
