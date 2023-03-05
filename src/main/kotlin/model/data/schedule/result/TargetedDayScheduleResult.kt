package model.data.schedule.result

import model.data.schedule.origin.TargetedDaySchedule
import java.util.Calendar


class TargetedDayScheduleResult(val scheduleDate: Calendar?, var scheduleIsChanged: Boolean,
                                val schedule: TargetedDaySchedule)
