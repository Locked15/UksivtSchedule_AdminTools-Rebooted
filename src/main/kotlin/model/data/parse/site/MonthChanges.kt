package model.data.parse.site

import model.data.schedule.base.day.Day
import java.util.*


class MonthChanges(val currentMonth: String?, val changes: List<ChangeElement>) {
	
	/* region Functions */
	
	/**
	 * Tries to find a change element by [Day] object.
	 *
	 * Calling this method cause [creation a copy][Collections.copy] of a list.
	 */
	fun tryToFindElementByDay(day: Day): ChangeElement? {
		val reversed = changes.sortedDescending()
		for (change in reversed) {
			if (change.dayOfWeek == day && change.haveChanges) return change
		}
		
		return null
	}
	
	/**
	 * Tries to find a change element by [day of month][dayNumber].
	 */
	fun tryToFindElementByNumberOfDay(dayNumber: Int): ChangeElement? {
		for (element in changes) {
			if (element.dayOfMonth == dayNumber && element.haveChanges) return element
		}
		
		return null
	}
	
	/**
	 * Creates a string representation of an [object][MonthChanges].
	 * Warning: [returned value][String] by this function will be **huge**.
	 */
	override fun toString(): String {
		val bodyBuilder = StringBuilder()
		for (change in changes) {
			bodyBuilder.append(change.toString())
		}
		
		return String.format(STRING_TEMPLATE,
							 currentMonth.toString(),
							 bodyBuilder.toString()
		)
	}
	/* endregion */
	
	/* region Companion */
	
	companion object {
		
		/**
		 * Template for create a string representation of an [object][MonthChanges].
		 *
		 * Use it with [String.format] function.
		 */
		const val STRING_TEMPLATE =
			"""
				New Month:
				{
					Current Month: %s;
					Changes:
					{
						%s
					}
				}
			"""
	}
	/* endregion */
}
