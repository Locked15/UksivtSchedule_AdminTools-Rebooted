package controller.data.getter

import model.data.parse.site.ChangeElement
import model.data.parse.site.MonthChanges
import model.data.schedule.base.day.Day
import model.data.parse.site.wrapper.BaseIteratorModel
import model.data.parse.site.wrapper.ChangeElementsWrapper
import model.data.parse.site.wrapper.InnerIteratorModel
import model.data.parse.site.wrapper.TableContentWrapper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.lang.Exception


/**
 * Site parser class.
 *
 * Its functionality allows scrapping [college website](https://www.uksivt.ru) [page](https://www.uksivt.ru/zameny)
 * and [extracting links][ChangeElement.linkToDocument] to change documents.
 *
 * Extracted links [boxed inside][ChangeElement] [objects][MonthChanges] with side-information
 * ([day][ChangeElement.dayOfWeek],
 * [month][ChangeElement.dayOfMonth],
 * [etc][MonthChanges.currentMonth]).
 */
class SiteParser {

	/* region Properties */

	/**
	 * WebPage, connected to college official site.
	 */
	private val webPage: Document?
	/* endregion */

	/* region Initializers */

	/**
	 * Tries to connect to the site of college, and if connection is successful, write loaded DOM to field.
	 * Otherwise, write 'NULL'.
	 */
	init {
		var document: Document? = null

		try {
			document = Jsoup.connect(COLLEGE_CHANGES_PAGE_PATH).get()
		}
		catch (e: Exception) {
			document = null
			println("In connection process, an error occurred: ${e.message}.")
		}
		finally {
			webPage = document
		}
	}
	/* endregion */

	/* region Functions */

	/**
	 * Parses college [website](https://uksivt.ru) [page](https://uksivt.ru/zameny)
	 * and seeks all available [changes nodes][ChangeElement].
	 *
	 * It parses [limited count][months] of [months][MonthChanges].
	 *
	 * Some Info:
	 * This function has been written in 2021 year, so it may not include last edits of the site.
	 * Please [tell](mailto:almgal353@gmail.com) [me](https://bit.ly/3WbRlMn)
	 * if something [WENT][Exception] [WRONG][Error] on a parsing process.
	 */
	fun getAvailableNodes(months: Int = 2): MutableList<MonthChanges> {
		val base = BaseIteratorModel(0, 0, "Январь", mutableListOf(), mutableListOf())
		if (webPage != null) {
			val elements = executePageQueriesAndGetElements()
            for (element in elements.listOfMonthChangesElements ?: emptyList()) {
                calculateNewMonthBeginning(element, base)
                if (element.nodeName() == "table" && base.monthCounter < months) {
					val table = TableContentWrapper(element.children()[0], element.children()[1],
													element.children()[1].children())
	                for (row in table.rows) {
						val innerIterator = InnerIteratorModel(0, false)
		                if (row == table.rows.first()) continue

		                iterateAndReadRowCells(row, base, innerIterator)
	                }
	                increaseMonth(base)
                }
            }
		}

		return base.monthChanges
	}

	/**
	 * Executes page queries (like CSS-Selector query) and extracts changes container children.
	 * Then packs it into a [new object][ChangeElementsWrapper] and returns.
	 */
	private fun executePageQueriesAndGetElements(): ChangeElementsWrapper {
		val containerElement = webPage?.getElementById(PATH_BEGIN_ELEMENT_ID)
			?.select(CSS_SELECTOR_TO_DOCUMENTS_SECTION)
			?.get(0)
		val contentElement = containerElement?.children()

		return ChangeElementsWrapper(containerElement, contentElement)
	}

	/**
	 * Evaluates expressions of a new month beginning.
	 * Updates relative objects and saves values.
	 *
	 * Also called on the first iteration,
	 * to initialize [some][BaseIteratorModel.changes] [properties][BaseIteratorModel.currentMonth].
	 */
    private fun calculateNewMonthBeginning(element: Element, model: BaseIteratorModel) {
        /* Month-scoped elements always lie inside <p> tag, but in the beginning of the page
           it is space, created by the same tag.
           Also, it contains non-common space (non-breaking one), so we need to check it. */
        if (element.nodeName() == "p" && element.text() != NON_BREAKING_SPACE) {
            // On first iteration we also go here, so we need to check it.
            if (model.changes.isNotEmpty()) model.monthChanges.add(MonthChanges(model.currentMonth, model.changes))

            // Then we just reload the content of other properties.
            model.currentMonth = element.text().substring(0, element.text()
                .lastIndexOf(' '))
                .replace(NON_BREAKING_SPACE, "")
            model.changes = mutableListOf()
            model.dayOfMonthCounter = 1
        }
    }

	/**
	 * Iterates [current row][row] [cells][List] and updates [related][base] [objects][inner].
	 * Cell content parses by [another function][parseCellValueAndUpdateObjects].
	 */
	private fun iterateAndReadRowCells(row: Element, base: BaseIteratorModel, inner: InnerIteratorModel) {
		// All rows after the first, contains target information.
		for (cell in row.children()) {
			if (checkFirstCellAndUpdateObjects(cell, inner)) continue
			else parseCellValueAndUpdateObjects(cell, base, inner)

			increaseIterators(base, inner)
		}
	}

	/**
	 * Checks first row cell and updates objects.
	 *
	 * The first cell contains a non-breaking space for indentation goal, so we'll have to skip it.
	 * If new month begins on non-monday, so we'll have to skip more than one cell.
	 */
	private fun checkFirstCellAndUpdateObjects(cell: Element, iterator: InnerIteratorModel): Boolean {
		var result = false
		if (cell.text() == NON_BREAKING_SPACE) {
			// If we don't meet at least one day, we'll continue to skip cells.
			if (!iterator.isFirstIteration) {
				iterator.dayOfWeekCounter++
			}
			// So, to skip some cells, we declare value and continue the cycle.
			iterator.isFirstIteration = false
			result = true
		}

		return result
	}

	/**
	 * Parses cell value, and, if it contains something, writes it to a [related][model] [objects][inner].
	 * Although it writes empty value, that can be skipped later.
	 */
	private fun parseCellValueAndUpdateObjects(cell: Element, model: BaseIteratorModel, inner: InnerIteratorModel) {
		if (cell.children().size < 1) {
			model.changes.add(ChangeElement(Day.getValueByIndex(inner.dayOfWeekCounter), -1, null))
		}
		else {
			val link = cell.children().first()
			model.changes.add(ChangeElement(Day.getValueByIndex(inner.dayOfWeekCounter), model.dayOfMonthCounter,
											link?.attr("href")))
		}
	}

	/**
	 * Increases month counter.
	 * If the current month is december (12), sets it to january (1).
	 *
	 * Nor more, nor less.
	 */
	private fun increaseMonth(base: BaseIteratorModel) {
		if (base.monthCounter < 12) base.monthCounter++
		else base.monthCounter = 1
	}

	/**
	 * Increases common [iterator][BaseIteratorModel.dayOfMonthCounter] [values][InnerIteratorModel.dayOfWeekCounter].
	 *
	 * Nor more, nor less.
	 */
	private fun increaseIterators(base: BaseIteratorModel, inner: InnerIteratorModel) {
		base.dayOfMonthCounter++
		inner.dayOfWeekCounter++
	}
	/* endregion */

	/* region Companion */

	companion object {

		/**
		 * A non-breaking space.
		 */
		const val NON_BREAKING_SPACE = "\u00A0"

		/**
		 * Path to official college website, targeted to changes page.
		 */
		const val COLLEGE_CHANGES_PAGE_PATH = "https://www.uksivt.ru/zameny"

		/**
		 * ID of the element that used as begin of CSS-Selector selection path.
		 */
		const val PATH_BEGIN_ELEMENT_ID = "inside-page"

		/**
		 * A CSS selector to the section with documents on the college official site.
		 */
		const val CSS_SELECTOR_TO_DOCUMENTS_SECTION = "section > div > div > div > div > div > div " +
			"> div > div > div > div > div > div > div > div > div > div > div"
	}
	/* endregion */
}
