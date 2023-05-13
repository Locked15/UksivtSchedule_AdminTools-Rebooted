package controller.view

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import globalState
import model.environment.log.LogLevel


class Logger {

    companion object {

        /* region Constants */

        private const val INFORMATION_TITLE = "INFO"

        private const val DEBUG_TITLE = "DEBUG"

        private const val WARNING_TITLE = "WARNING"

        private const val ERROR_TITLE = "ERROR"

        private const val CRITICAL_TITLE = "CRITICAL"

        /**
         * This one contains tabulation value.
         * Mordant depends on the system tabulation and this may cause some troubles, so I specified it explicitly.
         *
         * If someone doesn't know, on Windows OS default tab is equal to six spaces.
         * On Linux it's equal to four spaces.
         */
        private const val TABULATION_VALUE = "    "
        /* endregion */

        /* region Properties */

        /**
         * Terminal that allows to write and render text in a console with additional possibilities.
         * For example, colors or text styling.
         */
        private val terminal: Terminal = Terminal(AnsiLevel.TRUECOLOR)

        /**
         * This is a template for all logging messages.
         *
         * It contains two parts:
         * * First one is title, and it lies on zero-indent level;
         * * Second one is message body, and it lies on first-indent level and on the next line.
         */
        private val messageTemplate = """
            %s:
                %s.
        """.trimIndent()

        /**
         * Contains all messages, that was sent to logger during the current session.
         */
        private val sessionMessagesHistory = mutableListOf<String>()
        /* endregion */

        /* region Public Functions */

        fun logException(ex: Exception, indentLevel: Int = 1) = logMessage(LogLevel.ERROR, ex.message, indentLevel)

        fun logException(ex: Exception, indentLevel: Int = 1, message: String) = logMessage(LogLevel.ERROR,
                                                                                            "$message (${ex.message}).",
                                                                                            indentLevel)

        fun logMessage(logLevel: LogLevel, message: String?, indent: Int = 1) {
            val info = message ?: "Something happened in the application (severity: ${logLevel.name})."
            when (logLevel) {
                LogLevel.INFORMATION -> if (LogLevel.INFORMATION >= globalState.logLevel) logInformation(info, indent)
                LogLevel.DEBUG -> if (LogLevel.DEBUG >= globalState.logLevel) logDebug(info, indent)
                LogLevel.WARNING -> if (LogLevel.WARNING >= globalState.logLevel) logWarning(info, indent)
                LogLevel.ERROR -> if (LogLevel.ERROR >= globalState.logLevel) logError(info, indent)
                LogLevel.CRITICAL -> if (LogLevel.CRITICAL >= globalState.logLevel) logCritical(info, indent)

                //? For possible next updates (if new log levels will be added).
                else -> if (globalState.logLevel != LogLevel.SILENT) logInformation(info, indent)
            }

            sessionMessagesHistory.add(info)
        }
        /* endregion */

        /* region Private Functions */

        private fun getIndentedMessageTemplate(indentLevel: Int) = messageTemplate
            .prependIndent(TABULATION_VALUE.repeat(indentLevel))

        private fun logInformation(message: String, indentLevel: Int = 0) {
            with(getIndentedMessageTemplate(indentLevel).format(INFORMATION_TITLE, message)) {
                //? "Muted" will show white text (as common console output).
                terminal.muted(this)
            }
        }

        private fun logDebug(message: String, indentLevel: Int = 0) {
            with(getIndentedMessageTemplate(indentLevel).format(DEBUG_TITLE, message)) {
                //? "Info" will show bright blue text.
                terminal.info(this)
            }
        }

        private fun logWarning(message: String, indentLevel: Int = 0) {
            with(getIndentedMessageTemplate(indentLevel).format(WARNING_TITLE, message)) {
                //? "Warning" will show orange text (as a common warning text).
                terminal.warning(this)
            }
        }

        private fun logError(message: String, indentLevel: Int = 0) {
            with(getIndentedMessageTemplate(indentLevel).format(ERROR_TITLE, message)) {
                //? "Danger" will show red text.
                terminal.danger(this)
            }
        }

        private fun logCritical(message: String, indentLevel: Int = 0) {
            with(getIndentedMessageTemplate(indentLevel).format(CRITICAL_TITLE, message)) {
                terminal.println(TextColors.brightRed(this))
            }
        }
        /* endregion */
    }
}
