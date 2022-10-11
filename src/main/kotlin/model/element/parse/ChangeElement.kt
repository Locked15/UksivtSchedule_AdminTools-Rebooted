package model.element.parse

import model.element.schedule.base.day.Day


/**
 * Class, that represents one change element from college website.
 * It contains information about the following things:
 * - [Day of week][dayOfWeek]
 * - [Day of month][dayOfMonth]
 * - [Link to changes document][linkToDocument]
 *
 * Change elements are created based on the DOM elements of the site,
 * and if the structure of the site has changed, this class **probably must be updated too**.
 */
class ChangeElement(val dayOfWeek: Day, val dayOfMonth: Int, val linkToDocument: String?) : Comparable<ChangeElement> {
	
	/* region Properties */
	
	/**
	 * Returns current state of changes inside this element.
	 *
	 * This property is read-only and evaluates at a call moment.
	 */
	val haveChanges = !linkToDocument.isNullOrEmpty() && linkToDocument.isNotBlank()
	/* endregion */
	
	/* region Functions */
	
	/**
	 * Returns string representation of this object.
	 *
	 * Overrides basic method, inherited from [Object] class.
	 */
	override fun toString(): String = String.format(STRING_TEMPLATE,
		dayOfWeek.englishName,
		dayOfMonth,
		linkToDocument
	)
	
	/**
	 * Compares two objects and returns the result:
	 * - If this object is more than other — 1;
	 * - If this object is equal to another — 0;
	 * - If this object is less than other — -1.
	 *
	 * Inherited from [Comparable]<[ChangeElement]> interface.
	 */
	override fun compareTo(other: ChangeElement): Int {
		return if (dayOfMonth > other.dayOfMonth) 1
		else if (dayOfMonth == other.dayOfMonth) 0
		else -1
	}
	/* endregion */
	
	/* region Companion */
	
	companion object {
		
		/**
		 * Contains string template for creation a string representation of the object.
		 *
		 * Use it with [String.format] function.
		 */
		const val STRING_TEMPLATE =
			"""
				Change Element:
				{
					Day Of Week: %s;
					Day Of Month: %d;
					Link To Document: %s;
				}
			"""
	}
	/* endregion */
}
