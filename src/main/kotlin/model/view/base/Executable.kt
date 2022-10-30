package model.view.base

import model.view.Command


/**
 * Interface for all executable class.
 * Like [Commands][Command], etc.
 */
interface Executable {

    /**
     * Executes current action.
     */
    fun execute()
}
