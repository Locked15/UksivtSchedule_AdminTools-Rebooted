package model.data.schedule.common.origin.week

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.ObjectMapper
import model.data.schedule.base.day.Day
import model.data.schedule.common.origin.day.TargetedDaySchedule
import model.data.schedule.common.origin.week.base.BasicWeekSchedule


/**
 * Class, that represents whole week schedule.
 *
 * It indented to identify group schedule, so it contains [property][groupName] with group name.
 */
class TargetedWeekSchedule(var groupName: String?,
                           @JsonAlias("daySchedules") var targetedDaySchedules: MutableList<TargetedDaySchedule>) :
        BasicWeekSchedule {

    /* region Constructors */

    /**
     * Creates a new instance of [TargetedWeekSchedule].
     * New instance with given [group name][groupName].
     * Schedules initializes with [empty list][listOf].
     */
    constructor(groupName: String?) : this(groupName, mutableListOf())
    /* endregion */

    /* region Functions */

    fun getDayScheduleByDay(day: Day?): TargetedDaySchedule? {
        val index = day?.index ?: -1
        return targetedDaySchedules.getOrNull(index)
    }

    /**
     * Returns string representation of the [object][TargetedWeekSchedule].
     */
    override fun toString(): String {
        val serializer = ObjectMapper()
        return serializer.writerWithDefaultPrettyPrinter().writeValueAsString(this)
    }
    /* endregion */
}
