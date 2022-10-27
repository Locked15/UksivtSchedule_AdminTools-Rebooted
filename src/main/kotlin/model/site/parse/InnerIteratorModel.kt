package model.site.parse

import controller.data.getter.SiteParser


/**
 * Wrapper class for inner iteration of [site parsing function][SiteParser.getAvailableNodes].
 * Use to minify code.
 *
 * @param dayCounter  The number of current parsing day.
 * @param isFirstIteration  Define when iteration found an end of the month and re-begins iteration process.
 */
class InnerIteratorModel(var dayCounter: Int, var isFirstIteration: Boolean)
