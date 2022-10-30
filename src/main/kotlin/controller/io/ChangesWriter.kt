package controller.io

import com.fasterxml.jackson.databind.ObjectMapper
import model.data.schedule.Changes
import java.io.FileWriter
import java.nio.file.Path
import java.nio.file.Paths


/* region Properties */

/**
 * Contains file name template for newly creating changes file.
 */
private const val FILE_NAME_TEMPLATE = "%b.json"
/* endregion */

/* region Functions */

/**
 * Writes [changes][Changes] object to the file.
 *
 * Returns a [result][Boolean] of the process.
 */
fun writeChanges(changes: Changes) = writeToFile(changes)

/**
 * Writes [value][changes] to the file.
 * File will be placed inside the "resources" directory.
 */
private fun writeToFile(changes: Changes): Boolean {
    val serializer = ObjectMapper()
    val serializedValue = serializer.writerWithDefaultPrettyPrinter().writeValueAsString(changes)
    return try {
        val stream = FileWriter(getFilePath(changes.absolute).toFile(), false)
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
private fun getFilePath(absolute: Boolean) = Paths.get(System.getProperty("user.dir"), "src", "main", "resources",
                                                       String.format(FILE_NAME_TEMPLATE, absolute))
/* endregion */
