package controller.io

import com.fasterxml.jackson.databind.ObjectMapper
import model.data.schedule.DaySchedule
import model.data.schedule.WeekSchedule
import model.data.schedule.base.Lesson
import model.data.schedule.base.day.Day
import java.io.FileWriter
import java.io.IOException


/* region Properties */

/**
 * Contains file name template for newly creating assets.
 */
private const val FILE_NAME_TEMPLATE = "%s.json"
/* endregion */

/* region Functions */

/**
 * Extracts group schedule from an available document.
 * Then writes its schedule to asset-file.
 *
 * Returns a [result][Boolean] of the process.
 */
fun writeSchedule(schedule: WeekSchedule): Boolean {
    return try {
        writeToFile(schedule.groupName, schedule)
    }
    catch (e: Exception) {
        println("\n\nOn writing error occurred: ${e.message}.")
        false
    }
}

/**
 * Automatically extracts schedules from an available document.
 * Then writes all get schedules to asset-files.
 *
 * Returns a [result][Boolean] of this process.
 */
fun writeSchedule(schedules: List<*>): Boolean {
    return try {
        schedules.forEach { schedule ->
            if (schedule is WeekSchedule) writeToFile(schedule.groupName, schedule)
        }
        true
    }
    catch (e: Exception) {
        println("\n\nOn writing error occurred: ${e.message}.")
        false
    }
}

/**
 * Writes [given schedule][schedule] of [group] to asset-file.
 * File will be placed inside "resources" directory.
 */
private fun writeToFile(group: String?, schedule: WeekSchedule): Boolean {
    val serializer = ObjectMapper()
    val serializedValue = serializer.writerWithDefaultPrettyPrinter().writeValueAsString(schedule)
    return try {
        val stream = FileWriter("D:\\Java-Projects\\Schedule-AdminTools\\src\\main\\resources\\${
            String.format(FILE_NAME_TEMPLATE, group ?: "NotRecognized")
        }")
        stream.write(serializedValue)
        stream.close()

        true
    }
    catch (e: IOException) {
        false
    }
}
/* endregion */

/**
 * Generates schedules.
 * By user input.
 *
 * If you want to generate a schedule, firstly change these lines of code.
 */
private fun generateSchedules(): List<DaySchedule> {
    // Monday. And...
    val monSchedule = DaySchedule(Day.MONDAY)
    monSchedule.lessons.add(Lesson(0))
    monSchedule.lessons.add(Lesson(1))
    monSchedule.lessons.add(Lesson(2))
    monSchedule.lessons.add(Lesson(3))
    monSchedule.lessons.add(Lesson(4))
    monSchedule.lessons.add(Lesson(5))
    monSchedule.lessons.add(Lesson(6))

    // Tuesday. End...
    val tueSchedule = DaySchedule(Day.TUESDAY)
    tueSchedule.lessons.add(Lesson(0))
    tueSchedule.lessons.add(Lesson(1))
    tueSchedule.lessons.add(Lesson(2))
    tueSchedule.lessons.add(Lesson(3))
    tueSchedule.lessons.add(Lesson(4))
    tueSchedule.lessons.add(Lesson(5))
    tueSchedule.lessons.add(Lesson(6))

    // Wednesday. Is...
    val wedSchedule = DaySchedule(Day.WEDNESDAY)
    wedSchedule.lessons.add(Lesson(0))
    wedSchedule.lessons.add(Lesson(1))
    wedSchedule.lessons.add(Lesson(2))
    wedSchedule.lessons.add(Lesson(3))
    wedSchedule.lessons.add(Lesson(4))
    wedSchedule.lessons.add(Lesson(5))
    wedSchedule.lessons.add(Lesson(6))

    // Thursday. Where...
    val thuSchedule = DaySchedule(Day.THURSDAY)
    thuSchedule.lessons.add(Lesson(0))
    thuSchedule.lessons.add(Lesson(1))
    thuSchedule.lessons.add(Lesson(2))
    thuSchedule.lessons.add(Lesson(3))
    thuSchedule.lessons.add(Lesson(4))
    thuSchedule.lessons.add(Lesson(5))
    thuSchedule.lessons.add(Lesson(6))

    // Friday. We...
    val friSchedule = DaySchedule(Day.FRIDAY)
    friSchedule.lessons.add(Lesson(0))
    friSchedule.lessons.add(Lesson(1))
    friSchedule.lessons.add(Lesson(2))
    friSchedule.lessons.add(Lesson(3))
    friSchedule.lessons.add(Lesson(4))
    friSchedule.lessons.add(Lesson(5))
    friSchedule.lessons.add(Lesson(6))

    // Saturday. Can...
    val satSchedule = DaySchedule(Day.SATURDAY)
    satSchedule.lessons.add(Lesson(0))
    satSchedule.lessons.add(Lesson(1))
    satSchedule.lessons.add(Lesson(2))
    satSchedule.lessons.add(Lesson(3))
    satSchedule.lessons.add(Lesson(4))
    satSchedule.lessons.add(Lesson(5))
    satSchedule.lessons.add(Lesson(6))

    /* Sunday. Begin! */
    val sunSchedule = DaySchedule(Day.SUNDAY)
    sunSchedule.lessons.add(Lesson(0))
    sunSchedule.lessons.add(Lesson(1))
    sunSchedule.lessons.add(Lesson(2))
    sunSchedule.lessons.add(Lesson(3))
    sunSchedule.lessons.add(Lesson(4))
    sunSchedule.lessons.add(Lesson(5))
    sunSchedule.lessons.add(Lesson(6))

    return listOf(monSchedule, tueSchedule, wedSchedule, thuSchedule, friSchedule,
                  satSchedule, sunSchedule)
}
