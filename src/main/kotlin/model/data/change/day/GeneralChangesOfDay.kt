package model.data.change.day

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import model.data.change.day.base.BasicChangesOfDay
import model.data.schedule.base.day.Day
import model.data.schedule.base.day.fromCalendarObject
import java.util.*


/**
 * This is class, that needed to write last result and check its type correctly.
 * I made it, because in Kotlin no stable way to check generic type to equality.
 */
class GeneralChangesOfDay(val changes: List<TargetedChangesOfDay?>) : BasicChangesOfDay {

    /**
     * Private field, contains [changes date][Calendar].
     *
     * It created for more comfortable use of this class.
     * You don't need to iterate changes list and found actually date. You can call this one.
     * It automatically initializes, when [changes] property is assigned.
     */
    val changesDate: Calendar? = changes.first { change -> change?.changesDate != null }?.changesDate

    @JsonIgnore
    fun getChangesBasicDay(): Day? {
        val result = fromCalendarObject(changesDate)
        if (result == null) {
            println("WARNING:\n\t'Day' object for 'getChangesDay' in 'GeneralChangesOfDay' was null.")
        }

        return result
    }

    /**
     * Returns atomic values for changes date.
     * Atomic values wrapped inside [Triple] object.
     *
     * Technically, values contains followed properties:
     * * 'first' — Changes date, day of month (i.e. 12, 16, 30, etc);
     * * 'second' — Changes month, NOT INDEX (i.e. 1, 4, 12, etc);
     * * 'third' — Changes year (i.e. 2022, 2023, etc).
     */
    @JsonIgnore
    fun getAtomicDateValues(): Triple<Int?, Int?, Int?> {
        val dayOfMonth = changesDate?.get(Calendar.DAY_OF_MONTH)
        val monthIndex = changesDate?.get(Calendar.MONTH)
        val year = changesDate?.get(Calendar.YEAR)

        return Triple(dayOfMonth, monthIndex?.plus(1), year)
    }

    @JsonIgnore
    fun getTargetChangeByGroupName(groupName: String?) = changes.firstOrNull { change ->
        change?.targetGroup.equals(groupName, true)
    }

    override fun toString(): String = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
        .writeValueAsString(this)
}
