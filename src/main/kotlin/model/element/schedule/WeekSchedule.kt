package model.element.schedule


/**
 * Class, that represents whole week schedule.
 *
 * It indented to identify group schedule, so it contains [property][groupName] with group name.
 */
class WeekSchedule(var groupName: String?, var daySchedules: MutableList<DaySchedule>) {
	
	/* region Constructors */

	/**
	 * Creates a new instance of [WeekSchedule].
	 * New instance with given [group name][groupName].
	 * Schedules initializes with [empty list][listOf].
	 */
	constructor(groupName: String?): this(groupName, mutableListOf())
	/* endregion */
}
