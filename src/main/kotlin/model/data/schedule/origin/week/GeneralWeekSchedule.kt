package model.data.schedule.origin.week

import model.data.schedule.base.day.Day
import model.data.schedule.origin.day.GeneralDaySchedule
import model.data.schedule.origin.day.TargetedDaySchedule
import model.data.schedule.origin.week.common.AbstractWeekSchedule


class GeneralWeekSchedule(c: MutableList<out TargetedWeekSchedule?>) : ArrayList<TargetedWeekSchedule?>(c),
                                                                      AbstractWeekSchedule {

    fun getGeneralDayScheduleByDay(day: Day?): GeneralDaySchedule {
        val results = mutableListOf<TargetedDaySchedule?>()
        for (element in this) {
            results.add(element?.getDayScheduleByDay(day))
        }

        return GeneralDaySchedule(results)
    }
}
