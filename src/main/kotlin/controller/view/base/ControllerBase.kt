package controller.view.base

import controller.view.console.ActionsController
import model.data.schedule.base.day.Day
import model.exception.WrongDayInDocumentException


abstract class ControllerBase {

    companion object {

        /**
         * Asks user to input [file path][String] to target file.
         * Checks a file extension to be presented inside a [supported ones list][extensions].
         *
         * If you don't want to check value, just don't send anything to this function.
         */
        @JvmStatic
        protected fun getSafeFilePath(vararg extensions: String): String {
            val normalizedExtensions = extensions.map { extension -> extension.trimStart('.', ' ') }.toTypedArray()
            var path: String
            do {
                path = inputText("Input file path (${extensions.joinToString(", ")})").trim('"', ' ')
            } while (!normalizedExtensions.any { ext -> path.endsWith(".$ext") })

            return path
        }

        /**
         * Asks user to input [number][Int] with given [message][ask].
         * If user inputs incorrect value, function will continue to ask the user for input value,
         * until correct one will be inputted.
         *
         * Checks inputted value to be more than [minimal][min] and less than [maximum][max] values.
         * *Value checks with including, so if you sent '10' and user inputs '10' value will be marked as **correct**.*
         */
        @JvmStatic
        protected fun getSafeIntValue(ask: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): Int {
            var input: String
            do {
                input = inputText(ask)
            } while (input.toIntOrNull() == null || input.toInt() < min || input.toInt() > max)

            return input.toInt()
        }

        /**
         * Asks user to input [day index][Int] for day-check on changes [parsing process][ActionsController.parseChanges].
         * Then, a result depends on user input:
         * * If user input correct value, the program will check day-corresponding and
         *   throws [exception][WrongDayInDocumentException] if days aren't equal.
         * * If user input '-1', the program will ignore the day-corresponding check.
         */
        @JvmStatic
        protected fun getSafeTargetDay(): Day? {
            val index = getSafeIntValue("Input target day index (0..6) to insert day-check " +
                                                "OR '-1' to ignore it", -1, 6)

            return if (index == -1) null else Day.getValueByIndex(index)
        }

        /**
         * Asks user to input [group name][String].
         * Checks inputted group name to be presented inside an [available groups list][availableOnes].
         *
         * If user sent [message] console will show itself instead basic one ('Select target value').
         * Sent value must don't contain ending (program will finish it automatically).
         *
         * If you don't want to use check, just send an empty list ('[listOf]').
         */
        @JvmStatic
        protected fun inputValueSafely(availableOnes: List<String?>, message: String = "Select target value"): String {
            var target: String
            do {
                target = inputText(message)
            } while (checkFinalCondition(target, availableOnes))

            return getTargetValueOrInputtedOne(target, availableOnes)
        }

        /**
         * Asks user to [input][readln] [text][String].
         * Sent [text][ask] uses as tooltip for input.
         *
         * The program uses string templates for tooltip, so you don't need to write ':' an inside text.
         * Just write a query.
         */
        @JvmStatic
        protected fun inputText(ask: String): String {
            print("$ask: ")
            return readln()
        }

        @JvmStatic
        protected fun getFollowingArgumentByTarget(targetName: String, args: List<String>,
                                                   shortTargetName: String = ""): String? {
            val targetValueIndex = if (args.indexOf(targetName) != -1) args.indexOf(targetName)
            else args.indexOf(shortTargetName)

            //? The most simple way to make it: just call the next by target argument.
            return try {
                args[targetValueIndex + 1]
            }
            //? And catch exception, if something went wrong (include out-of-bounds exception).
            catch (ex: Exception) {
                null
            }
        }

        /**
         * Checks 'do...while' condition to [inputValueSafely] function.
         */
        private fun checkFinalCondition(inputted: String, available: List<String?>) = available.isNotEmpty() &&
                available.find { availableOne ->
                    availableOne?.equals(inputted, true) == true
                } == null

        /**
         * Returns the [available value][String], as it presented in [available list][availableOnes] OR
         * if list it empty, returns base user [inputted value][inputtedValue].
         */
        private fun getTargetValueOrInputtedOne(inputtedValue: String,
                                                availableOnes: List<String?>) = availableOnes.find { available ->
            available?.equals(inputtedValue, true) == true
        } ?: inputtedValue
    }
}
