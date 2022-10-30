package model.data.parse.site.wrapper

import controller.data.getter.SiteParser
import model.data.schedule.base.day.Day

/**
 * Wrapper class for inner iteration of [site parsing function][SiteParser.getAvailableNodes].
 * Use to minify code.
 *
 * @param dayOfWeekCounter  Current day of week ID. Can be used to evaluate [Day] value by itself.
 * @param isFirstIteration  Define when iteration found an end of the month and re-begins an iteration process.
 */
class InnerIteratorModel(var dayOfWeekCounter: Int, var isFirstIteration: Boolean)

