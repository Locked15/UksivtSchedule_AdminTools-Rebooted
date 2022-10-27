package controller.data.getter

import model.element.parse.MonthChanges
import model.site.parse.BaseIteratorModel
import model.site.parse.ChangeElementsWrapper
import org.jsoup.Connection.Base
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.lang.Exception


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

	fun getAvailableNodes(months: Int = 2): MutableList<MonthChanges> {
		val base = BaseIteratorModel(0, 0, "Январь", mutableListOf(), mutableListOf())
		if (webPage != null) {
			val elements = ChangeElementsWrapper(webPage.getElementById(PATH_BEGIN_ELEMENT_ID)!!
                                                     .select(CSS_SELECTOR_TO_DOCUMENTS_SECTION)[0],
												 webPage.getElementById(PATH_BEGIN_ELEMENT_ID)!!
                                                     .select(CSS_SELECTOR_TO_DOCUMENTS_SECTION)[0]
                                                     .children())
            for (element in elements.listOfMonthChangesElements) {
                calculateNewMonthBeginning(element, base)
                if (element.nodeName() == "table" && base.monthCounter < months) {

                }
            }
		}

		return base.monthChanges
	}

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
            model.dayCounter = 1
        }
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
