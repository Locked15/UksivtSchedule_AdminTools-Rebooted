import java.util.Dictionary

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
val actions = mapOf(
        "help" to Pair("Show context help for this application.", Runnable {
            print("Hey, you selected 0!")
        }),
)

/**
 * Entry point of the program.
 *
 * @param args Arguments of the program.
 */
fun main(args: Array<String>) {
    do {
        print("So, what you want to do now?\nEnter command code: ")
        val action = readlnOrNull()

        performUserInput(action ?: "help")
    }
    while (!action.isNullOrBlank())

    println("\nConnection terminated.\nGood day.")
}

private fun performUserInput(input: String) {
    val action = actions[input.lowercase()]
    if (action != null) {
        print("Selected command: ${action.first}. \nAre you sure (Y/N)? ")
        val confirmation = readlnOrNull()

        if (!confirmation.isNullOrBlank() && confirmation.equals("y", true)) {
            action.second.run()
            println("\nExecution complete.\n")
        }
    }
    else {
        println("Inputted command isn't supported, please enter 'help' to get more information.\n")
    }
}
