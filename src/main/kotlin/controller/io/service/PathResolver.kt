package controller.io.service

import globalState
import java.nio.file.Path
import java.nio.file.Paths


class PathResolver {

    companion object {

        const val StorageFolderName = ".Storage"

        val finalResourcePath: Path

        val changesResourceFolderPath: Path

        val finalSchedulesResourceFolderPath: Path

        private val subFoldersList = listOf("Schedule", "Y23", "S2")

        private val changesSubFoldersList = listOf(StorageFolderName, "Changes")

        private val finalSchedulesFoldersList = listOf(StorageFolderName, "FinalSchedules")

        init {
            finalResourcePath = resolvePath(Paths.get(globalState.projectDirectory), globalState.resourceProjectPath,
                                            subFoldersList)

            changesResourceFolderPath = resolvePath(finalResourcePath,
                                                    changesSubFoldersList)
            finalSchedulesResourceFolderPath = resolvePath(finalResourcePath,
                                                           finalSchedulesFoldersList)
        }

        fun resolvePath(basicPath: Path, vararg additionalPaths: List<String>): Path {
            var result = basicPath
            for (path in additionalPaths) {
                path.forEach { element -> result = result.resolve(element) }
            }

            return result
        }
    }
}
