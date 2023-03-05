package model.data.schedule.origin

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.ObjectMapper


/**
 * Class, that represents whole week schedule.
 *
 * It indented to identify group schedule, so it contains [property][groupName] with group name.
 */
class TargetedWeekSchedule(var groupName: String?,
						   @JsonAlias("daySchedules") var targetedDaySchedules: MutableList<TargetedDaySchedule>) {
	
	/* region Constructors */

	/**
	 * Creates a new instance of [TargetedWeekSchedule].
	 * New instance with given [group name][groupName].
	 * Schedules initializes with [empty list][listOf].
	 */
	constructor(groupName: String?): this(groupName, mutableListOf())
	/* endregion */

	/* region Functions */

	/**
	 * Returns string representation of the [object][TargetedWeekSchedule].
	 */
	override fun toString(): String {
		val serializer = ObjectMapper()
		return serializer.writerWithDefaultPrettyPrinter().writeValueAsString(this)
	}
	/* endregion */
}
