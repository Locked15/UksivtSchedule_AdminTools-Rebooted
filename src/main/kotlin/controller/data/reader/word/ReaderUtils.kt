package controller.data.reader.word

import model.data.parse.changes.wrapper.BaseIteratorModel
import model.data.parse.changes.wrapper.InnerIteratorModel
import model.data.parse.changes.wrapper.OuterIteratorModel
import org.apache.poi.xwpf.usermodel.XWPFTable
import java.util.Calendar


/* region Date Parsing sub-functions */

fun getMonthIndexByName(name: String): Int {
    return when (name.lowercase()) {
        // Winter is coming...
        "декабрь", "декабря" -> Calendar.DECEMBER
        "январь", "января" -> Calendar.JANUARY
        "февраль", "февраля" -> Calendar.FEBRUARY

        // Spring is outta here...
        "март", "марта" -> Calendar.MARCH
        "апрель", "апреля" -> Calendar.APRIL
        "май", "мая" -> Calendar.MAY

        // We have only dreams about Summer...
        "июнь", "июня" -> Calendar.JUNE
        "июль", "июля" -> Calendar.JULY
        "август", "августа" -> Calendar.AUGUST

        // It will end. We all are falling in the Fall...
        "сентябрь", "сентября" -> Calendar.SEPTEMBER
        "октябрь", "октября" -> Calendar.OCTOBER
        "ноябрь", "ноября" -> Calendar.NOVEMBER

        else -> throw IllegalArgumentException("Found invalid month name.\nOriginal value: $name.")
    }
}
/* endregion */

/* region "searchTargetTable" function */

fun searchTargetTable(tables: List<XWPFTable>): XWPFTable {
    for (table in tables) {
        if (checkTableIsCorrect(table)) {
            return table
        }
    }

    throw NoSuchElementException("Can't find target table.")
}

private fun checkTableIsCorrect(table: XWPFTable): Boolean {
    val header = table.text

    /* Change table always contains some of these values, so check it.
       And, as you know, sometimes a changes document contains more than one table. */
    return firstTableCheckPart(header) || secondTableCheckPart(header) || thirdTableCheckPart(header)
}

private fun firstTableCheckPart(header: String): Boolean = header.contains("группа", true) &&
        header.contains("заменяемая дисциплина", true)

private fun secondTableCheckPart(header: String): Boolean = header.contains("заменяемый преподаватель", true) &&
        header.contains("заменяющая дисциплина", true)

private fun thirdTableCheckPart(header: String): Boolean = header.contains("заменяющий преподаватель", true) &&
        header.contains("ауд", true)
/* endregion */

/* region "checkToParsingStopper" function */

fun checkToParsingStopper(base: BaseIteratorModel, outer: OuterIteratorModel, inner: InnerIteratorModel,
                          centeredCellId: Int, target: String): Boolean {
    val metAnotherGroupWhileParseChanges = checkCellToDifference(base.listenToChanges, inner.text, target)
    val anotherGroupNameMetOnSpecifiedCell = checkCellToBeDeclarationSpecificCell(outer.cellNumber, centeredCellId)

    return metAnotherGroupWhileParseChanges && anotherGroupNameMetOnSpecifiedCell
}

private fun checkCellToDifference(listen: Boolean, foundText: String, target: String): Boolean = listen &&
        foundText.isNotBlank() && foundText.lowercase() != target.lowercase()

fun checkCellToBeDeclarationSpecificCell(cellNumber: Int, centeredGroupNameCellId: Int): Boolean = cellNumber == 0 ||
        cellNumber == centeredGroupNameCellId
/* endregion */
