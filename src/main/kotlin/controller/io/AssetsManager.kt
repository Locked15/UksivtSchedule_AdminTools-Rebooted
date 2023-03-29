package controller.io

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import controller.io.service.PathResolver.Companion.finalResourcePath
import controller.io.service.writer.BasicScheduleWriter
import controller.io.service.writer.ChangesWriter
import controller.io.service.writer.FinalScheduleWriter
import model.data.change.day.GeneralChangesOfDay
import model.data.change.day.TargetedChangesOfDay
import model.data.change.day.base.BasicChangesOfDay
import model.data.schedule.common.result.BasicFinalSchedule
import model.data.schedule.common.origin.week.GeneralWeekSchedule
import model.data.schedule.common.origin.week.TargetedWeekSchedule
import model.data.schedule.common.origin.week.base.BasicWeekSchedule
import model.data.schedule.common.result.day.GeneralFinalDaySchedule
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths


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

fun writeBasicScheduleToTargetFile(schedule: BasicWeekSchedule): Boolean {
    return when (schedule) {
        is TargetedWeekSchedule -> BasicScheduleWriter.beginWritingStandaloneSchedule(schedule)
        is GeneralWeekSchedule -> BasicScheduleWriter.beginWritingSchedulesToSplitFiles(schedule)

        else -> false
    }
}

fun writeBasicScheduleToUnitedAsset(fileName: String, schedules: GeneralWeekSchedule) = BasicScheduleWriter
    .beginWritingToUnitedFile(fileName, schedules)
/* endregion */

/* region Changes */

fun writeChangesToAssetFile(changes: BasicChangesOfDay): Boolean {
    return when (changes) {
        is TargetedChangesOfDay -> ChangesWriter.beginWritingToTargetFile(changes)
        is GeneralChangesOfDay -> ChangesWriter.beginWritingToUnitedFile(changes)

        else -> false
    }
}
/* endregion */

/* region Final Schedule */

fun writeFinalSchedule(fileName: String?, finalSchedule: BasicFinalSchedule): Boolean {
    return when (finalSchedule) {
        is GeneralFinalDaySchedule -> FinalScheduleWriter.beginWriteFinalSchedule(fileName, finalSchedule)

        else -> false
    }
}
/* endregion */
/* endregion */
