package controller.io.service

import config
import globalState
import java.nio.file.Path
import java.nio.file.Paths


class PathResolver {

    companion object {

        /* region Top-Level Paths. */

        val generalApplicationFolderName = getPathFromConfigByParameter("App.Folders.Resources.Application.Storage")

        val generalUserSecretsFolderName = getPathFromConfigByParameter("App.Folders.Resources.Application.Secrets")

        val applicationResourcePath: Path
        /* endregion */

        /* region Sub-Level Paths. */

        val secretConnectionsDataFolderName = getPathFromConfigByParameter("App.Folders.Resources.Application.Secrets.Connections")

        /**
         * Contains three values:
         * 1. Path to current semester resources (e.g.: "./resources/Schedule/Y23/S2";
         * 2. Path to current changes resources (e.g.: "./resources/Schedule/Y23/S2/.Storage/Changes");
         * 3. Path to current final schedules resources (e.g.: "./resources/Schedule/Y23/S2/.Storage/FinalSchedules").
         */
        val currentSemesterResourcePaths: Triple<Path, Path, Path>
        /* endregion */

        /* region Private Paths. */

        private val applicationResourcesFoldersList =
            listOf(getPathFromConfigByParameter("App.Folders.Resources.Application"))

        private val currentScheduleCycleSubFoldersList =
            listOf(getPathFromConfigByParameter("App.Folders.Resources.Application.Storage.Schedule"),
                   "Y${getPathFromConfigByParameter("App.Settings.TargetYear")}",
                   "S${getPathFromConfigByParameter("App.Settings.TargetSemester")}")

        private val changesSubSubFoldersList = listOf(generalApplicationFolderName,
                                                      getPathFromConfigByParameter(
                                                              "App.Folders.Resources.Application.Storage.Changes"))

        private val finalSchedulesSubSubFoldersList = listOf(generalApplicationFolderName,
                                                             getPathFromConfigByParameter(
                                                                     "App.Folders.Resources.Application.Storage.Final"))
        /* endregion */

        init {
            applicationResourcePath =
                resolvePath(Paths.get(globalState.projectDirectory), globalState.resourceProjectPath,
                            applicationResourcesFoldersList)

            val thisSemesterResourcePath =
                resolvePath(Paths.get(globalState.projectDirectory), globalState.resourceProjectPath,
                            currentScheduleCycleSubFoldersList)
            val changesResourceFolderPath = resolvePath(thisSemesterResourcePath,
                                                        changesSubSubFoldersList)
            val finalSchedulesResourceFolderPath = resolvePath(thisSemesterResourcePath,
                                                               finalSchedulesSubSubFoldersList)

            currentSemesterResourcePaths = Triple(thisSemesterResourcePath, changesResourceFolderPath,
                                                  finalSchedulesResourceFolderPath)
        }

        fun resolvePath(basicPath: Path, vararg additionalPaths: List<String>): Path {
            var result = basicPath
            for (path in additionalPaths) {
                path.forEach { element -> result = result.resolve(element) }
            }

            return result
        }

        private fun getPathFromConfigByParameter(parameter: String) = config.getProperty(parameter) ?: ""
    }
}
