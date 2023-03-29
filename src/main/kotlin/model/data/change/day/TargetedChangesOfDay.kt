package model.data.change.day

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import model.data.change.day.base.BasicChangesOfDay
import model.data.schedule.base.Lesson
import model.data.schedule.common.origin.day.TargetedDaySchedule
import java.util.Calendar


/**
 * Class, that encapsulates TargetedChangesOfDay for the schedule.
 *
 * Earlier, it wasn't a class, just a combination of list with [Changes][TargetedChangesOfDay]
 * and boolean, that defines is absolute that change or not.
 */
class TargetedChangesOfDay(var targetGroup: String?, @JsonAlias("absolute") var isAbsolute: Boolean,
                           var changesDate: Calendar?, val changedLessons: MutableList<Lesson>) : BasicChangesOfDay {

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
    override fun toString(): String = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
        .writeValueAsString(this)
    /* endregion */

    /* region Companion */

    companion object {

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
