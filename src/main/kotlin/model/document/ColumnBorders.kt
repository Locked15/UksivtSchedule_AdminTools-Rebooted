package model.document

/**
 * Used by parse classes, to identify target columns borders.
 *
 * Properties contain neighbors indices of the target column.
 */
class ColumnBorders(var leftBorderIndex: Int?, var rightBorderIndex: Int?)
