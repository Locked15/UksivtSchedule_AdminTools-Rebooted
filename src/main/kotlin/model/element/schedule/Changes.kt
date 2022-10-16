package model.element.schedule

import model.element.schedule.base.Lesson


/**
 * Class, that encapsulates Changes for the schedule.
 *
 * Earlier, it wasn't a class, just a combination of list with changes and boolean with absolute determination.
 */
class Changes(val changedLessons: MutableList<Lesson>, var absolute: Boolean) {
	
	/* region Constructors */
	
	/**
	 * Additional constructor, that write default values to properties.
	 * Info: [changedLessons] set to an empty list, [absolute] to false.
	 */
	constructor() : this(mutableListOf<Lesson>(), false)
	
	/**
	 * Additional constructor, that write changes to property.
	 * Another property ([absolute]) set to false.
	 */
	constructor(changes: MutableList<Lesson>) : this(changes, false)
	/* endregion */
}
