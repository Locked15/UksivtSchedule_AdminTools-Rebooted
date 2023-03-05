package controller.io

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import model.data.schedule.origin.GeneralWeekSchedule
import model.data.schedule.origin.TargetedWeekSchedule
import projectDirectory
import resourcePathElements
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension


class AssetsManager {

    companion object {

        /* region Properties */

        private val finalResourcePath: Path

        private const val writerBufferSize = 4096

        private val subFoldersList = listOf("Y22", "S2")
        /* endregion */

        /* region Initializers */

        init {
            finalResourcePath = resolveFinalPath()
        }

        private fun resolveFinalPath(): Path {
            var path = Paths.get(projectDirectory)
            resourcePathElements.forEach { element -> path = path.resolve(element) }
            subFoldersList.forEach { element -> path = path.resolve(element) }

            return path
        }
        /* endregion */

        /* region Functions */

        /* region Assets Access */

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

        fun writeScheduleUnitedAssetFile(fileName: String, schedules: GeneralWeekSchedule): Boolean {
            val finalPath = Paths.get(finalResourcePath.toString(), "$fileName.json")
            return try {
                val serializer = jacksonObjectMapper()
                val writer = FileWriter(File(finalPath.toUri()))
                val buffered = BufferedWriter(writer, writerBufferSize)

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

        /* region Clear Names Getters */

        fun getBranchesNames() = Files.walk(finalResourcePath, 1)
            .filter(Files::isDirectory)
            .collect(Collectors.toList())
            .map { path -> path.name }
            .drop(1)

        fun getAffiliationsNames(branch: String) = Files.walk(resolveBranchPath(branch), 1)
            .filter(Files::isDirectory)
            .collect(Collectors.toList())
            .map { path -> path.name }
            .drop(1)

        fun getGroupsNames(branch: String, affiliation: String) =
            Files.walk(resolveAffiliationPath(branch, affiliation), 1)
                .collect(Collectors.toList())
                .map { path -> path.nameWithoutExtension }
                .drop(1)
        /* endregion */

        /* region Full Path Getters */

        fun getBranchesPaths() = Files.walk(finalResourcePath, 1)
            .filter(Files::isDirectory)
            .collect(Collectors.toList())
            .map { path -> path.toUri().path }
            .drop(1)

        fun getAffiliationsPaths(branch: String) = Files.walk(resolveBranchPath(branch), 1)
            .filter(Files::isDirectory)
            .collect(Collectors.toList())
            .map { path -> path.toUri().path }
            .drop(1)

        fun getGroupsPaths(branch: String, affiliation: String) =
            Files.walk(resolveAffiliationPath(branch, affiliation), 1)
                .collect(Collectors.toList())
                .map { path -> path.toUri().path }
                .drop(1)
        /* endregion */

        /* region Path Resolvers */

        private fun resolveBranchPath(branch: String): Path = finalResourcePath.resolve(branch)

        private fun resolveAffiliationPath(branch: String, affiliation: String): Path =
            finalResourcePath.resolve(branch).resolve(affiliation)
        /* endregion */
        /* endregion */
    }
}
