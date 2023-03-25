package controller.io.service

import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension


/* region Clear Names Getters */

fun getBranchesNames() = Files.walk(PathResolver.finalResourcePath, 1)
    .filter { it.isDirectory() && !it.name.equals(PathResolver.StorageFolderName, true) }
    .collect(Collectors.toList())
    .map { path -> path.name }
    .drop(1)

fun getAffiliationsNames(branch: String) = Files.walk(PathResolver.resolvePath(PathResolver.finalResourcePath,
                                                                               listOf(branch)), 1)
    .filter(Files::isDirectory)
    .collect(Collectors.toList())
    .map { path -> path.name }
    .drop(1)

fun getGroupsNames(branch: String, affiliation: String) = Files.walk(PathResolver.resolvePath(
        PathResolver.finalResourcePath, listOf(branch, affiliation)), 1)
    .collect(Collectors.toList())
    .map { path -> path.nameWithoutExtension }
    .drop(1)
/* endregion */

/* region Full Path Getters */

fun getBranchesPaths() = Files.walk(PathResolver.finalResourcePath, 1)
    .filter(Files::isDirectory)
    .collect(Collectors.toList())
    .map { path -> path.toUri().path }
    .drop(1)

fun getAffiliationsPaths(branch: String) = Files.walk(PathResolver.resolvePath(PathResolver.finalResourcePath,
                                                                               listOf(branch)), 1)
    .filter(Files::isDirectory)
    .collect(Collectors.toList())
    .map { path -> path.toUri().path }
    .drop(1)

fun getGroupsPaths(branch: String, affiliation: String) = Files.walk(PathResolver.resolvePath(
        PathResolver.finalResourcePath, listOf(branch, affiliation)), 1)
    .collect(Collectors.toList())
    .map { path -> path.toUri().path }
    .drop(1)
/* endregion */
