package model.data.schedule.base.day

import model.data.schedule.base.day.Day.*


/**
 * Function to get english day name of a [Day] element.
 * It can be useful, when you need to get value without 'UPPERCASE'.
 *
 * Saved to maintain backward compatibility with some modules.
 */
fun toString(day: Day?): String {
	return when (day) {
		MONDAY -> "Monday"
		TUESDAY -> "Tuesday"
		WEDNESDAY -> "Wednesday"
		THURSDAY -> "Thursday"
		FRIDAY -> "Friday"
		SATURDAY -> "Saturday"
		SUNDAY -> "Sunday"
		
		else -> "Unknown"
	}
}

/**
 * Function that converts english day name to [Day] element.
 *
 * Saved to maintain backward compatibility with some modules.
 */
fun fromString(s: String): Day? {
	return when (s) {
		"Monday" -> MONDAY
		"Tuesday" -> TUESDAY
		"Wednesday" -> WEDNESDAY
		"Thursday" -> THURSDAY
		"Friday" -> FRIDAY
		"Saturday" -> SATURDAY
		"Sunday" -> SUNDAY
		
		else -> null
	}
}
