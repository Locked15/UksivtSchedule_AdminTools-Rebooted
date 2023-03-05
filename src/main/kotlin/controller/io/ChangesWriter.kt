package controller.io

import com.fasterxml.jackson.databind.ObjectMapper
import model.data.changes.TargetedChangesOfDay
import model.data.changes.GeneralChangesOfDay
import java.io.FileWriter
import java.nio.file.Path
import java.nio.file.Paths


/* region Properties */

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
private const val UNITED_FILE_NAME_TEMPLATE = "Changes (%d.%d.%d!). Available-Count — %d.json"
/* endregion */

/* region Functions */

/**
 * Writes [targeted changes of day][TargetedChangesOfDay] object to the file.
 *
 * Returns a [result][Boolean] of the process.
 */
fun writeChanges(changes: TargetedChangesOfDay) = writeToTargetFile(changes)

fun writeChanges(generalChangesOfDay: GeneralChangesOfDay) = writeToUnitedFile(generalChangesOfDay)

/**
 * Writes [value][targetChanges] to the file.
 * File will be placed inside the "resources" directory.
 */
private fun writeToTargetFile(targetChanges: TargetedChangesOfDay): Boolean {
    val serializer = ObjectMapper()
    val serializedValue = serializer.writerWithDefaultPrettyPrinter().writeValueAsString(targetChanges)
    return try {
        val stream = FileWriter(getTargetFilePath(targetChanges.isAbsolute).toFile(), false)
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
 * Writes [value][generalChangesOfDay] to the file.
 * File will be placed inside the "resources" directory.
 */
private fun writeToUnitedFile(generalChangesOfDay: GeneralChangesOfDay): Boolean {
    val serializer = ObjectMapper()
    val serializedValue = serializer.writerWithDefaultPrettyPrinter().writeValueAsString(generalChangesOfDay)
    return try {
        val dateAtomicValues = generalChangesOfDay.getAtomicDateValues()
        val stream =
            FileWriter(getUnitedFilePath(dateAtomicValues.first, dateAtomicValues.second, dateAtomicValues.third,
                                         generalChangesOfDay.changes.size).toFile(), false)
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
private fun getTargetFilePath(absolute: Boolean) = Paths.get(System.getProperty("user.dir"), "src", "main", "resources",
                                                             String.format(TARGET_FILE_NAME_TEMPLATE, absolute))

/**
 * Returns the file [path][Path] for writing generalChangesOfDay.
 * It contains [day of month][day], [month number][month], [year] and [declared changed schedules count][count].
 */
private fun getUnitedFilePath(day: Int, month: Int, year: Int,
                              count: Int) = Paths.get(System.getProperty("user.dir"), "src", "main", "resources",
                                                      String.format(UNITED_FILE_NAME_TEMPLATE, day, month, year, count)
)
/* endregion */
