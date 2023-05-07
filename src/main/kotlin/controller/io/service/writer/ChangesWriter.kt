package controller.io.service.writer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import controller.io.service.PathResolver
import controller.io.service.writer.base.ValueWriter
import model.data.change.day.TargetedChangesOfDay
import model.data.change.day.GeneralChangesOfDay
import java.io.FileWriter
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths


class ChangesWriter : ValueWriter {

    companion object {

        /* region Constants */

        /**
         * Contains file name template for newly creating targeted changes of day file.
         */
        private const val TARGET_FILE_NAME_TEMPLATE = "%b.json"

        /**
         * Contains file name template for newly creating generalChangesOfDay file.
         * Supposed to be used with united file ([GeneralChangesOfDay] object).
         *
         * Name template contains date, month, year and general changed schedules count.
         */
        private const val UNITED_FILE_NAME_TEMPLATE = "Date %d.%d.%d!. Available-Count — %d.json"
        /* endregion */

        /* region Writing to Target File */

        /**
         * Writes [targeted changes of day][TargetedChangesOfDay] object to the file.
         *
         * Returns a [result][Boolean] of the process.
         */
        fun beginWritingToTargetFile(changes: TargetedChangesOfDay) = writeToTargetFile(changes)

        /**
         * Writes [value][targetChanges] to the file.
         * File will be placed inside the "resources" directory.
         */
        private fun writeToTargetFile(targetChanges: TargetedChangesOfDay): Boolean {
            val serializedValue =
                jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(targetChanges)
            return try {
                val stream = FileWriter(getTargetFilePath(targetChanges.isAbsolute).toFile(),
                                        Charset.forName(ValueWriter.DEFAULT_ENCODING), false)
                stream.write(serializedValue)
                stream.close()

                true
            }
            catch (e: Exception) {
                println("\n\nOn writing error occurred: ${e.message}.")
                false
            }
        }

        /**
         * Returns the file [path][Path] for writing targeted changes object.
         * It contains [absolute value][absolute] declaration variable.
         */
        private fun getTargetFilePath(absolute: Boolean): Path {
            val path = PathResolver.changesResourceFolderPath
            return Paths.get(path.toString(), String.format(TARGET_FILE_NAME_TEMPLATE, absolute))
        }
        /* endregion */

        /* region Writing to United File */

        fun beginWritingToUnitedFile(generalChangesOfDay: GeneralChangesOfDay) = writeToUnitedFile(generalChangesOfDay)

        /**
         * Writes [value][generalChangesOfDay] to the file.
         * File will be placed inside the "resources" directory.
         */
        private fun writeToUnitedFile(generalChangesOfDay: GeneralChangesOfDay): Boolean {
            val serializedValue =
                jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(generalChangesOfDay)
            return try {
                val dateAtomicValues = generalChangesOfDay.getAtomicDateValues()
                val stream = FileWriter(getUnitedFilePath(dateAtomicValues.first ?: -1, dateAtomicValues.second ?: -1,
                                                          dateAtomicValues.third ?: -1,
                                                          generalChangesOfDay.changes.size).toFile(),
                                        Charset.forName(ValueWriter.DEFAULT_ENCODING), false)
                stream.write(serializedValue)
                stream.close()

                true
            }
            catch (e: Exception) {
                println("\n\nOn writing error occurred: ${e.message}.")
                false
            }
        }

        /**
         * Returns the file [path][Path] for writing generalChangesOfDay.
         * It contains [day of month][day], [month number][month], [year] and [declared changed schedules count][count].
         */
        private fun getUnitedFilePath(day: Int, month: Int, year: Int, count: Int): Path {
            val path = PathResolver.changesResourceFolderPath
            return Paths.get(path.toString(), String.format(UNITED_FILE_NAME_TEMPLATE, day, month, year, count))
        }
        /* endregion */
    }
}
