package model.data.parse.changes.wrapper


/**
 * Represents a third iteration level (inner) data model of Word document parse.
 * This one must be placed inside the third cycle.
 *
 * @param text  Text of current parsing cell.
 * @param lowerText  Lowered text of current parsing cell.
 */
class InnerIteratorModel(val text: String, val lowerText: String)
