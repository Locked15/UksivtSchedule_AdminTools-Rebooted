package controller.data.reader.excel

import model.document.ColumnBorders
import model.document.DayColumnInfo
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFTable
import java.util.concurrent.atomic.AtomicBoolean
import model.element.schedule.base.day.fromString as getDayValueFromString


fun searchTargetSheet(book: XSSFWorkbook, groupName: String): Sheet? {
    val sheets = book.sheetIterator()
    for (sheet in sheets) {
        if (checkSheetToContain(sheet, groupName)) {
            return sheet
        }
    }

    return null
}

private fun checkSheetToContain(sheet: Sheet, groupName: String): Boolean {
    val cells = sheet.getRow(0).cellIterator()
    for (cell in cells) {
        if (checkCellToContain(cell, groupName)) {
            return true
        }
    }

    return false
}

private fun checkCellToContain(cell: Cell, groupName: String): Boolean {
    val value = cell.stringCellValue.lowercase()

    return value == groupName
}

fun searchTargetColumns(sheet: Sheet, group: String): List<ColumnBorders> {
    val searchIndices = mutableListOf<ColumnBorders>()
    completeParseProcess(group, searchIndices, sheet)

    return searchIndices
}

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

private fun getLeftIndex(cell: Cell): Int {
    return if (cell.columnIndex > 0) cell.columnIndex - 1
    else 0
}

private fun checkCellContinuousState(currentCell: Cell, value: String): Boolean {
    return currentCell.cellStyle.wrapText ||
            (value != Reader.UNITED_CELL_VALUE && value != Reader.EMPTY_CELL_VALUE)
}

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

private fun parseCellToFindDayCoordinates(cell: Cell): DayColumnInfo? {
    // Info: If we try to get a string value from non-string cell, we'll get an exception.
    try {
        val stringValue = cell.stringCellValue
        val dayValue = getDayValueFromString(stringValue)
        if (dayValue != null) {
            return DayColumnInfo(cell.address.column, cell.address.row, dayValue)
        }
    }
    catch (ex: IllegalStateException) {
        println("\n\nFind *non-string* cell inside document.\nOccurred error: $ex.")
    }

    return null
}

fun checkThatTableIsCorrect(table: XWPFTable): Boolean {
    val header = table.text

    /* Change table always contains some of these values, so check it.
       And, as you know, sometimes a changes document contains more than one table. */
    return firstCheckPart(header) || secondCheckPart(header) || thirdCheckPart(header)
}

private fun firstCheckPart(header: String): Boolean = header.contains("группа", true) &&
        header.contains("заменяемая дисциплина", true)

private fun secondCheckPart(header: String): Boolean = header.contains("заменяемый преподаватель", true) &&
        header.contains("заменяющая дисциплина", true)

private fun thirdCheckPart(header: String): Boolean = header.contains("заменяющий преподаватель", true) &&
        header.contains("ауд", true)
