package model.data.schedule.common.result.day

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import model.data.schedule.common.origin.day.TargetedDaySchedule
import model.data.schedule.common.result.day.base.BasicFinalDaySchedule
import java.util.Calendar


class TargetedFinalDaySchedule(val targetGroup: String?, val scheduleDate: Calendar?,
                               val schedule: TargetedDaySchedule) : BasicFinalDaySchedule {

    /**
     * Public field with information about changed this schedule or not.
     *
     * This field works automatically, by parsing available schedules.
     * It was made to more efficient and easily using this class.
     */
    val isChanged: Boolean = schedule.lessons.any { lesson -> lesson.isChanged }

    override fun toString(): String = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
        .writeValueAsString(this)
}
