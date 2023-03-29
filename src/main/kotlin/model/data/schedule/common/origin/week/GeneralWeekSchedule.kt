package model.data.schedule.common.origin.week

import model.data.schedule.base.day.Day
import model.data.schedule.common.origin.day.GeneralDaySchedule
import model.data.schedule.common.origin.day.TargetedDaySchedule
import model.data.schedule.common.origin.week.base.BasicWeekSchedule


class GeneralWeekSchedule(c: MutableList<out TargetedWeekSchedule?>) : ArrayList<TargetedWeekSchedule?>(c),
                                                                       BasicWeekSchedule {

    fun getGeneralDayScheduleByDay(day: Day?): GeneralDaySchedule {
        val results = mutableListOf<TargetedDaySchedule?>()
        for (element in this) {
            results.add(element?.getDayScheduleByDay(day))
        }

        return GeneralDaySchedule(results)
    }
}
