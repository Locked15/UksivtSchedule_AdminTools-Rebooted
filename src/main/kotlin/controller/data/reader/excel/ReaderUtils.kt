package controller.data.reader.excel

import model.data.parse.schedule.ColumnBorders
import model.data.parse.schedule.DayColumnInfo
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.util.concurrent.atomic.AtomicBoolean
import model.data.schedule.base.day.fromRussianString as getDayValueFromString


/* region "searchTargetSheet" function */

/**
 * Searches [a target sheet][Sheet] inside [book], by send [group name][groupName].
 * If search isn't successful, returns 'NULL' value.
 *
 * Use full-array iterations, so may negatively affect performance.
 *
 * Earlier, it was a monolithic and the whole function, but on *Rebooted* version, I refactored it.
 * Now statement-checks logic moved to [private][checkSheetToContain] [functions][checkCellToContain].
 */
fun searchTargetSheet(book: XSSFWorkbook, groupName: String): Sheet? {
    val sheets = book.sheetIterator()
    for (sheet in sheets) {
        if (checkSheetToContain(sheet, groupName)) {
            return sheet
        }
    }

    return null
}

/**
 * Checks sent [sheet] to contain [target group][groupName].
 */
private fun checkSheetToContain(sheet: Sheet, groupName: String): Boolean {
    val cells = sheet.getRow(0).cellIterator()
    for (cell in cells) {
        if (checkCellToContain(cell, groupName)) {
            return true
        }
    }

    return false
}

/**
 * Checks sent [cell] to contain [target group][groupName].
 */
private fun checkCellToContain(cell: Cell, groupName: String): Boolean {
    val value = cell.stringCellValue
    return value.contains(groupName, true)
}
/* endregion */

/* region "searchTargetColumns" function */

/**
 * Searches target columns inside [sheet].
 * Target columns define by sent [group].
 *
 * Returns [list][List] with [data][ColumnBorders] about target columns.
 */
fun searchTargetColumns(sheet: Sheet, group: String): List<ColumnBorders> {
    val searchIndices = mutableListOf<ColumnBorders>()
    completeParseProcess(group, searchIndices, sheet)

    return searchIndices
}

/**
 * Parses current [sheet] to find target columns.
 *
 * When the target column is found, it writes info about their borders.
 */
private fun completeParseProcess(group: String, searchIndices: MutableList<ColumnBorders>, sheet: Sheet) {
    val listenToCycleEnding = AtomicBoolean(false)
    val cells = sheet.getRow(0).cellIterator()
    for (cell in cells) {
        parseCellAndUpdateIndices(group, cell, listenToCycleEnding, searchIndices)
    }

    // Most likely, we complete cycle before write last value.
    if (searchIndices[searchIndices.size - 1].rightBorderIndex == null) {
        searchIndices[searchIndices.size - 1].rightBorderIndex = sheet.getRow(0).lastCellNum.toInt()
    }
}

/**
 * Parses [cell] and updates info about border indices.
 */
private fun parseCellAndUpdateIndices(group: String, cell: Cell, listenToCycleEnding: AtomicBoolean,
                                      searchIndices: MutableList<ColumnBorders>) {
    val value = cell.stringCellValue
    if (value.lowercase() == group.lowercase()) {
        listenToCycleEnding.set(true)

        searchIndices.add(ColumnBorders(null, null))
        searchIndices[searchIndices.size - 1].leftBorderIndex = getLeftIndex(cell)
    }
    else if (listenToCycleEnding.acquire && checkCellContinuousState(cell, value)) {
        listenToCycleEnding.set(false)

        searchIndices[searchIndices.size - 1].rightBorderIndex = cell.columnIndex
    }
}

/**
 * Get left border index of given [cell].
 */
private fun getLeftIndex(cell: Cell): Int {
    return if (cell.columnIndex > 0) cell.columnIndex - 1
    else 0
}

/**
 * Checks if [cell][currentCell] with given [value] is continuous.
 */
private fun checkCellContinuousState(currentCell: Cell, value: String): Boolean {
    return currentCell.cellStyle.wrapText ||
            (value != Reader.UNITED_CELL_VALUE && value != Reader.EMPTY_CELL_VALUE)
}
/* endregion */

/* region "searchDaysCoordinates" function */

/**
 * Searches [coordinates][DayColumnInfo] of day declarations inside a document.
 * Returns list with these objects.
 */
fun searchDaysCoordinates(sheet: Sheet): List<DayColumnInfo> {
    val rows = sheet.rowIterator()
    val toReturn = mutableListOf<DayColumnInfo>()
    for (row in rows) {
        val cells = row.cellIterator()
        for (cell in cells) {
            val parseResult = parseCellToFindDayCoordinates(cell)
            if (parseResult != null) {
                toReturn.add(DayColumnInfo(parseResult.coordinates.column, parseResult.coordinates.row,
                                           parseResult.currentDay))
            }
        }
    }

    return toReturn
}

/**
 * Parses [cell] to find [day coordinates][DayColumnInfo].
 */
private fun parseCellToFindDayCoordinates(cell: Cell, verbose: Boolean = false): DayColumnInfo? {
    // Info: If we try to get a string value from non-string cell, we'll get an exception.
    try {
        val stringValue = cell.stringCellValue
        val dayValue = getDayValueFromString(stringValue)
        if (dayValue != null) {
            return DayColumnInfo(cell.address.column, cell.address.row, dayValue)
        }
    }
    catch (ex: IllegalStateException) {
        if (verbose) println("\n\nFind *non-string* cell inside document.\nOccurred error: $ex.")
    }

    return null
}
/* endregion */
