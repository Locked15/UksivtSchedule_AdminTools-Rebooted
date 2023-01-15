package controller.io

import com.fasterxml.jackson.databind.ObjectMapper
import model.data.schedule.TargetChangesOfDay
import model.data.schedule.GeneralChangesOfDay
import java.io.FileWriter
import java.nio.file.Path
import java.nio.file.Paths


/* region Properties */

/**
 * Contains file name template for newly creating targetChangesOfDay file.
 */
private const val TARGET_FILE_NAME_TEMPLATE = "%b.json"

/**
 * Contains file name template for newly creating targetChangesOfDay file.
 * Supposed to be used with united file ([GeneralChangesOfDay] object).
 */
private const val UNITED_FILE_NAME_TEMPLATE = "%d.json"
/* endregion */

/* region Functions */

/**
 * Writes [targetChangesOfDay][TargetChangesOfDay] object to the file.
 *
 * Returns a [result][Boolean] of the process.
 */
fun writeChanges(targetChangesOfDay: TargetChangesOfDay) = writeToTargetFile(targetChangesOfDay)

fun writeChanges(generalChangesOfDay: GeneralChangesOfDay) = writeToUnitedFile(generalChangesOfDay.changes)

/**
 * Writes [value][targetChangesOfDay] to the file.
 * File will be placed inside the "resources" directory.
 */
private fun writeToTargetFile(targetChangesOfDay: TargetChangesOfDay): Boolean {
    val serializer = ObjectMapper()
    val serializedValue = serializer.writerWithDefaultPrettyPrinter().writeValueAsString(targetChangesOfDay)
    return try {
        val stream = FileWriter(getTargetFilePath(targetChangesOfDay.isAbsolute).toFile(), false)
        stream.write(serializedValue)
        stream.close()

        true
    }
    catch (e: Exception) {
        println("\n\nOn writing error occurred: ${e.message}.")
        false
    }
}

private fun writeToUnitedFile(targetChangesOfDayList: List<TargetChangesOfDay?>): Boolean {
    val serializer = ObjectMapper()
    val serializedValue = serializer.writerWithDefaultPrettyPrinter().writeValueAsString(targetChangesOfDayList)
    return try {
        val stream = FileWriter(getUnitedFilePath(targetChangesOfDayList.size).toFile(), false)
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
 * Returns the file [path][Path] for writing targetChangesOfDay.
 * It contains [absolute value][absolute].
 */
private fun getTargetFilePath(absolute: Boolean) = Paths.get(System.getProperty("user.dir"), "src", "main", "resources",
                                                             String.format(TARGET_FILE_NAME_TEMPLATE, absolute))

private fun getUnitedFilePath(count: Int) = Paths.get(System.getProperty("user.dir"), "src", "main", "resources",
                                                      String.format(UNITED_FILE_NAME_TEMPLATE, count))
/* endregion */
