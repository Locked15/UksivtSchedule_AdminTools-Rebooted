package model.environment.state

import model.environment.log.LogLevel


class GlobalStateBuilder {

    /* region Properties */

    private var projectDirectory: String? = null

    private var resourceProjectPath: List<String>? = null

    private var logLevel: LogLevel? = null
    /* endregion */

    /* region Setter Functions */

    fun setProjectDirectory(directory: String): GlobalStateBuilder {
        projectDirectory = directory
        return this
    }

    fun setByArgs(args: Array<String>): GlobalStateBuilder {
        setLogLevel(with(args) {
            when {
                contains("--trace-all") || contains("-v") || contains("--verbose") -> LogLevel.INFORMATION
                contains("--trace-debug") -> LogLevel.DEBUG
                contains("--trace-warning") || contains("--trace-warn") -> LogLevel.WARNING
                contains("--trace-error") -> LogLevel.ERROR
                contains("--trace-critical") -> LogLevel.CRITICAL
                contains("--trace-no") || contains("-s") || contains("--silent") -> LogLevel.SILENT

                else -> LogLevel.WARNING
            }
        })
        setResourcePath(with(args) {
            when {
                contains("-d") || contains("--dev") -> developmentResourcePath

                else -> productionResourcePath
            }
        })

        return this
    }

    private fun setResourcePath(path: List<String>): GlobalStateBuilder {
        resourceProjectPath = path
        return this
    }

    private fun setLogLevel(level: LogLevel): GlobalStateBuilder {
        logLevel = level
        return this
    }
    /* endregion */

    fun build() = GlobalState(projectDirectory!!, resourceProjectPath!!,
                              logLevel!!)

    companion object {

        private val developmentResourcePath = listOf("src", "main", "resources")

        private val productionResourcePath = listOf("")
    }
}
