package controller.view.console

import controller.data.getter.SiteParser
import controller.data.reader.word.Reader as WordReader
import controller.data.reader.excel.Reader as ExcelReader
import controller.io.*
import model.data.schedule.DaySchedule
import model.data.schedule.WeekSchedule
import model.data.schedule.base.Lesson
import model.data.schedule.base.day.Day
import model.data.schedule.Changes
import model.data.schedule.ChangesList
import model.exception.WrongDayInDocumentException
import view.console.Basic
import java.util.Locale
import kotlin.system.exitProcess


/**
 * Controller class for the [Basic] view.
 * It contains functions that are intended to 'listen' console inputted commands.
 */
class BasicController {

    /* region Properties */

    /**
     * Contains the last gotten result of the executed commands.
     * Basically, it contains string value with 'There is Nothing.\nFor now.' value.
     *
     * For current update, it may contain follow values:
     * * [String] — Before any command was executed.
     * * [WeekSchedule] — After [parseSchedule] command;
     * * [List] with [WeekSchedule] — After [parseSchedule] in Automatic Mode;
     * * [Changes] — After [parseChanges] command.
     */
    private var lastResult: Any = "There is Nothing.\nFor now."
    /* endregion */

    /* region Valuable Commands */

    /* region Command: 'Schedule' */

    /**
     * Executes a parsing schedule process.
     * Execution completes depending on [sent arguments][args].
     * Writes gotten value to [last available result][lastResult].
     *
     * Supported parameters:
     * * '-a' or '--automatic' — Converts a process to automatic, that parses all available schedules from a document;
     * * '-t' or '--target' — Basic mode, but can be used explicitly. Parsed schedule only for the targeted group;
     * * '-m' or '--manual' — Converts a process to manual,
     *                        that allows the user to input all values manually from the terminal or console.
     * * '-w' or '--write' — Program will automatically write gotten value to file.
     */
    fun parseSchedule(args: List<String>) {
        parseScheduleBySelectedMode(args)
        writeScheduleBySentArgs(args)
    }

    /**
     * Begins a parsing process in [the][processAutomaticMode] [selected][processTargetMode] [mode][processManualMode].
     * Selected mode depends on [sent arguments][args].
     */
    private fun parseScheduleBySelectedMode(args: List<String>) {
        lastResult = if (args.contains("-a") || args.contains("--automatic")) {
            processAutomaticMode()
        }
        else if (args.contains("-m") || args.contains("--manual")) {
            processManualMode()
        }
        else {
            processTargetMode()
        }
    }

    /**
     * Processing a parsing process in automatic mode.
     * Can be used if a target document is prepared to fully automatic parse.
     *
     * Writes [List] with [WeekSchedule] to [result][lastResult].
     *
     * Send '-a' or '--automatic' to args, to activate this mode.
     */
    private fun processAutomaticMode(): List<WeekSchedule> {
        val path = getSafeFilePath(".xls", ".xlsx")
        val reader = ExcelReader(path)

        return generateSchedulesForAutomaticMode(reader)
    }

    /**
     * Processing a parsing process in target mode.
     * This mode is used by default.
     * Can be used if you are not sure about a whole document preparing, but the current part is prepared.
     *
     * This mode parses only one group schedule.
     * So, writes [WeekSchedule] to [result][lastResult].
     *
     * Send '-t' or '--target' to args, to activate this mode.
     * OR, you can end nothing's, cause it used by default, as I said.
     */
    private fun processTargetMode(): WeekSchedule {
        val data = getTargetDataForScheduleParse()
        return data.second.getWeekSchedule(data.first)
    }

    /**
     * Processing a parsing process in manual mode.
     * It's invented to be used on non-parsable documents or sections.
     *
     * So, if you are not sure about the stability of the section or parser, use this mode.
     * Writes [WeekSchedule] to [result][lastResult].
     *
     * Send '-m' or '--manual' to args, to activate this mode.
     */
    private fun processManualMode(): WeekSchedule {
        val groupName = inputText("Input group name")
        return generateScheduleForManualMode(groupName)
    }

    /**
     * Checks sent [arguments][args] and writes a value to the file, if user sent needed arg.
     *
     * To enable this, sent '-w' or '--write' as arg.
     */
    private fun writeScheduleBySentArgs(args: List<String>) {
        if (args.contains("-w") || args.contains("--write")) {
            writeLastResult()
        }
    }
    /* endregion */

    /* region Command: 'Changes' */

    /**
     * Completes a parsing process on [changes][Changes] [document][WordReader.document].
     * Writes value to file, if user sent '-w' or '--write' as arg.
     */
    fun parseChanges(args: List<String>) {
        val auto = args.contains("-a") || args.contains("--auto")
        collectDataAndParseChangesFile(auto)

        if (args.contains("-w") || args.contains("--write")) {
            writeLastResult()
        }
    }

    /**
     * Collects target data for a parsing process.
     * Then, begins changes document parse.
     */
    private fun collectDataAndParseChangesFile(auto: Boolean) {
        val data = getTargetDataForChangesParse(auto)
        var reader = WordReader(data.first)

        if (auto) {
            val results = mutableListOf<Changes?>()
            for (group in reader.getAvailableGroups()) {
                reader = WordReader(data.first)
                results.add(reader.getChanges(group, data.third))
            }

            lastResult = ChangesList(results)
        }
        else {
            // "data.second" never will be NULL in this place. Because it may be null only in auto mode.
            lastResult = reader.getChanges(data.second!!, data.third) ?: Changes()
        }
    }
    /* endregion */
    /* endregion */

    /* region Functional Commands */

    /* region Command: 'Help' */

    /**
     * Shows a context help message for this application.
     *
     * Update [helpMessage] if you update interaction logic.
     */
    fun showHelp() = println(helpMessage)
    /* endregion */

    /* region Command: 'Parse' */

    /**
     * Initializes a basic parsing process (without data-storage) by sent arguments.
     *
     * Currently supported parsing types:
     * * '-s' or '--site' — Parses college [website](https://www.uksivt.ru/zameny).
     * * '-sd' or '--schedule-document' — Parses prepared schedule document in target mode.
     * * '-cd' or '--changes-document' — Parses downloaded changes document.
     *
     * This function invented to debugging purpose, so it DOES NOT write value to [result][lastResult].
     * Also, it supports only a target mode for schedule parsing, because it's a basic mode for others.
     */
    fun initializeBasicParsingProcessByArguments(args: List<String>) {
        beginBasicChangesDocumentParsingIfPossible(args)
        beginBasicScheduleParsingIfPossible(args)
        beginBasicSiteParsingIfPossible(args)
    }

    /**
     * Begins basic changes document parsing, if user sent right arguments.
     */
    private fun beginBasicChangesDocumentParsingIfPossible(args: List<String>) {
        if (args.contains("-cd") || args.contains("--changes-document")) {
            val data = getTargetDataForChangesParse(false)
            val reader = WordReader(data.first)

            reader.getChanges(data.second!!, data.third)
        }
    }

    /**
     * Begins basic schedule document parsing if user sent rights arguments.
     */
    private fun beginBasicScheduleParsingIfPossible(args: List<String>) {
        if (args.contains("-sd") || args.contains("--schedule-document")) {
            val data = getTargetDataForScheduleParse()
            data.second.getWeekSchedule(data.first)
        }
    }

    /**
     * Begins basic site parsing if user sent right arguments.
     */
    private fun beginBasicSiteParsingIfPossible(args: List<String>) {
        if (args.contains("-s") || args.contains("--site")) {
            val months = getSafeIntValue("Input count of parsed months", 0, 12)
            SiteParser().getAvailableNodes(months)
        }
    }
    /* endregion */

    /* region Command: 'Write' */

    /**
     * Writes last [gotten result][lastResult] to the file.
     * File will be saved inside './src/main/resources' directory.
     *
     * Returns [result of writing][Boolean].
     * If value can't be written, prints message and returns false.
     */
    fun writeLastResult() = when (lastResult) {
        is WeekSchedule -> writeSchedule(lastResult as WeekSchedule)
        is List<*> -> writeSchedule(lastResult as List<*>)

        is Changes -> writeChanges(lastResult as Changes)
        is ChangesList -> writeChanges(lastResult as ChangesList)

        else -> {
            println("Unknown (or incompatible) type to write.")
            false
        }
    }
    /* endregion */

    /* region Command: 'Show' */

    /**
     * Shows string representation of the last [gotten result][lastResult] of executed commands.
     *
     * REMEMBER: If you add a new type, that can be written to the result, you MUST define '.toString()' override.
     * If you don't, string representation will be incorrect.
     */
    fun showLastResult() = println(lastResult.toString())
    /* endregion */

    /* region Command: 'Exit' */

    /**
     * Exits a current program process.
     *
     * Warning: calling this function will close program immediately, without any other actions.
     */
    fun exit(): Nothing = exitProcess(0)
    /* endregion */
    /* endregion */

    /* region Companion */

    companion object {

        /* region Properties */

        /**
         * Contains a current locale help message.
         *
         * Current supported locales:
         * * English;
         * * Chinese;
         * * Russian.
         */
        private val helpMessage: String
        /* endregion */

        /* region Initializers */

        /**
         * Initializes the help message with current locale.
         */
        init {
            when (Locale.getDefault()) {
                Locale.ENGLISH -> helpMessage =
                    """
                        Admin Tools for Uksivt Schedule System by Locked15.
                        
                        To close program, input nothing in command input box.
                        All commands are case-insensitive.
                        
                        Valuable commands:
                          * Type 'schedule' to begin schedule-reading process (requires prepared file);
                          * Type 'changes' to begin processing changes document (requires downloaded document).
                        (This commands writes gotten value to property, so they can be written later).
                        
                        Functional commands:
                          * Type 'help' to show context help message (this command);
                          * Type 'parse' to begin basic parsing process:
                            It requires second parameter:
                              '-cd' or '--changes-document' — To parse changes document;
                              '-sd' or '--schedule-document' — To parse schedule document;
                              '-s' or '--site' — To parse college site.
                          * Type 'write' to write last gotten value to file;
                          * Type 'show' to show last gotten value in terminal.
                          * Type 'exit', to completely close the program.
                            Using this command will close program immediately.
                        (This commands just makes some functions and don't store data).
                        
                        If you used 'schedule' command, you can use optional parameter:
                          * '-a' or '--automatic' — To automatically generate all available schedule files;
                          * '-t' or '--target' — To generate schedule in target mode (used by default);
                          * '-m' or '--manual' — To generate schedule file from manual-mode function.
                        
                        All valuable commands supports optional parameters:
                          * '-w' or '--write' — To immediately write gotten value to file.
                        
                        Divide parts of the command by one space.
                        You can NOT merge parameters, write it SEPARATELY.
                        All parameters are case-insensitive and order-insensitive.
                        All meaningless/wrong parameters will be automatically ignored.
                    """.trimIndent()
                Locale.CHINESE -> helpMessage =
                    """
                        Locked15 的 Uksivt 計劃系統管理工具。
                        
                        要關閉程序，在命令選擇框中不輸入任何內容。
                        所有命令都不區分大小寫。
                        
                        有價值的命令：
                          * 類型 'schedule' 開始計劃閱讀過程（需要準備好的文件）;
                          * 類型 'changes' 開始處理變更文件（需要下載文件）。
                        （此命令將獲取的值寫入屬性，因此可以稍後再寫入）。
                        
                        功能命令：
                          * 類型 'help' 顯示上下文幫助消息（此命令）；
                          * 類型 'parse' 開始基本的解析過程：
                            它需要第二個參數：
                              '-cd' 或者 '--changes-document' — 解析更改文檔；
                              '-sd' 或者 '--schedule-document' — 解析進度文件；
                              '-s' 或者 '--site' — 解析大學網站。
                          * 類型 'write' 將最後得到的值寫入文件；
                          * 類型 'show' 在終端中顯示最後得到的值。
                          * 鍵入“退出”以完全關閉程序。
                            使用此命令將立即退出。
                        （這個命令只是做一些功能，不存儲數據）。
                        
                        如果你用過 'schedule' 命令，您可以使用可選參數：
                          * '-a' 或者 '--automatic' — 自動生成所有可用的日程文件；
                          * '-t' 或者 '--target' — 在目標模式下生成計劃（默認使用）；
                          * '-m' 或者 '--manual' — 從手動模式功能生成計劃文件。
                        
                        所有有價值的命令都支持可選參數：
                          * '-w' 或者 '--write' — 立即將獲得的值寫入文件。
                        
                        將命令的各個部分除以一個空格。
                        你不能合併參數，單獨寫。
                        所有參數都不區分大小寫和順序。
                        所有無意義的錯誤參數將被自動忽略。
                    """.trimIndent()

                else -> helpMessage =
                    """
                        Средства Администрирования для Системы Расписания УКСиВТ от Locked15.
                        
                        Чтобы закрыть программу не указывайте название команды в строке ввода.
                        Все команды нечувствительны к регистру.
                        
                        Значимые команды:
                          * Введите 'schedule', чтобы начать чтение документа расписания (требует готового документа);
                          * Введите 'changes', чтобы начать чтение документа замен (требует скачанного документа).
                        (Эти команды записывают полученное значение в свойство, так что его можно обработать позднее).
                        
                        Функциональные команды:
                          * Введите 'help', чтобы вывести контекстную справку по приложению (текущая команда);
                          * Введите 'parse', чтобы начать базовый процесс чтения чего-либо:
                            Команда требует хотя бы один параметр для работы:
                              '-cd' или '--changes-document' — Для чтения документа замен;
                              '-sd' или '--schedule-document' — Для чтения документа расписания;
                              '-s' или '--site' — Для чтения страницы сайта колледжа.
                          * Введите 'write', чтобы записать последнее полученное значение в файл;
                          * Введите 'show', чтобы вывести последнее полученное значение в терминал (консоль).
                          * Введите 'exit', чтобы полностью закрыть программу.
                            Использование этой команды завершит работу немедленно. 
                        (Эти команды просто выполняют какие-либо действия и не сохраняют свои результаты).
                        
                        Если вы использовали команду 'schedule', вы можете использовать дополнительные параметры:
                          * '-a' или '--automatic' — Чтобы выполнить чтение в автоматическом режиме;
                          * '-t' или '--target' — Чтобы выполнить чтение в целевом режиме;
                          * '-m' или '--manual' — Чтобы сгенерировать расписание вручную.
                        
                        Все значимые команды поддерживают следующие параметры:
                          * '-w' или '--write' — Автоматически записывает результат выполнения в файл.
                        
                        Разделяйте части команды ОДНИМ пробелом.
                        Параметры НЕЛЬЗЯ объединять, следует писать их РАЗДЕЛЬНО.
                        Все параметры нечувствительны к регистру и порядку их написания.
                        Все бессмысленные/неверные параметры будут автоматически проигнорированы.
                    """.trimIndent()
            }
        }
        /* endregion */

        /* region Commands Functions */

        /**
         * Completes schedule generating for automatic mode.
         * Requires to be created [reader] object to work.
         *
         * Iterates through all available groups and writes its schedule to list.
         * Then, return [List] with all [available schedules][WeekSchedule].
         */
        private fun generateSchedulesForAutomaticMode(reader: ExcelReader): List<WeekSchedule> {
            val list = mutableListOf<WeekSchedule>()
            reader.getGroups().forEach { group ->
                group?.let {
                    list.add(reader.getWeekSchedule(group))
                }
            }

            return list
        }

        /**
         * Completes schedule generating for manual mode.
         * Requires target [group name][group].
         *
         * Iterator through all seven days of week and asks user to input information.
         * Returns [generated schedule][WeekSchedule].
         */
        private fun generateScheduleForManualMode(group: String): WeekSchedule {
            val schedule = WeekSchedule(group)
            for (i in 0 until 7) {
                val day = generateNewDayScheduleAndNoticeUser(i)
                for (j in 0 until 7) {
                    if (confirmLessonIsPresented(j)) {
                        val name = inputText("Input lesson name")
                        val teacher = inputText("Input lesson teacher")
                        val place = inputText("Input lesson place")

                        day.lessons.add(Lesson(j, name, teacher, place))
                    }
                    else {
                        day.lessons.add(Lesson(j))
                    }
                }
                schedule.daySchedules.add(day)
            }

            return schedule
        }

        /**
         * Generates new [day schedule object][DaySchedule] by sent [day index][index].
         * Returns this object.
         *
         * Notices user, when generating is complete.
         */
        private fun generateNewDayScheduleAndNoticeUser(index: Int): DaySchedule {
            val day = DaySchedule(Day.getValueByIndex(index))
            println("Current Day: ${day.day.englishName}.")

            return day
        }

        /**
         * Asks user to confirm that the [current][lessonNumber] [Lesson] is presented for target day.
         * The Next actions will depend on the user answer:
         * * If Not (N/n), the program will write default value (a lesson without data, only with number).
         * * If Yes (Y/y), the program will [collect][Lesson.name] [data][Lesson.teacher] [about][Lesson.place]
         *   a lesson, and then generates it.
         */
        private fun confirmLessonIsPresented(lessonNumber: Int): Boolean {
            val confirm = inputText("Lesson №$lessonNumber is presented (Y/N)")
            return confirm.contains("y", true)
        }

        /**
         * Asks user to input [day index][Int] for day-check on changes [parsing process][parseChanges].
         * Then, a result depends on user input:
         * * If user input correct value, the program will check day-corresponding and
         *   throws [exception][WrongDayInDocumentException] if days aren't equal.
         * * If user input '-1', the program will ignore the day-corresponding check.
         */
        private fun getSafeTargetDay(): Day? {
            val index = getSafeIntValue("Input target day index (0..6) to insert day-check " +
                                                "OR '-1' to ignore it", -1, 6)

            return if (index == -1) null else Day.getValueByIndex(index)
        }
        /* endregion */

        /* region Input Group Functions */

        /**
         * Asks user to input [group name][String].
         * Checks inputted group name to be presented inside an [available groups list][groups].
         *
         * If you don't want to use check, just send an empty list ('[listOf]').
         */
        private fun getSafeTargetGroup(groups: List<String?>): String {
            var target: String
            do {
                target = inputText("Select target group")
            } while (checkGroupCondition(target, groups))

            return getGroupNameOrInputtedValue(target, groups)
        }

        /**
         * Checks 'do...while' condition to [getSafeTargetGroup] function.
         */
        private fun checkGroupCondition(target: String, groups: List<String?>) = groups.isNotEmpty() &&
                groups.find { group ->
                    group?.equals(target, true) == true
                } == null

        /**
         * Returns the [group name][String], as it presented in [available list][groups] OR
         * if list it empty, returns base user [inputted value][target].
         */
        private fun getGroupNameOrInputtedValue(target: String, groups: List<String?>) = groups.find { group ->
            group?.equals(target, true) == true
        } ?: target
        /* endregion */

        /* region General Functions */

        /**
         * Asks user to input target information to [schedule document][ExcelReader.document] [parsing][parseSchedule].
         *
         * It returns [Pair] object with following data:
         * * [First][Pair.first] — Target group name;
         * * [Second][Pair.second] — [Reader][ExcelReader] object.
         */
        private fun getTargetDataForScheduleParse(): Pair<String, ExcelReader> {
            val path = getSafeFilePath(".xls", ".xlsx")
            val reader = ExcelReader(path)
            val target = getSafeTargetGroup(reader.getGroups())

            return Pair(target, reader)
        }

        /**
         * Asks user to input target information to the [changes document][WordReader.document] [parse][parseChanges].
         *
         * It returns [Triple] object with following data:
         * * [First][Triple.first] — Path to the changes document;
         * * [Second][Triple.second] — Target group name;
         * * [Third][Triple.third] — Day (may be null), to the day-corresponding check.
         */
        private fun getTargetDataForChangesParse(auto: Boolean): Triple<String, String?, Day?> {
            val path = getSafeFilePath(".doc", ".docx")
            val group = if (auto) null else inputText("Input target group")
            val day = getSafeTargetDay()

            return Triple(path, group, day)
        }

        /**
         * Asks user to input [file path][String] to target file.
         * Checks a file extension to be presented inside a [supported ones list][extensions].
         *
         * If you don't want to check value, just don't send anything to this function.
         */
        private fun getSafeFilePath(vararg extensions: String): String {
            var path: String
            do {
                path = inputText("Input file path").trim('"', ' ')
            } while (!extensions.any { ext -> path.endsWith(ext) })

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
        private fun getSafeIntValue(ask: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): Int {
            var input: String
            do {
                input = inputText(ask)
            } while (input.toIntOrNull() == null || input.toInt() < min || input.toInt() > max)

            return input.toInt()
        }

        /**
         * Asks user to [input][readln] [text][String].
         * Sent [text][ask] uses as tooltip for input.
         *
         * The program uses string templates for tooltip, so you don't need to write ':' an inside text.
         * Just write a query.
         */
        private fun inputText(ask: String): String {
            print("$ask: ")
            return readln()
        }
        /* endregion */
    }
    /* endregion */
}
