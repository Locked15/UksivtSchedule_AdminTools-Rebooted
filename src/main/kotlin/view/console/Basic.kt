package view.console

import controller.view.console.ActionsController
import model.view.Command
import model.view.CommandInfo
import java.util.*


/**
 * Basic view element.
 *
 * Contains logic of the user-system interactions.
 * Works via console.
 */
class Basic(private val user: String) {

    /* region Properties */

    /**
     * Contains last executed command with arguments and properties.
     */
    private var lastCommand = "help -f";

    /**
     * Contains a controller object with realizations of the interaction functions.
     *
     * When user performs any accessible action, view sends a message to the controller,
     *      and the controller executes corresponding actions (commands).
     * Then, controller may return something (like the success of execution).
     *
     * View Action -> Controller Command -> View Result.
     */
    private val controller = ActionsController()

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
            "schedule" to Pair(scheduleCommandDescription,
                               Command("Schedule") {
                                   controller.parseSchedule(it)
                               }),
            "read" to Pair(readCommandDescription,
                           Command("Read") {
                                 controller.readAssets(it)
                             }),
            "changes" to Pair(changesCommandDescription,
                              Command("Changes") {
                                  controller.parseChanges(it)
                              }),
            "final" to Pair(finalCommandDescription,
                            Command("Final") {
                                controller.parseFinalSchedule(it)
                            }),
            // Functional Commands:
            "help" to Pair(helpCommandDescription,
                           Command("Help") {
                               controller.showHelp()
                           }),
            "parse" to Pair(parseCommandDescription,
                            Command("Parse") {
                                controller.initializeBasicParsingProcessByArguments(it)
                            }),
            "sync" to Pair(syncCommandDescription,
                           Command("Sync") {
                               controller.beginSynchronization(it)
                           }),
            "write" to Pair(writeCommandDescription,
                            Command("Write") {
                                controller.writeLastResult()
                            }),
            "show" to Pair(showCommandDescription,
                           Command("Show") {
                               controller.showLastResult()
                           }),
            "exit" to Pair(exitCommandDescription,
                           Command("Exit") {
                               controller.exit()
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
        if (input.isNotBlank()) {
            val rawCommand = if (input.contains("last", true)) lastCommand else input
            val commandInfo = parseInputtedText(this, rawCommand.trim())
            //? We should check action to presence or app will crash (as expected, by the way).
            if (commandInfo.action != null) {
                val force = commandInfo.args.contains("-f") || commandInfo.args.contains("--force")
                if (force || confirmCommandExecution(commandInfo.action.first)) {
                    try {
                        executeCommand(commandInfo.args, commandInfo.action.second)
                    }
                    catch (exception: Exception) {
                        println("\n\n!ERROR:\n\tNot-Specified error happened on command execution." +
                                        "\nInfo: ${exception.message}.\n")
                    }
                }
                //? We save the last command after execution, so it saves only if command and arguments are valid.
                lastCommand = rawCommand
            }
            else {
                println("Inputted command isn't supported, please enter 'help' to get list of supported ones.\n")
            }
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
        private val helpCommandDescription: String

        /**
         * Description of the 'Schedule' command.
         *
         * This is a valuable one.
         */
        private val scheduleCommandDescription: String

        /**
         * Description of the 'Assets' command.
         *
         * This is valuable.
         */
        private val readCommandDescription: String

        /**
         * Description of the 'Changes' command.
         *
         * This is a valuable one.
         */
        private val changesCommandDescription: String

        /**
         * Description of the 'Final' command.
         *
         * This is a valuable one.
         */
        private val finalCommandDescription: String

        /**
         * Description of the 'Parse' command.
         */
        private val parseCommandDescription: String

        /**
         * Description of the 'Sync' command.
         */
        private val syncCommandDescription: String

        /**
         * Description of the 'Write' command.
         */
        private val writeCommandDescription: String

        /**
         * Description of the 'Show' command.
         */
        private val showCommandDescription: String

        /**
         * Description of the 'Exit' command.
         */
        private val exitCommandDescription: String
        /* endregion */

        /* region Initializers */

        /**
         * Initializes all static properties by current system language.
         * Some languages aren't supported yet.
         */
        init {
            when (Locale.getDefault()) {
                Locale.ENGLISH -> {
                    scheduleCommandDescription = "Begins schedule-reading process (requires prepared file)"
                    readCommandDescription = "Begins assets-reading process (requires prepared assets in json format)"
                    changesCommandDescription = "Begins changesOfDay-reading process (requires downloaded document)"
                    finalCommandDescription =
                        "Begins merging process between basic schedule and changes. Requires last value to contain basic schedule"

                    helpCommandDescription = "Show context help for this application"
                    parseCommandDescription = "Begins basic parsing process (may be useful for debugging process)"
                    syncCommandDescription = "Begins synchronization process between last result and db"
                    writeCommandDescription = "Writes last gotten result value to file"
                    showCommandDescription = "Show last gotten result in the console (terminal)"
                    exitCommandDescription = "Exits from program"
                }
                Locale.CHINESE -> {
                    scheduleCommandDescription = "開始計劃閱讀過程（需要準備好的文件）"
                    readCommandDescription = "開始資產讀取過程（需要 json 格式的準備資產）"
                    changesCommandDescription = "開始更改閱讀過程（需要下載的文檔）"
                    finalCommandDescription = "開始基本計劃和變更之間的合併過程。 需要最後一個值來包含基本計劃。"

                    helpCommandDescription = "顯示此應用程序的上下文幫助"
                    parseCommandDescription = "開始基本解析過程（可能對調試過程有用）"
                    syncCommandDescription = "TODO: Translate this."
                    writeCommandDescription = "將最後獲得的結果值寫入文件"
                    showCommandDescription = "在控制台（終端）中顯示最後得到的結果"
                    exitCommandDescription = "退出程序"
                }

                else -> {
                    scheduleCommandDescription = "Начать процесс считывания файла расписания (требует готового файла)"
                    readCommandDescription =
                        "Начать процесс считывания файла ассетов (требуется готовый файл в формате JSON)"
                    changesCommandDescription = "Начать процесс чтения замен (требуется загруженный документ)"
                    finalCommandDescription =
                        "Начать процесс слияния базового расписания и замен. Требует наличия базового расписания в 'lastResult'"

                    helpCommandDescription = "Показать контекстную справку для приложения"
                    parseCommandDescription =
                        "Начать базовый процесс парса чего-либо (может быть полезно для тестирования)"
                    syncCommandDescription = "Начать процесс синхронизации последнего полученного результата и БД"
                    writeCommandDescription = "Записать последний полученный результат в файл"
                    showCommandDescription = "Отобразить последний полученный результат в консоли (терминале)"
                    exitCommandDescription = "Выход из программы"
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
                                "\nBeware, $user...")
            }
        }

        /**
         * Parses user [inputted a text][input] and tries to transform it into [object][CommandInfo].
         * Requires [instance][console] of the main class to get [target action][Command] from [list][availableActions].
         */
        private fun parseInputtedText(console: Basic, input: String): CommandInfo {
            val split = input.split(' ')
            val command = split[0]

            // We take all arguments, ignoring the first element (that contains the command itself).
            return CommandInfo(args = split.drop(1).map { arg -> arg.lowercase() },
                               action = console.availableActions[command.lowercase()])
        }
        /* endregion */
    }
    /* endregion */
}
