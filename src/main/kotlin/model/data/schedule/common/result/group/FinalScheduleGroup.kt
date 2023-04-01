package model.data.schedule.common.result.group

import model.data.schedule.common.result.BasicFinalSchedule
import model.data.schedule.common.result.day.GeneralFinalDaySchedule


class FinalScheduleGroup(c: MutableCollection<out GeneralFinalDaySchedule>) : ArrayList<GeneralFinalDaySchedule>(c),
                                                                              BasicFinalSchedule
