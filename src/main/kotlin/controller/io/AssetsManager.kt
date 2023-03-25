package controller.io

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import controller.io.service.PathResolver.Companion.finalResourcePath
import controller.io.service.PathResolver.Companion.finalSchedulesResourceFolderPath
import controller.io.service.writer.writeChanges
import controller.io.service.writer.writeSchedule
import model.data.change.day.GeneralChangesOfDay
import model.data.change.day.TargetedChangesOfDay
import model.data.change.day.common.AbstractChangesOfDay
import model.data.schedule.origin.week.GeneralWeekSchedule
import model.data.schedule.origin.week.TargetedWeekSchedule
import model.data.schedule.origin.week.common.AbstractWeekSchedule
import model.data.schedule.result.day.GeneralDayScheduleResult
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths


private const val WRITER_BUFFER_SIZE = 4096

/* region Asset Reading */

inline fun <reified T> readUnknownAsset(path: Path): T? {
    val target = File(path.toUri())
    return try {
        val serializer = jacksonObjectMapper()
        val result = serializer.readValue<T>(target)

        result
    }
    catch (io: IOException) {
        println("ERROR:\nIO exception happened on asset file reading. " +
                        "Stack trace: ${io.localizedMessage}.")
        null
    }
    catch (mapping: JsonMappingException) {
        println("ERROR:\nSent type isn't equal to asset file JSON structure." +
                        "Stack trace: ${mapping.localizedMessage}.")
        null
    }
}

/* region Basic Schedule */

fun readScheduleAsset(branch: String, affiliation: String, group: String): TargetedWeekSchedule? {
    val target = File(Paths.get(finalResourcePath.toString(), branch, affiliation, "$group.json").toUri())
    return try {
        val serializer = jacksonObjectMapper()
        val result = serializer.readValue<TargetedWeekSchedule>(target)

        result
    }
    catch (exception: IOException) {
        println("ERROR:\nIO exception happened on asset file reading. " +
                        "Stack trace:\n${exception.localizedMessage}.")
        null
    }
}
/* endregion */
/* endregion */

/* region Asset Writing */

/* region Basic Schedule */

fun writeBasicScheduleToTargetFile(schedule: AbstractWeekSchedule): Boolean {
    return when (schedule) {
        is TargetedWeekSchedule -> writeSchedule(schedule)
        is GeneralWeekSchedule -> writeSchedule(schedule)
        else -> false
    }
}

fun writeBasicScheduleToUnitedAsset(fileName: String, schedules: GeneralWeekSchedule): Boolean {
    val finalPath = Paths.get(finalResourcePath.toString(), "$fileName.json")
    return try {
        val serializer = jacksonObjectMapper()
        val writer = FileWriter(File(finalPath.toUri()))
        val buffered = BufferedWriter(writer, WRITER_BUFFER_SIZE)

        buffered.write(serializer.writerWithDefaultPrettyPrinter().writeValueAsString(schedules))
        buffered.close()

        true
    }
    catch (exception: IOException) {
        println("ERROR:\nError occurred on united schedule asset writing. " +
                        "Stack trace: ${exception.message}.")
        false
    }
}
/* endregion */

/* region Changes */

fun writeDayChangesToFile(changes: AbstractChangesOfDay): Boolean {
    return when (changes) {
        is TargetedChangesOfDay -> writeChanges(changes)
        is GeneralChangesOfDay -> writeChanges(changes)
        else -> false
    }
}
/* endregion */

/* region Final Schedule */

fun writeFinalSchedule(fileName: String, finalSchedules: GeneralDayScheduleResult): Boolean {
    val finalPath = Paths.get(finalSchedulesResourceFolderPath.toString(), "$fileName.json")
    return try {
        val serializer = jacksonObjectMapper()
        val writer = FileWriter(File(finalPath.toUri()))
        val buffered = BufferedWriter(writer, WRITER_BUFFER_SIZE)

        buffered.write(serializer.writerWithDefaultPrettyPrinter().writeValueAsString(finalSchedules))
        buffered.close()

        true
    }
    catch (exception: IOException) {
        println("ERROR:\nError occurred on final schedule asset writing. " +
                        "Stack trace: ${exception.message}.")
        false
    }
}
/* endregion */
/* endregion */
