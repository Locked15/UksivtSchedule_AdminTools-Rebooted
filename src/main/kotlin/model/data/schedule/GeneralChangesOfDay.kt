package model.data.schedule


/**
 * This is class, that needed to write last result and check its type correctly.
 * I made it, because in Kotlin no stable way to check generic type to equality.
 */
class GeneralChangesOfDay(val changes: List<TargetChangesOfDay?>)
