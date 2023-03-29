import model.environment.state.GlobalState
import model.environment.state.GlobalStateBuilder
import view.console.Basic


private var programArgs: Array<String> = arrayOf()

val globalState: GlobalState by lazy {
    val builder = GlobalStateBuilder()
        .setProjectDirectory(System.getProperty("user.dir"))
        .setByArgs(programArgs)

    builder.build()
}

/**
 * Entry point of the program.
 *
 * @param args Arguments of the program.
 */
fun main(args: Array<String>) {
    programArgs = args.map { it.lowercase() }.toTypedArray()

    print("Enter your name: ")
    Basic(readln()).beginSession()
    endSession()
}

private fun endSession() {
    println("\nConnection terminated.\nGood day.")
}
