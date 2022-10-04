package model.elements.schedule

import model.elements.schedule.base.Lesson


/**
 * Class, that encapsulates Changes for the schedule.
 *
 * Earlier, it wasn't a class, just a combination of list with changes and boolean with absolute determination.
 */
class Changes(val changes: MutableList<Lesson>, var absolute: Boolean) {
	
	/* region Constructors */
	
	/**
	 * Additional constructor, that write default values to properties.
	 * Info: [changes] set to an empty list, [absolute] to false.
	 */
	constructor() : this(changes = mutableListOf<Lesson>(), absolute = false)
	
	/**
	 * Additional constructor, that write changes to property.
	 * Another property ([absolute]) set to false.
	 */
	constructor(changes: MutableList<Lesson>) : this(changes, false)
	/* endregion */
}
