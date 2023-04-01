package controller.io.service

import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension


/* region Clear Names Getters */

fun getBranchesNames() = Files.walk(PathResolver.thisSemesterResourcePath, 1)
    .filter { it.isDirectory() && !it.name.equals(PathResolver.STORAGE_FOLDER_NAME, true) }
    .collect(Collectors.toList())
    .map { path -> path.name }
    .drop(1)

fun getAffiliationNames(branch: String) = Files.walk(PathResolver.resolvePath(PathResolver.thisSemesterResourcePath,
                                                                              listOf(branch)), 1)
    .filter(Files::isDirectory)
    .collect(Collectors.toList())
    .map { path -> path.name }
    .drop(1)

fun getGroupsNames(branch: String, affiliation: String) = Files.walk(PathResolver.resolvePath(
        PathResolver.thisSemesterResourcePath, listOf(branch, affiliation)), 1)
    .collect(Collectors.toList())
    .map { path -> path.nameWithoutExtension }
    .drop(1)
/* endregion */

/* region Full Path Getters */

fun getBranchesPaths() = Files.walk(PathResolver.thisSemesterResourcePath, 1)
    .filter(Files::isDirectory)
    .collect(Collectors.toList())
    .map { path -> path.toUri().path }
    .drop(1)

fun getAffiliationPaths(branch: String) = Files.walk(PathResolver.resolvePath(PathResolver.thisSemesterResourcePath,
                                                                              listOf(branch)), 1)
    .filter(Files::isDirectory)
    .collect(Collectors.toList())
    .map { path -> path.toUri().path }
    .drop(1)

fun getGroupsPaths(branch: String, affiliation: String) = Files.walk(PathResolver.resolvePath(
        PathResolver.thisSemesterResourcePath, listOf(branch, affiliation)), 1)
    .collect(Collectors.toList())
    .map { path -> path.toUri().path }
    .drop(1)
/* endregion */

/* region Storage Sub-Folders Getters */

fun getChangesStorageMonthFolderNames() = Files.walk(PathResolver.changesResourceFolderPath, 1)
    .filter(Files::isDirectory)
    .collect(Collectors.toList())
    .map { path -> path.name }
    .drop(1)

fun getFinalSchedulesStorageMonthFolderNames() = Files.walk(PathResolver.finalSchedulesResourceFolderPath, 1)
    .filter(Files::isDirectory)
    .collect(Collectors.toList())
    .map { path -> path.name }
    .drop(1)
/* endregion */

/* region Files in Storage Sub-Folders Getters */

fun getChangesStorageFileNames(monthFolder: String) = Files.walk(PathResolver.resolvePath(
        PathResolver.changesResourceFolderPath, listOf(monthFolder)), 1)
    .collect(Collectors.toList())
    .map { path -> path.nameWithoutExtension }
    .drop(1)

fun getFinalSchedulesStorageFileNames(monthFolder: String) = Files.walk(PathResolver.resolvePath(
        PathResolver.finalSchedulesResourceFolderPath, listOf(monthFolder)), 1)
    .collect(Collectors.toList())
    .map { path -> path.nameWithoutExtension }
    .drop(1)
/* endregion */
