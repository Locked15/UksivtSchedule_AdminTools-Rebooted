import view.console.Basic


val projectDirectory: String = System.getProperty("user.dir")

val resourcePathElements = listOf("src", "main", "resources")

/**
 * Entry point of the program.
 *
 * @param args Arguments of the program.
 */
fun main(args: Array<String>) {
    print("Enter your name: ")
    Basic(readln()).beginSession()

    println("\nConnection terminated.\nGood day.")
}
