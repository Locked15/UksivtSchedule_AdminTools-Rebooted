package controller.io

import com.fasterxml.jackson.databind.ObjectMapper
import model.data.schedule.Changes
import java.io.FileWriter


/* region Properties */

private const val FILE_NAME_TEMPLATE = "%b.json"
/* endregion */

/* region Functions */

fun writeChanges(changes: Changes): Boolean {
    return try {
        writeToFile(changes)
    }
    catch (e: Exception) {
        println("\n\nOn writing error occurred: ${e.message}.")
        false
    }
}

private fun writeToFile(changes: Changes): Boolean {
    val serializer = ObjectMapper()
    val serializedValue = serializer.writerWithDefaultPrettyPrinter().writeValueAsString(changes)
    return try {
        val stream = FileWriter("D:\\Java-Projects\\Schedule-AdminTools\\src\\main\\resources\\${
            String.format(FILE_NAME_TEMPLATE, changes.absolute)
        }")
        stream.write(serializedValue)
        stream.close()

        true
    }
    catch (e: Exception) {
        false
    }
}
/* endregion */
