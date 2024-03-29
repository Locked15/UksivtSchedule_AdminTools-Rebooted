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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TargetedChangesOfDay

        if (targetGroup != other.targetGroup) return false
        if (isAbsolute != other.isAbsolute) return false
        if (changesDate != other.changesDate) return false

        return changedLessons == other.changedLessons
    }

    /**
     * User-Override for hash code function.
     */
    override fun hashCode(): Int {
        var result = targetGroup?.hashCode() ?: 0
        result = 31 * result + isAbsolute.hashCode()
        result = 31 * result + (changesDate?.hashCode() ?: 0)
        result = 31 * result + changedLessons.hashCode()

        return result
    }

    /**
     * Returns string representation of the current object.
     */
    override fun toString(): String = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
        .writeValueAsString(Pair(this, hashCode()))
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
                changes.add(Lesson(i, "Практика", null, null, true))
            }

            return TargetedChangesOfDay(target, true, date, changes)
        }

        /**
         * Generates a template [changes][TargetedChangesOfDay] object with 'Ликвидация Задолженностей' values.
         */
        fun getDebtLiquidationChanges(date: Calendar?, target: String?): TargetedChangesOfDay {
            val changes = mutableListOf<Lesson>()
            for (i in 0..6) {
                changes.add(Lesson(i, "Ликвидация Задолженностей", null, null, true))
            }

            return TargetedChangesOfDay(target, true, date, changes)
        }
    }
    /* endregion */
}
