package controller.data.getter

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
	
	fun getAvailableNodes(): Unit {
	
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
		 * A CSS selector to the section with documents on the college official site.
		 */
		const val CSS_SELECTOR_TO_DOCUMENTS_SECTION = "section > div > div > div > div > div > div " +
			"> div > div > div > div > div > div > div > div > div > div > div"
	}
	/* endregion */
}
