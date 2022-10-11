package model.exception

/**
 * Throws, when a parsed document doesn't contain a targeted group.
 *
 * Throws with [setten message][message].
 */
class GroupDoesNotExistException(message: String): Exception(message)
