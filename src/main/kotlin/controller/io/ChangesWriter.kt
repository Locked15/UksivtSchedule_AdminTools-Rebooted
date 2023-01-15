package controller.io

import com.fasterxml.jackson.databind.ObjectMapper
import model.data.schedule.Changes
import model.data.schedule.ChangesList
import java.io.FileWriter
import java.nio.file.Path
import java.nio.file.Paths


/* region Properties */

/**
 * Contains file name template for newly creating changes file.
 */
private const val TARGET_FILE_NAME_TEMPLATE = "%b.json"

/**
 * Contains file name template for newly creating changes file.
 * Supposed to be used with united file ([ChangesList] object).
 */
private const val UNITED_FILE_NAME_TEMPLATE = "%d.json"
/* endregion */

/* region Functions */

/**
 * Writes [changes][Changes] object to the file.
 *
 * Returns a [result][Boolean] of the process.
 */
fun writeChanges(changes: Changes) = writeToTargetFile(changes)

fun writeChanges(changesList: ChangesList) = writeToUnitedFile(changesList.changes)

/**
 * Writes [value][changes] to the file.
 * File will be placed inside the "resources" directory.
 */
private fun writeToTargetFile(changes: Changes): Boolean {
    val serializer = ObjectMapper()
    val serializedValue = serializer.writerWithDefaultPrettyPrinter().writeValueAsString(changes)
    return try {
        val stream = FileWriter(getTargetFilePath(changes.isAbsolute).toFile(), false)
        stream.write(serializedValue)
        stream.close()

        true
    }
    catch (e: Exception) {
        println("\n\nOn writing error occurred: ${e.message}.")
        false
    }
}

private fun writeToUnitedFile(changesList: List<Changes?>): Boolean {
    val serializer = ObjectMapper()
    val serializedValue = serializer.writerWithDefaultPrettyPrinter().writeValueAsString(changesList)
    return try {
        val stream = FileWriter(getUnitedFilePath(changesList.size).toFile(), false)
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
 * Returns the file [path][Path] for writing changes.
 * It contains [absolute value][absolute].
 */
private fun getTargetFilePath(absolute: Boolean) = Paths.get(System.getProperty("user.dir"), "src", "main", "resources",
                                                             String.format(TARGET_FILE_NAME_TEMPLATE, absolute))

private fun getUnitedFilePath(count: Int) = Paths.get(System.getProperty("user.dir"), "src", "main", "resources",
                                                      String.format(UNITED_FILE_NAME_TEMPLATE, count))
/* endregion */
