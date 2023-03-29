package model.data.schedule.common.result.day

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import model.data.schedule.common.result.day.base.BasicFinalDaySchedule
import java.util.*


class GeneralFinalDaySchedule(resultingSchedules: List<TargetedFinalDaySchedule>) :
        ArrayList<TargetedFinalDaySchedule>(resultingSchedules), BasicFinalDaySchedule {

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
    fun getAtomicDateValues(): Triple<Int?, Int?, Int?> {
        // I must increase 'dayOfMonth' property, because instead it will return decreased by '1' value.
        val dayOfMonth = targetDate?.get(Calendar.DAY_OF_MONTH)?.plus(1)
        val monthIndex = (targetDate?.get(Calendar.MONTH)?.plus(1))
        val year = targetDate?.get(Calendar.YEAR)

        return Triple(dayOfMonth, monthIndex, year)
    }

    override fun toString(): String = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
        .writeValueAsString(this)
}
