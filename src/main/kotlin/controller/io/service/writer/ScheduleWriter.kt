package controller.io.service.writer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import model.data.schedule.origin.week.GeneralWeekSchedule
import model.data.schedule.origin.week.TargetedWeekSchedule
import projectDirectory
import resourcePathElements
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths


/* region Functions */

/**
 * Writes [schedule] to the file.
 * Returns a [result][Boolean] of the process.
 */
fun writeSchedule(schedule: TargetedWeekSchedule) = writeToTargetFile(schedule.groupName, schedule)

/**
 * Writes [all available schedules][basicSchedules] to the corresponding files.
 * Returns a [result][Boolean] of this process.
 */
fun writeSchedule(basicSchedules: GeneralWeekSchedule) = basicSchedules.all { schedule ->
    schedule?.let {
        writeToTargetFile(it.groupName, it)
    } ?: false
}

/**
 * Writes [given schedule][schedule] of [group] to asset-file.
 * File will be placed inside the "resources" directory.
 */
private fun writeToTargetFile(group: String?, schedule: TargetedWeekSchedule): Boolean {
    val serializedValue = jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(schedule)
    return try {
        val stream = FileWriter(getTargetFilePath(group ?: "NotRecognized").toString(), false)
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
private fun getTargetFilePath(group: String): Path? {
    var path = Paths.get(projectDirectory)
    resourcePathElements.forEach { path = path.resolve(it) }

    return path.resolve("$group.json")
}
/* endregion */
