import view.Console


/**
 * Entry point of the program.
 *
 * @param args Arguments of the program.
 */
fun main(args: Array<String>) {
    print("Enter your name: ")
    Console(readln()).beginSession()

    println("\nConnection terminated.\nGood day.")
}
