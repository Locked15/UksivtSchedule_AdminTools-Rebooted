package controller.io.service.writer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import controller.io.service.PathResolver
import controller.io.service.writer.base.ValueWriter
import controller.view.Logger
import model.data.schedule.common.result.day.GeneralFinalDaySchedule
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths


class FinalScheduleWriter : ValueWriter {

    companion object {

        /* region Constants */

        private const val PRESERVED_FILE_NAME = "Final"

        private const val FILE_NAME_TEMPLATE = "%s (%d.%d.%d!).json"
        /* endregion */

        /* region Writing Final Schedule to File */

        fun beginWriteFinalSchedule(fileName: String?, finalSchedules: GeneralFinalDaySchedule): Boolean {
            val finalPath = getFinalScheduleFilePath(fileName, finalSchedules.getAtomicDateValues())
            return writeFinalScheduleToFile(finalPath, finalSchedules)
        }

        private fun writeFinalScheduleToFile(finalPath: Path, finalSchedules: GeneralFinalDaySchedule): Boolean {
            return try {
                val serializer = jacksonObjectMapper()
                val writer = FileWriter(File(finalPath.toUri()), Charset.forName(ValueWriter.DEFAULT_ENCODING), false)
                val buffered = BufferedWriter(writer, ValueWriter.WRITER_BUFFER_SIZE)

                buffered.write(serializer.writerWithDefaultPrettyPrinter().writeValueAsString(finalSchedules))
                buffered.close()

                true
            }
            catch (exception: IOException) {
                Logger.logException(exception, 1, "Error occurred on result schedule asset writing")
                false
            }
        }

        private fun getFinalScheduleFilePath(fileName: String?, date: Triple<Int?, Int?, Int?>) = Paths.get(
                PathResolver.finalSchedulesResourceFolderPath.toString(), String.format(FILE_NAME_TEMPLATE,
                                                                                        fileName ?: PRESERVED_FILE_NAME,
                                                                                        date.first ?: -1,
                                                                                        date.second ?: -1,
                                                                                        date.third ?: -1)
        )
        /* endregion */
    }
}
