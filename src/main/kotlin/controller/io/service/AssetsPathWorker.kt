package controller.io.service

import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.*


/* region Clear Names Getters */

fun getBranchesNames() = Files.walk(PathResolver.currentSemesterResourcePaths.first, 1)
    .filter { it.isDirectory() && !it.name.equals(PathResolver.generalApplicationFolderName, true) }
    .collect(Collectors.toList())
    .map { path -> path.name }
    .drop(1)

fun getAffiliationNames(branch: String) = Files.walk(PathResolver.resolvePath(PathResolver.currentSemesterResourcePaths.first,
                                                                              listOf(branch)), 1)
    .filter(Files::isDirectory)
    .collect(Collectors.toList())
    .map { path -> path.name }
    .drop(1)

fun getGroupsNames(branch: String, affiliation: String) = Files.walk(PathResolver.resolvePath(
        PathResolver.currentSemesterResourcePaths.first, listOf(branch, affiliation)), 1)
    .collect(Collectors.toList())
    .map { path -> path.nameWithoutExtension }
    .drop(1)
/* endregion */

/* region Full Path Getters */

fun getBranchesPaths() = Files.walk(PathResolver.currentSemesterResourcePaths.first, 1)
    .filter(Files::isDirectory)
    .collect(Collectors.toList())
    .map { path -> path.toUri().path }
    .drop(1)

fun getAffiliationPaths(branch: String) = Files.walk(PathResolver.resolvePath(PathResolver.currentSemesterResourcePaths.first,
                                                                              listOf(branch)), 1)
    .filter(Files::isDirectory)
    .collect(Collectors.toList())
    .map { path -> path.toUri().path }
    .drop(1)

fun getGroupsPaths(branch: String, affiliation: String) = Files.walk(PathResolver.resolvePath(
        PathResolver.currentSemesterResourcePaths.first, listOf(branch, affiliation)), 1)
    .collect(Collectors.toList())
    .map { path -> path.toUri().path }
    .drop(1)
/* endregion */

/* region Storage Sub-Folders Getters */

fun getChangesStorageMonthFolderNames() = Files.walk(PathResolver.currentSemesterResourcePaths.second, 1)
    .filter { it.isDirectory() }
    .collect(Collectors.toList())
    .map { path -> path.name }
    .drop(1)

fun getFinalSchedulesStorageMonthFolderNames() = Files.walk(PathResolver.currentSemesterResourcePaths.third, 1)
    .filter { it.isDirectory() }
    .collect(Collectors.toList())
    .map { path -> path.name }
    .drop(1)
/* endregion */

/* region Files in Month-Level Directory (Non-Normalized) */

fun getChangesStorageMonthLevelAssetFiles() = Files.walk(PathResolver.currentSemesterResourcePaths.second, 1)
    .filter { !it.isDirectory() && it.extension.equals("json", true) }
    .collect(Collectors.toList())
    .map { path -> path.nameWithoutExtension }

fun getFinalScheduleStorageMonthLevelAssetFiles() = Files.walk(PathResolver.currentSemesterResourcePaths.third, 1)
    .filter { !it.isDirectory() && it.extension.equals("json", true) }
    .collect(Collectors.toList())
    .map { path -> path.nameWithoutExtension }
/* endregion */

/* region Files in Storage Sub-Folders Getters */

fun getChangesStorageFileNames(monthFolder: String) = Files.walk(PathResolver.resolvePath(
        PathResolver.currentSemesterResourcePaths.second, listOf(monthFolder)), 1)
    .filter { it.extension.equals("json", true) }
    .collect(Collectors.toList())
    .map { path -> path.nameWithoutExtension }

fun getFinalSchedulesStorageFileNames(monthFolder: String) = Files.walk(PathResolver.resolvePath(
        PathResolver.currentSemesterResourcePaths.third, listOf(monthFolder)), 1)
    .filter { it.extension.equals("json", true) }
    .collect(Collectors.toList())
    .map { path -> path.nameWithoutExtension }
/* endregion */
