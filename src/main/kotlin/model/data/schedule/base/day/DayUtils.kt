package model.data.schedule.base.day

import model.data.schedule.base.day.Day.*


/**
 * Function that converts english day name to [Day] element.
 *
 * Saved to maintain backward compatibility with some modules.
 * Splatted to two functions: [english][fromEnglishString] and [russian][fromRussianString] versions.
 */
fun fromEnglishString(name: String) = when (name.lowercase()) {
    "monday" -> MONDAY
    "tuesday" -> TUESDAY
    "wednesday" -> WEDNESDAY
    "thursday" -> THURSDAY
    "friday" -> FRIDAY
    "saturday" -> SATURDAY
    "sunday" -> SUNDAY

    else -> null
}

/**
 * Function that converts russian day name to [Day] element.
 *
 * Saved to maintain backward compatibility with some modules.
 * Splatted to two functions: [english][fromEnglishString] and [russian][fromRussianString] versions.
 */
fun fromRussianString(name: String) = when (name.lowercase()) {
    "понедельник" -> MONDAY
    "вторник" -> TUESDAY
    "среда" -> WEDNESDAY
    "четверг" -> THURSDAY
    "пятница" -> FRIDAY
    "суббота" -> SATURDAY
    "воскресенье" -> SUNDAY

    else -> null
}
