package model.data.schedule.base.day

import model.data.schedule.base.day.Day.*
import java.text.SimpleDateFormat
import java.util.*


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

fun fromCalendarObject(calendar: Calendar?): Day? {
    return if (calendar == null) {
        println("WARNING:" +
                        "\n\tGot 'NULL' calendar value in 'DayUtils.fromCalendarObject'.")
        null
    }
    else {
        return when (Locale.getDefault()) {
            Locale.ENGLISH -> fromEnglishString(SimpleDateFormat("EEEE").format(calendar.time))
            else -> fromRussianString(SimpleDateFormat("EEEE").format(calendar.time))
        }
    }
}
