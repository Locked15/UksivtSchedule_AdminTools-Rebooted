package model.view

/**
 * Contains information about the [command][Command].
 *
 * @param args  Given arguments of the function.
 * @param action  [Pair] object, that contains [description of the command][Pair.first] and the [command][Pair.second].
 *                This value may be 'NULL'.
 */
class CommandInfo(var args: List<String>, val action: Pair<String, Command>?)
