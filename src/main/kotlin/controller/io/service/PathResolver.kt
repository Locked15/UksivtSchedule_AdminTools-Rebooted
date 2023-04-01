package controller.io.service

import globalState
import java.nio.file.Path
import java.nio.file.Paths


class PathResolver {

    companion object {

        const val STORAGE_FOLDER_NAME = ".Storage"

        const val USER_SECRETS_FOLDER_NAME = "secrets"

        val applicationResourcePath: Path

        val thisSemesterResourcePath: Path

        val changesResourceFolderPath: Path

        val finalSchedulesResourceFolderPath: Path

        private val applicationSubFoldersList = listOf("Application")

        private val subFoldersList = listOf("Schedule", "Y23", "S2")

        private val changesSubFoldersList = listOf(STORAGE_FOLDER_NAME, "Changes")

        private val finalSchedulesFoldersList = listOf(STORAGE_FOLDER_NAME, "FinalSchedules")

        init {
            applicationResourcePath = resolvePath(Paths.get(globalState.projectDirectory), globalState.resourceProjectPath,
                                                  applicationSubFoldersList)
            thisSemesterResourcePath = resolvePath(Paths.get(globalState.projectDirectory), globalState.resourceProjectPath,
                                                   subFoldersList)

            changesResourceFolderPath = resolvePath(thisSemesterResourcePath,
                                                    changesSubFoldersList)
            finalSchedulesResourceFolderPath = resolvePath(thisSemesterResourcePath,
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
