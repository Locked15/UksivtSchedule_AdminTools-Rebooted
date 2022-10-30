package controller.io

import com.fasterxml.jackson.databind.ObjectMapper
import model.data.schedule.WeekSchedule
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths


/* region Properties */

/**
 * Contains file name template for newly creating assets.
 */
private const val FILE_NAME_TEMPLATE = "%s.json"
/* endregion */

/* region Functions */

/**
 * Writes [schedule] to the file.
 * Returns a [result][Boolean] of the process.
 */
fun writeSchedule(schedule: WeekSchedule) = writeToFile(schedule.groupName, schedule)

/**
 * Writes [all available schedules][schedules] to the corresponding files.
 * Returns a [result][Boolean] of this process.
 */
fun writeSchedule(schedules: List<*>) = schedules.all { schedule ->
    if (schedule is WeekSchedule) writeToFile(schedule.groupName, schedule)
    else false
}

/**
 * Writes [given schedule][schedule] of [group] to asset-file.
 * File will be placed inside the "resources" directory.
 */
private fun writeToFile(group: String?, schedule: WeekSchedule): Boolean {
    val serializer = ObjectMapper()
    val serializedValue = serializer.writerWithDefaultPrettyPrinter().writeValueAsString(schedule)
    return try {
        val stream = FileWriter(getFilePath(group ?: "NotRecognized").toFile(), false)
        stream.write(serializedValue)
        stream.close()

        true
    }
    catch (e: IOException) {
        println("\n\nOn writing error occurred: ${e.message}.")
        false
    }
}

/**
 * Returns the file [path][Path] for writing asset-file.
 * It contains [group name][group].
 */
private fun getFilePath(group: String) = Paths.get(System.getProperty("user.dir"), "src", "main", "resources",
                                                   String.format(FILE_NAME_TEMPLATE, group))
/* endregion */
