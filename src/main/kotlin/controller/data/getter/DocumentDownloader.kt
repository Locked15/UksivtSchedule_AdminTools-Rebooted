package controller.data.getter

import controller.view.Logger
import model.environment.log.LogLevel
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URI


/**
 * Rewritten on Kotlin.
 *
 * Base version in Java has been splatted into two classes:
 * DataGetter -> DocumentDownloader, SiteParser.
 * ```
 * Class, intended to get documents from college official site for parse it.
 * Contains base original logic of document parsing and must be saved.
 * ```
 *
 * @author Locked15.
 */
@Suppress("Unused")
class DocumentDownloader {

	/* region Functions */
	
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
	 * Downloads a file from url to the targeted directory.
	 *
	 * @param url Ready-To-Download url with a document.
	 * @param path Path to save a downloaded document.
	 * @return Result of document download.
	 */
	fun downloadGoogleFile(url: String, path: String): Boolean {
		return try {
			FileUtils.copyURLToFile(URI.create(url).toURL(), File(path))
			true
		}
		catch (e: MalformedURLException) {
			Logger.logMessage(LogLevel.ERROR, "Sent URL was wrong", 1)
			false
		}
		catch (e: IOException) {
			Logger.logException(e, 1, "On document download error occurred")
			false
		}
	}
	/* endregion */
	
	/* region Companion */
	
	companion object {
		
		/**
		 * A template for a download link to a document on Google Drive.
		 */
		const val GOOGLE_DRIVE_LINK_TEMPLATE = "https://drive.google.com/uc?export=download&id="
	}
	/* endregion */
}
