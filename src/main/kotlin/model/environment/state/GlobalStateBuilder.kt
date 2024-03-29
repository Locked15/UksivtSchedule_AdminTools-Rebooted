package model.environment.state

import model.environment.log.LogLevel


class GlobalStateBuilder {

    /* region Properties */

    private var projectDirectory: String? = null

    private var resourceProjectPath: List<String>? = null

    private var dbNameParam: String? = null

    private var dbTypeParam: String? = null

    private var configFileName: String? = null

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
        setDBNameParam(with(args) {
            when (val dbNameIndex = indexOf("--db-name")) {
                -1 -> DB_DEFAULT_NAME
                else -> args.getOrElse(dbNameIndex + 1) { DB_DEFAULT_NAME }
            }
        })
        setDBTypeParam(with(args) {
            when (val dbTypeIndex = indexOf("--db-type")) {
                -1 -> ""
                else -> args.getOrElse(dbTypeIndex + 1) { "" }
            }
        })
        setConfigFileName(with(args) {
            when (val configFileNameArgIndex = indexOf("--config")) {
                -1 -> "App"
                else -> args.getOrElse(configFileNameArgIndex + 1) { "App" }
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

    private fun setDBNameParam(name: String): GlobalStateBuilder {
        dbNameParam = name
        return this
    }

    private fun setDBTypeParam(param: String): GlobalStateBuilder {
        //? To make it case-insensitive (and because config values write with a first letter in upper-case), we make it.
        dbTypeParam = if (param.isNotEmpty()) extractDBParameter(param.trim('\'', '"'))
        else ""

        return this
    }

    private fun setConfigFileName(name: String): GlobalStateBuilder {
        configFileName = name
        return this
    }

    /**
     * Extracts normalized DB Configuration prefix from one, that sent by argument.
     * For example,
     * "--db-type local" will be converted to ".Local" prefix (and "Local" value).
     */
    private fun extractDBParameter(param: String) = ".${param.substring(0, 1).uppercase()}" +
            param.substring(1)
    /* endregion */

    fun build() = GlobalState(projectDirectory!!, resourceProjectPath!!, dbNameParam!!,
                              dbTypeParam!!, logLevel!!, configFileName!!)

    companion object {

        private const val DB_DEFAULT_NAME = "UksivtSchedule"

        private val developmentResourcePath = listOf("src", "main", "resources")

        private val productionResourcePath = listOf("")
    }
}
