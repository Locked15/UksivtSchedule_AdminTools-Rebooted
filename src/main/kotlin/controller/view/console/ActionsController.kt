package controller.view.console

import controller.data.getter.SiteParser
import controller.data.reader.word.Reader as WordReader
import controller.data.reader.excel.Reader as ExcelReader
import controller.io.*
import controller.io.service.getAffiliationsNames
import controller.io.service.getBranchesNames
import controller.io.service.getGroupsNames
import controller.view.base.ControllerBase
import model.data.schedule.origin.day.TargetedDaySchedule
import model.data.schedule.origin.week.TargetedWeekSchedule
import model.data.schedule.base.Lesson
import model.data.schedule.base.day.Day
import model.data.change.day.TargetedChangesOfDay
import model.data.change.day.GeneralChangesOfDay
import model.data.parse.ParseSource
import model.data.parse.changes.ParseDataDetails
import model.data.schedule.origin.week.GeneralWeekSchedule
import model.data.schedule.result.day.GeneralDayScheduleResult
import model.data.schedule.result.day.TargetedDayScheduleResult
import view.console.Basic
import java.nio.file.Paths
import java.util.Calendar
import java.util.Locale
import kotlin.system.exitProcess


/**
 * Controller class for the [Basic] view.
 * It contains functions that are intended to 'listen' console inputted commands.
 */
class ActionsController : ControllerBase() {

    /* region Properties */

    /**
     * Contains the last gotten result of the executed commands.
     * Basically, it contains string value with 'There is Nothing.\nFor now.' value.
     *
     * For current update, it may contain follow values:
     * * [String] — Before any command was executed.
     *
     * * [TargetedWeekSchedule] — After [parseSchedule] command in Target or Manual mode;
     * * [GeneralWeekSchedule] — After [parseSchedule] in Automatic mode (or reading united asset).
     *
     * * [TargetedChangesOfDay] — After [parseChanges] command in Targeted or Manual mode;
     * * [GeneralChangesOfDay] — After [parseChanges] in Automatic mode.
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
        lastResult = parseScheduleBySelectedMode(args)
        if (args.contains("-w") || args.contains("--write")) writeLastResult()
    }

    /**
     * Begins a parsing process in [the][processAutomaticMode] [selected][processTargetMode] [mode][processManualMode].
     * Selected mode depends on [sent arguments][args].
     */
    private fun parseScheduleBySelectedMode(args: List<String>): Any {
        return if (args.contains("-a") || args.contains("--automatic")) {
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
     * Writes [List] with [TargetedWeekSchedule] to [result][lastResult].
     *
     * Send '-a' or '--automatic' to args, to activate this mode.
     */
    private fun processAutomaticMode(): GeneralWeekSchedule {
        val path = getSafeFilePath("xls", "xlsx")
        val reader = ExcelReader(path)

        return generateSchedulesForAutomaticMode(reader)
    }

    /**
     * Processing a parsing process in target mode.
     * This mode is used by default.
     * Can be used if you are not sure about a whole document preparing, but the current part is prepared.
     *
     * This mode parses only one group schedule.
     * So, writes [TargetedWeekSchedule] to [result][lastResult].
     *
     * Send '-t' or '--target' to args, to activate this mode.
     * OR, you can end nothing's, cause it used by default, as I said.
     */
    private fun processTargetMode(): TargetedWeekSchedule {
        val data = getTargetDataForScheduleParse()
        return data.second.getWeekSchedule(data.first)
    }

    /**
     * Processing a parsing process in manual mode.
     * It's invented to be used on non-parsable documents or sections.
     *
     * So, if you are not sure about the stability of the section or parser, use this mode.
     * Writes [TargetedWeekSchedule] to [result][lastResult].
     *
     * Send '-m' or '--manual' to args, to activate this mode.
     */
    private fun processManualMode(): TargetedWeekSchedule {
        val groupName = inputText("Input group name")
        return generateScheduleForManualMode(groupName)
    }
    /* endregion */

    /* region Command: 'Assets' */

    fun parseAssets(args: List<String>) {
        lastResult = parseAssetsBySelectedMode(args)
        if (args.contains("-w") || args.contains("--write")) writeLastResult()
    }

    private fun parseAssetsBySelectedMode(args: List<String>): Any {
        return if (args.contains("-a") || args.contains("--automatic")) {
            parseAllAvailableAssets()
        }
        else if (args.contains("-u") || args.contains("--united")) {
            parseAssetsByUnitedFile()
        }
        else if (args.contains("-b") || args.contains("--branch")) {
            parseAssetsByBranch()
        }
        else {
            parseAssetsByTarget()
        }
    }

    private fun parseAllAvailableAssets(): GeneralWeekSchedule {
        val results = mutableListOf<TargetedWeekSchedule>()
        for (branch in getBranchesNames()) {
            for (affiliation in getAffiliationsNames(branch)) {
                for (group in getGroupsNames(branch, affiliation)) {
                    readScheduleAsset(branch, affiliation, group)?.let {
                        results.add(it)
                    } ?: run {
                        println("WARNING:\nFind empty value, while processing auto-assets parse." +
                                        "Args: $branch — $affiliation — $group.")
                    }
                }
            }
        }

        println("Auto-Parsing is complete. Is it faster than 'System.Text.Json' on C#?")
        return GeneralWeekSchedule(results)
    }

    private fun parseAssetsByUnitedFile(): GeneralWeekSchedule {
        val path = getSafeFilePath("json")
        return readUnknownAsset<GeneralWeekSchedule>(Paths.get(path))!!
    }

    private fun parseAssetsByBranch(): GeneralWeekSchedule {
        val results = mutableListOf<TargetedWeekSchedule>()
        val targetBranch = inputValueSafely(getBranchesNames(), "Select target branch to parse")
        for (affiliation in getAffiliationsNames(targetBranch)) {
            for (group in getGroupsNames(targetBranch, affiliation)) {
                readScheduleAsset(targetBranch, affiliation, group)?.let {
                    results.add(it)
                }
            }
        }

        return GeneralWeekSchedule(results)
    }

    private fun parseAssetsByTarget(): TargetedWeekSchedule {
        val branch = inputValueSafely(getBranchesNames(), "Select target branch to parse")
        val affiliation = inputValueSafely(getAffiliationsNames(branch), "Select affiliation")
        val group = inputValueSafely(getGroupsNames(branch, affiliation), "Select group to parse")

        return readScheduleAsset(branch, affiliation, group)!!
    }
    /* endregion */

    /* region Command: 'Changes' */

    /**
     * Completes a parsing process on [changes][TargetedChangesOfDay] [document][WordReader.document].
     * Writes value to file, if user sent '-w' or '--write' as arg.
     */
    fun parseChanges(args: List<String>) {
        lastResult = parseChangesBySelectedMode(args)

        if (args.contains("-w") || args.contains("--write")) writeLastResult()
    }

    /**
     * Collects target data for a parsing process.
     * Then, begins changes document parse.
     */
    private fun parseChangesBySelectedMode(args: List<String>): Any {
        val isAutoMode = args.contains("-a") || args.contains("--auto")
        val isUnitedMode = args.contains("-u") || args.contains("--united")
        val data = getTargetDataForChangesParse(isAutoMode,
                                                if (isUnitedMode) ParseSource.UNITED_FILE else ParseSource.DOCUMENT)

        val result = completeChangesParseBySelectedSource(if (isUnitedMode) ParseSource.UNITED_FILE
                                                          else ParseSource.DOCUMENT, data)

        return if (result == null) {
            println("WARNING:\n\tWhen parsing changes (auto: $isAutoMode, united: $isUnitedMode) got 'NULL' value!")
            TargetedChangesOfDay()
        }
        else result
    }

    private fun completeChangesParseBySelectedSource(dataSource: ParseSource, parseData: ParseDataDetails): Any? {
        return if (dataSource.isUnitedMode()) completeChangesParseByUnitedFile(parseData)
        else completeChangesParseByDocument(parseData)
    }

    private fun completeChangesParseByDocument(data: ParseDataDetails): Any? {
        var reader = WordReader(data.pathToFile)
        return if (data.isAutoMode) {
            val results = mutableListOf<TargetedChangesOfDay?>()
            for (group in reader.getAvailableGroups()) {
                reader = WordReader(data.pathToFile)
                results.add(reader.getChanges(group, data.targetDay))
            }

            GeneralChangesOfDay(results)
        }
        else {
            // "data.second" never will be NULL in this place. Because it may be null only in auto mode.
            reader.getChanges(data.targetGroup!!, data.targetDay)
        }
    }

    private fun completeChangesParseByUnitedFile(data: ParseDataDetails): Any? {
        val result = readUnknownAsset<GeneralChangesOfDay>(Paths.get(data.pathToFile))
        return if (data.isAutoMode) {
            result
        }
        else {
            result?.changes?.firstOrNull { change -> change?.targetGroup.equals(data.targetGroup, true) }
        }
    }
    /* endregion */

    /* region Command: 'Final' */

    fun parseFinalSchedule(args: List<String>) {
        val isUnitedMode = args.contains("-u") || args.contains("--united")
        if (completeChecksBeforeBeginFinalize()) {
            val parseSource = if (isUnitedMode) ParseSource.UNITED_FILE else ParseSource.DOCUMENT
            convertLastResultToGeneralScheduleIfItIsTargeted()

            // From this point we begin to get required data and then generate final schedule objects.
            val changesData = completeChangesParseBySelectedSource(parseSource,
                                                                   getTargetDataForChangesParse(true, parseSource))
            lastResult = buildFinalSchedulesWithChangesData(changesData as GeneralChangesOfDay)

            if (args.contains("-w") || args.contains("--write")) writeLastResult()
        }
        else {
            println("WARNING:\nCommand 'Final' can be used only if latest result is filled with basic schedule.")
        }
    }

    private fun convertLastResultToGeneralScheduleIfItIsTargeted() {
        if (lastResult is TargetedWeekSchedule)
            lastResult = GeneralWeekSchedule(mutableListOf((lastResult as TargetedWeekSchedule)))
    }

    private fun buildFinalSchedulesWithChangesData(changesData: GeneralChangesOfDay): GeneralDayScheduleResult {
        val finalSchedules = mutableListOf<TargetedDayScheduleResult>()
        for (weekSchedule in (lastResult as GeneralWeekSchedule)) {
            val targetSchedule = weekSchedule?.getDayScheduleByDay(changesData.getChangesBasicDay())
            // We checks got target schedule. And notify user, because 'NULL' in this situation isn't awaited value.
            if (targetSchedule != null) {
                finalSchedules.add(buildTargetFinalSchedule(weekSchedule.groupName ?: "[NOT_AVAILABLE]",
                                                            targetSchedule, changesData))
            }
            else {
                println("WARNING: \n\t'targetSchedule' in 'buildFinalSchedulesWithChangesData' was 'NULL'.")
            }
        }

        return GeneralDayScheduleResult(finalSchedules)
    }

    private fun buildTargetFinalSchedule(targetGroup: String, targetSchedule: TargetedDaySchedule,
                                         changesData: GeneralChangesOfDay): TargetedDayScheduleResult {
        val targetChange = changesData.getTargetChangeByGroupName(targetGroup)
        return if (targetChange != null) {
            val builtResult = targetSchedule.buildFinalSchedule(changesData.getTargetChangeByGroupName(targetGroup))
            builtResult
        }
        else {
            // TODO: Make 'Information' log level or similar, to log additional information (such non-changed schedule building).
            val builtIn =
                targetSchedule.buildFinalSchedule(targetGroup, changesData.changesDate ?: Calendar.getInstance())
            builtIn
        }
    }

    private fun completeChecksBeforeBeginFinalize() = lastResult is TargetedWeekSchedule ||
            lastResult is GeneralWeekSchedule
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
        if (args.contains("-cd") || args.contains("--changes-document"))
            beginBasicChangesDocumentParsingIfPossible()
        if (args.contains("-sd") || args.contains("--schedule-document"))
            beginBasicScheduleParsingIfPossible()
        if (args.contains("-s") || args.contains("--site"))
            beginBasicSiteParsingIfPossible()
    }

    /**
     * Begins basic changes document parsing, if user sent right arguments.
     */
    private fun beginBasicChangesDocumentParsingIfPossible() {
        val data = getTargetDataForChangesParse(isAutoMode = false,
                                                ParseSource.DOCUMENT)
        val reader = WordReader(data.pathToFile)

        reader.getChanges(data.targetGroup!!, data.targetDay)
    }

    /**
     * Begins basic schedule document parsing if user sent rights arguments.
     */
    private fun beginBasicScheduleParsingIfPossible() {
        val data = getTargetDataForScheduleParse()
        data.second.getWeekSchedule(data.first)
    }

    /**
     * Begins basic site parsing if user sent right arguments.
     */
    private fun beginBasicSiteParsingIfPossible() {
        val months = getSafeIntValue("Input count of parsed months", 0, 12)
        SiteParser().getAvailableNodes(months)
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
        is TargetedWeekSchedule -> writeBasicScheduleToTargetFile(lastResult as TargetedWeekSchedule)
        is GeneralWeekSchedule -> {
            val writeType = getSafeIntValue("Select writing type:" +
                                                    "\n\t0 — United files mode;" +
                                                    "\n\t1 — Standalone files mode." +
                                                    "\nChoose",
                                            0, 1)
            if (writeType == 0)
                writeBasicScheduleToUnitedAsset(inputText("File name"), lastResult as GeneralWeekSchedule)
            else
                writeBasicScheduleToTargetFile(lastResult as GeneralWeekSchedule)
        }

        is TargetedChangesOfDay -> writeDayChangesToFile(lastResult as TargetedChangesOfDay)
        is GeneralChangesOfDay -> writeDayChangesToFile(lastResult as GeneralChangesOfDay)

        is GeneralDayScheduleResult -> {
            val writeType = getSafeIntValue("Select writing type:" +
                                                    "\n\t0 — JSON Asset mode;" +
                                                    "\n\t1 — Word Document mode." +
                                                    "\nChoose",
                                            0, 1)
            if (writeType == 0)
                writeFinalSchedule(inputText("File name"), lastResult as GeneralDayScheduleResult)
            else
                TODO("It can be modified, to create possibility to generate '.docx' (Word) document with final schedule.")
        }

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
         * Then, return [List] with all [available schedules][TargetedWeekSchedule].
         */
        private fun generateSchedulesForAutomaticMode(reader: ExcelReader): GeneralWeekSchedule {
            val list = GeneralWeekSchedule(mutableListOf())
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
         * Returns [generated schedule][TargetedWeekSchedule].
         */
        private fun generateScheduleForManualMode(group: String): TargetedWeekSchedule {
            val schedule = TargetedWeekSchedule(group)
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
                schedule.targetedDaySchedules.add(day)
            }

            return schedule
        }

        /**
         * Generates new [day schedule object][TargetedDaySchedule] by sent [day index][index].
         * Returns this object.
         *
         * Notices user, when generating is complete.
         */
        private fun generateNewDayScheduleAndNoticeUser(index: Int): TargetedDaySchedule {
            val day = TargetedDaySchedule(Day.getValueByIndex(index))
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
        /* endregion */

        /* region Specific-Safe Input Functions */

        /**
         * Asks user to input target information to [schedule document][ExcelReader.document] [parsing][parseSchedule].
         *
         * It returns [Pair] object with following data:
         * * [First][Pair.first] — Target group name;
         * * [Second][Pair.second] — [Reader][ExcelReader] object.
         */
        private fun getTargetDataForScheduleParse(): Pair<String, ExcelReader> {
            val path = getSafeFilePath("xls", "xlsx")
            val reader = ExcelReader(path)
            val target = inputValueSafely(reader.getGroups(), "Select target group")

            return Pair(target, reader)
        }

        /**
         * Asks user to input target information to the [changes document][WordReader.document] [parse][parseChanges].
         *
         * It returns [Triple] object with following data:
         * * [First][Triple.first] — Path to the changes document (or '.json' united-asset, if [mode is united][ParseSource.UNITED_FILE]);
         * * [Second][Triple.second] — Target group name;
         * * [Third][Triple.third] — Day (may be null), to the day-corresponding check.
         */
        private fun getTargetDataForChangesParse(isAutoMode: Boolean, source: ParseSource): ParseDataDetails {
            val path = getSafeFilePath(if (source.isUnitedMode()) "json"
                                       else "docx")
            val group = if (isAutoMode) null else inputText("Input target group")
            val day = if (source.isUnitedMode()) null else getSafeTargetDay()

            return ParseDataDetails(path, group, isAutoMode, day)
        }
        /* endregion */
    }
    /* endregion */
}