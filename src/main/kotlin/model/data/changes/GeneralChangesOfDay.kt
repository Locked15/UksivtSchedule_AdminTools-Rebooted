package model.data.changes

import java.util.*


/**
 * This is class, that needed to write last result and check its type correctly.
 * I made it, because in Kotlin no stable way to check generic type to equality.
 */
class GeneralChangesOfDay(val changes: List<TargetedChangesOfDay?>) {

    /**
     * Private field, contains [changes date][Calendar].
     *
     * It created for more comfortable use of this class.
     * You don't need to iterate changes list and found actually date. You can call this one.
     * It automatically initializes, when [changes] property is assigned.
     */
    private val changesDate: Calendar? = changes.first { change -> change?.changesDate != null }?.changesDate

    /**
     * Returns atomic values for changes date.
     * Atomic values wrapped inside [Triple] object.
     *
     * Technically, values contains followed properties:
     * * 'first' — Changes date, day of month (i.e. 12, 16, 30, etc);
     * * 'second' — Changes month, NOT INDEX (i.e. 1, 4, 12, etc);
     * * 'third' — Changes year (i.e. 2022, 2023, etc).
     */
    fun getAtomicDateValues(): Triple<Int, Int, Int> {
        val dayOfMonth = changesDate?.get(Calendar.DAY_OF_MONTH) ?: 0
        val monthIndex = (changesDate?.get(Calendar.MONTH)?.plus(1)) ?: 0
        val year = changesDate?.get(Calendar.YEAR) ?: 0

        return Triple(dayOfMonth, monthIndex, year)
    }
}
