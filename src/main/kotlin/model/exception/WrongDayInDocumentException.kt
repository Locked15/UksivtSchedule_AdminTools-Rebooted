package model.exception

/**
 * Thrown, when a parsing document and searched-time have different values.
 *
 * Throws with [setten message][message].
 */
class WrongDayInDocumentException(message: String) : IllegalArgumentException(message)
