package view

import controller.view.ConsoleController
import model.view.Command
import model.view.CommandInfo
import java.util.*


/**
 * Console view element.
 * Contains logic of the user-system interactions.
 */
class Console(private val user: String) {

    /* region Properties */

    /**
     * Contains a controller object with realizations of the interaction functions.
     *
     * When user performs any accessible action, view sends a message to the controller,
     *      and the controller executes corresponding actions (commands).
     * Then, controller may return something (like the success of execution).
     *
     * View Action -> Controller Command -> View Result.
     */
    private val controller = ConsoleController()

    /**
     * [Dictionary] with key-value pairs.
     * Second values contains [Pair] objects.
     * [First of them][Pair.first] contains description of the function.
     * [Second one][Pair.second] contains [Runnable] objects, that contains execution logic for this action.
     *
     * Please, get the values of this dictionary with ignoring register.
     * Use [Map.get] or [String.lowercase] to prevent register dependant.
     *
     * If you want to create new action, you must remember:
     * KEYS CAN'T REPEAT.
     * Templates can create anything else.
     */
    private val availableActions = mapOf(
            // Valuable Commands:
            "schedule" to Pair(scheduleDescription,
                               Command("Schedule") {
                                   controller.parseSchedule(it)
                               }),
            "changes" to Pair(changesDescription,
                              Command("Changes") {
                                  controller.parseChanges(it)
                              }
            ),
            // Functional Commands:
            "help" to Pair(helpDescription,
                           Command("Help") {
                               controller.showHelp()
                           }),
            "parse" to Pair(parseDescription,
                            Command("Parse") {
                                controller.initializeBasicParsingProcessByArguments(it)
                            }),
            "write" to Pair(writeDescription,
                            Command("Write") {
                                controller.writeLastResult()
                            }),
            "show" to Pair(showDescription,
                           Command("Show") {
                               controller.showLastResult()
                           }
            )
    )
    /* endregion */

    /* region Functions */

    /**
     * [Greets][greetUser] a [user] and begins new session of the work.
     *
     * Call this function to begin work session with AdminTools.
     * This is the entry point (after constructor, of course).
     */
    fun beginSession() {
        greetUser(user, Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        do {
            print("So, what you want to do now?\nEnter command code: ")
            val action = readlnOrNull()

            performUserInput(action ?: "help")
        } while (!action.isNullOrBlank())
    }

    /**
     * Performs any user input.
     * It parses inputted text and seeks corresponding command.
     *
     * If the corresponding command is found, it [confirms execution][confirmCommandExecution] and
     *    then executes the command.
     * Although, it [returns to input of the command name][beginSession].
     */
    private fun performUserInput(input: String) {
        val commandInfo = parseInputtedText(this, input)
        if (commandInfo.action != null) {
            if (confirmCommandExecution(commandInfo.action.first)) {
                executeCommand(commandInfo.args, commandInfo.action.second)
            }
        }
        else {
            println("Inputted command isn't supported, please enter 'help' to get list of supported ones.\n")
        }
    }

    /**
     * Confirms [Command] [execution][executeCommand].
     * It shows command description and asks user to input [confirmation][String].
     *
     * If a user confirms execution, it [returns][Boolean] 'true', another 'false'.
     */
    private fun confirmCommandExecution(desc: String): Boolean {
        print("Selected command: $desc. \nAre you sure (Y/N)? ")
        val confirmation = readlnOrNull()

        return !confirmation.isNullOrBlank() && confirmation.equals("y", true)
    }

    /**
     * Executes [command] with given [arguments][args].
     * Also, it marks command output in the terminal (console) output.
     */
    private fun executeCommand(args: List<String>, command: Command) {
        println("\n\t\tCommand ('${command.name}') Output:")
        command.setUpArgs(args).execute()

        println("\t\tExecution complete.\n")
    }
    /* endregion */

    /* region Companion */

    companion object {

        /* region Properties */

        /**
         * Description of the 'Help' command.
         *
         * This is a functional one.
         */
        private val helpDescription: String

        /**
         * Description of the 'Schedule' command.
         *
         * This is a valuable one.
         */
        private val scheduleDescription: String

        /**
         * Description of the 'Changes' command.
         *
         * This is a valuable one.
         */
        private val changesDescription: String

        /**
         * Description of the 'Parse' command.
         */
        private val parseDescription: String

        /**
         * Description of the 'Write' command.
         */
        private val writeDescription: String

        /**
         * Description of the 'show' command.
         */
        private val showDescription: String
        /* endregion */

        /* region Initializers */

        /**
         * Initializes all static properties by current system language.
         * Some languages aren't supported yet.
         */
        init {
            when (Locale.getDefault()) {
                Locale.ENGLISH -> {
                    scheduleDescription = "Begins schedule-reading process (requires prepared file)"
                    changesDescription = "Begins changes-reading process (requires downloaded document)"

                    helpDescription = "Show context help for this application"
                    parseDescription = "Begins basic parsing process (may be useful for debugging process)"
                    writeDescription = "Writes last gotten result value to file"
                    showDescription = "Show last gotten result in the console (terminal)"
                }
                Locale.CHINESE -> {
                    scheduleDescription = "開始計劃閱讀過程（需要準備好的文件）"
                    changesDescription = "開始更改閱讀過程（需要下載的文檔）"

                    helpDescription = "顯示此應用程序的上下文幫助"
                    parseDescription = "開始基本解析過程（可能對調試過程有用）"
                    writeDescription = "將最後獲得的結果值寫入文件"
                    showDescription = "在控制台（終端）中顯示最後得到的結果"
                }

                else -> {
                    scheduleDescription = "Начать процесс считывания файла расписания (требует готового файла)"
                    changesDescription = "Начать процесс чтения замен (требуется загруженный документ)"

                    helpDescription = "Показать контекстную справку для приложения"
                    parseDescription = "Начать базовый процесс парса чего-либо (может быть полезно для тестирования)"
                    writeDescription = "Записать последний полученный результат в файл"
                    showDescription = "Отобразить последний полученный результат в консоли (терминале)"
                }
            }
        }
        /* endregion */

        /* region Functions */

        /**
         * Greets [user].
         * Greeting message depends by [current hour][time].
         */
        private fun greetUser(user: String, time: Int) {
            if (time != 23) {
                if (time <= 6) println("\nGood night, $user!")
                else if (time <= 9) println("\nGood morning, $user!")
                else if (time <= 16) println("\nGood afternoon, $user!")
                else println("\nGood evening, $user!")
            }
            else {
                println("\nNight's become. Civilians lies to sleep and mafia wakes up." +
                                "Beware, $user...")
            }
        }

        /**
         * Parses user [inputted a text][input] and tries to transform it into [object][CommandInfo].
         * Requires [instance][console] of the main class to get [target action][Command] from [list][availableActions].
         */
        private fun parseInputtedText(console: Console, input: String): CommandInfo {
            val splatted = input.split(" ")
            val command = splatted[0]

            // We take all arguments, ignoring the first element (that contains the command itself).
            return CommandInfo(args = splatted.drop(1), action = console.availableActions[command.lowercase()])
        }
        /* endregion */
    }
    /* endregion */
}
