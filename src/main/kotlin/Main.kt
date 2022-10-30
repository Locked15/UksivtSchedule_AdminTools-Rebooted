import view.console.Basic


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
