package model.data.schedule.common.result.day

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import model.data.schedule.common.result.day.base.BasicFinalDaySchedule
import java.util.*


class GeneralFinalDaySchedule(resultingSchedules: List<TargetedFinalDaySchedule>) :
        ArrayList<TargetedFinalDaySchedule>(resultingSchedules), BasicFinalDaySchedule {

    private val targetDate: Calendar? = resultingSchedules.first { result ->
        result.scheduleDate != null
    }.scheduleDate

    /**
     * Returns atomic values for schedule target date.
     * Atomic values wrapped inside [Triple] object.
     *
     * Technically, values contains followed properties:
     * * 'first' — Schedule date, day of month (i.e. 12, 16, 30, etc);
     * * 'second' — Schedule month, NOT INDEX (i.e. 1, 4, 12, etc);
     * * 'third' — Schedule year (i.e. 2022, 2023, etc).
     */
    fun getAtomicDateValues(): Triple<Int?, Int?, Int?> {
        val dayOfMonth = targetDate?.get(Calendar.DAY_OF_MONTH)
        val monthIndex = targetDate?.get(Calendar.MONTH)
        val year = targetDate?.get(Calendar.YEAR)

        // I must increase 'monthIndex' property, because it's index (and starts with '0').
        return Triple(dayOfMonth, monthIndex?.plus(1), year)
    }

    override fun toString(): String = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
        .writeValueAsString(this)
}
