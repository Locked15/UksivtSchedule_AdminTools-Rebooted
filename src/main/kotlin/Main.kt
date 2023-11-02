import model.environment.state.GlobalState
import model.environment.state.GlobalStateBuilder
import view.console.Basic
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.Properties
import javax.naming.ConfigurationException


private var programArgs: Array<String> = arrayOf()

val globalState: GlobalState by lazy {
    val builder = GlobalStateBuilder()
        .setProjectDirectory(System.getProperty("user.dir"))
        .setByArgs(programArgs)

    builder.build()
}

val config: Properties by lazy {
    val props = Properties()
    val result = runCatching {
        val readerStream = FileInputStream(Paths.get(System.getProperty("user.dir"),
                                                     "${globalState.configFileName}.cfg").toFile())
        props.load(readerStream)
    }

    if (result.isFailure) throw ConfigurationException(
            "Config file MUST be provided ('${globalState.configFileName}.cfg' in root dir).")
    props
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
