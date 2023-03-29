package controller.io.service.writer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import controller.io.service.PathResolver
import controller.io.service.writer.base.ValueWriter
import model.data.schedule.common.origin.week.GeneralWeekSchedule
import model.data.schedule.common.origin.week.TargetedWeekSchedule
import projectDirectory
import resourcePathElements
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths


class BasicScheduleWriter : ValueWriter {

    companion object {

        /* region Constants */

        private const val TARGET_FILE_NAME_TEMPLATE = "%s.json"

        private const val UNITED_FILE_NAME_TEMPLATE = "%s (%d).json"
        /* endregion */

        /* region Writing to Target File(-s) */

        /**
         * Writes [all available schedules][basicSchedules] to the corresponding files.
         * Returns a [result][Boolean] of this process.
         */
        fun beginWritingSchedulesToSplitFiles(basicSchedules: GeneralWeekSchedule) = basicSchedules.all { schedule ->
            schedule?.let {
                beginWritingStandaloneSchedule(it)
            } ?: false
        }

        /**
         * Writes [schedule] to the file.
         * Returns a [result][Boolean] of the process.
         */
        fun beginWritingStandaloneSchedule(schedule: TargetedWeekSchedule) = writeToTargetFile(schedule.groupName,
                                                                                               schedule)

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
        private fun getTargetFilePath(group: String): Path {
            var path = Paths.get(projectDirectory)
            resourcePathElements.forEach { path = path.resolve(it) }

            return path.resolve(String.format(TARGET_FILE_NAME_TEMPLATE,
                                              group))
        }
        /* endregion */

        /* region Writing to United File */

        /**
         * Writes schedule to united file.
         */
        fun beginWritingToUnitedFile(fileName: String, schedules: GeneralWeekSchedule): Boolean {
            val finalPath = getUnitedFilePath(schedules.size, fileName)
            return try {
                val serializer = jacksonObjectMapper()
                val writer = FileWriter(File(finalPath.toUri()))
                val buffered = BufferedWriter(writer, ValueWriter.WRITER_BUFFER_SIZE)

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

        private fun getUnitedFilePath(size: Int, fileName: String) = Paths.get(
                PathResolver.finalResourcePath.toString(), String.format(UNITED_FILE_NAME_TEMPLATE, fileName, size))
        /* endregion */
    }
}
