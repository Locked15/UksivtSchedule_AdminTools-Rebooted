package model.data.schedule.result.day

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import model.data.schedule.origin.day.TargetedDaySchedule
import java.util.Calendar


class TargetedDayScheduleResult(val targetGroup: String?, val scheduleDate: Calendar?,
                                val schedule: TargetedDaySchedule) {
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
