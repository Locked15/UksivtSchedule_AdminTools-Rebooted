package model.element.schedule


/**
 * Class, that represents whole week schedule.
 *
 * It indented to identify group schedule, so it contains [property][groupName] with group name.
 */
class WeekSchedule(var groupName: String?, var daySchedules: List<DaySchedule>) {
	
	/* region Additional Constructors */
	
	/**
	 * Empty constructor.
	 *
	 * Write 'NULL' value to [groupName], and empty list to [daySchedules].
	 */
	constructor() : this(null, listOf<DaySchedule>())
	/* endregion */
}
