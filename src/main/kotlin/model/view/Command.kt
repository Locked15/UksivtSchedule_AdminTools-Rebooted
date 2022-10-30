package model.view

import model.view.base.Executable


/**
 * Command class that contains basic information and wrapper for executable code (also known as 'functor').
 *
 * Use it to create new custom commands with custom implementations.
 * I highly recommend following these rules:
 * * Don't place logic inside command, place it inside controllers instead;
 * * Inside commands must be placed only controller functions callings and invokes, not realizations;
 * * Don't overload commands with logic/code, keep them simple;
 * * Use *[setUpArgs]* function instead whole object late-initialization,
 *   unless you already know all arguments on constructor call;
 * * Don't try to make [commands][action] valuable (with return values),
 *   make controller functions valuable with properties instead.
 *
 * Inherits (realizes) [Executable] interface.
 */
class Command(val name: String, private var args: List<String>, private val action: (args: List<String>) -> Unit) :
        Executable {

    /* region Constructors */

    /**
     * Creates a new [command object][Command] with a given executable code.
     *
     * Sets [name] to '—' and [args] to an [empty list][listOf].
     */
    constructor(action: (args: List<String>) -> Unit) : this("—", listOf(), action)

    /**
     * Creates a new [command object][Command] with a given name and executable code.
     *
     * Sets [args] to an [empty list][listOf].
     */
    constructor(name: String, action: (args: List<String>) -> Unit) : this(name, listOf(), action)
    /* endregion */

    /* region Functions */

    /**
     * Monad-Pattern function, that allows to set [arguments][Command.args] to a current object inside a calling chain.
     * After setting [args], it returns the [current object][Command].
     */
    fun setUpArgs(args: List<String>): Command {
        this.args = args
        return this
    }

    /**
     * Executes current [action] with the given [arguments][args].
     */
    override fun execute() {
        action.invoke(args)
    }
    /* endregion */
}
