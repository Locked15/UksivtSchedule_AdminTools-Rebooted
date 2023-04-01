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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TargetedFinalDaySchedule

        if (targetGroup != other.targetGroup) return false
        if (scheduleDate != other.scheduleDate) return false
        if (schedule != other.schedule) return false
        return isChanged == other.isChanged
    }

    override fun hashCode(): Int {
        var result = targetGroup?.hashCode() ?: 0
        result = 31 * result + (scheduleDate?.hashCode() ?: 0)
        result = 31 * result + schedule.hashCode()
        result = 31 * result + isChanged.hashCode()
        return result
    }

    override fun toString(): String = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
        .writeValueAsString(this)
}
