package model.data.schedule.result

import java.util.*


class GeneralDayScheduleResult(private val resultingSchedules: List<TargetedDayScheduleResult>) :
        ArrayList<TargetedDayScheduleResult>(resultingSchedules) {

    private val targetDate: Calendar? = resultingSchedules.first { result -> result.scheduleDate != null }.scheduleDate

    /**
     * Returns atomic values for schedule target date.
     * Atomic values wrapped inside [Triple] object.
     *
     * Technically, values contains followed properties:
     * * 'first' — Schedule date, day of month (i.e. 12, 16, 30, etc);
     * * 'second' — Schedule month, NOT INDEX (i.e. 1, 4, 12, etc);
     * * 'third' — Schedule year (i.e. 2022, 2023, etc).
     */
    fun getAtomicDateValues(): Triple<Int, Int, Int> {
        val dayOfMonth = targetDate?.get(Calendar.DAY_OF_MONTH) ?: 0
        val monthIndex = (targetDate?.get(Calendar.MONTH)?.plus(1)) ?: 0
        val year = targetDate?.get(Calendar.YEAR) ?: 0

        return Triple(dayOfMonth, monthIndex, year)
    }
}
