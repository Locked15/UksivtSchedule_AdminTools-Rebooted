package controller.data.reader.word

import model.document.parse.word.iterator.BaseIteratorModel
import model.document.parse.word.iterator.InnerIteratorModel
import model.document.parse.word.iterator.OuterIteratorModel
import org.apache.poi.xwpf.usermodel.XWPFTable


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

fun checkToParsingStopper(base: BaseIteratorModel, outer: OuterIteratorModel, inner: InnerIteratorModel,
                          target: String): Boolean {
    val metAnotherGroupWhileParseChanges = firstStopperCheckPart(base.listenToChanges, inner.text, target)
    val anotherGroupNameMetOnSpecifiedCell = secondStopperCheckPart(outer.cellNumber)

    return metAnotherGroupWhileParseChanges && anotherGroupNameMetOnSpecifiedCell
}

private fun firstStopperCheckPart(listen: Boolean, foundText: String, target: String): Boolean = listen &&
        foundText.lowercase() == target.lowercase()

private fun secondStopperCheckPart(cellNumber: Int): Boolean = cellNumber == 0 ||
        cellNumber == 3
