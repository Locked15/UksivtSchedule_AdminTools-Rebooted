package dataGetter

import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.net.MalformedURLException
import java.net.URL

/**
 * Rewritten on Kotlin.
 * ```
 * Class, intended to get documents from college official site for parse it.
 * Contains base original logic of document parsing and must be saved.
 * ```
 *
 * @author Locked15.
 */
@Suppress("Unused")
class DataGetter {
	
	// region Class Data
	
	/**
	 * Object with static data of this class.
	 */
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
		 * A template for a download link to a document on Google Drive.
		 */
		const val GOOGLE_DRIVE_LINK_TEMPLATE = "https://drive.google.com/uc?export=download&id="
		
		/**
		 * A CSS selector to the section with documents on the college official site.
		 */
		const val CSS_SELECTOR_TO_DOCUMENTS_SECTION = "section > div > div > div > div > div > div " +
			"> div > div > div > div > div > div > div > div > div > div > div"
	}
	
	/**
	 * WebPage, connected to college official site.
	 */
	private val webPage: Document?
	// endregion
	
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
	
	// region Functions
	
	/**
	 * Merge an original document link and Google Drive a template link.
	 * Returned link can be used to download a changes document.
	 *
	 * @param linkToFile Original link to the document.
	 * @return Ready-To-Download link to the document.
	 */
	fun getDownloadableLinkToFileWithChanges(linkToFile: String): String {
		var newLink = linkToFile.substring(0, linkToFile.lastIndexOf('/'))
		newLink = newLink.substring(linkToFile.lastIndexOf('/') + 1)
		
		return "$GOOGLE_DRIVE_LINK_TEMPLATE $newLink"
	}
	
	/**
	 * Downloads a file with changes from url to the targeted directory.
	 *
	 * @param url Ready-To-Download url with a changes document.
	 * @param path Path to save a downloaded document.
	 * @return Result of document download.
	 */
	fun downloadFileWithChanges(url: String, path: String): Boolean {
		try {
			FileUtils.copyURLToFile(URL(url), File(path))
			
			return true
		}
		catch (e: MalformedURLException) {
			println("Send URL was wrong: $url.")
		}
		catch (e: IOException) {
			println("In download process an error occurred: ${e.message}.")
		}
		
		return false
	}
	//endregion
}
