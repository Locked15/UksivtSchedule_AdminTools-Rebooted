package model.data.parse.site.wrapper

import model.data.parse.site.ChangeElement
import model.data.parse.site.MonthChanges
import controller.data.getter.SiteParser


/**
 * Base model for site [parsing function][SiteParser.getAvailableNodes].
 *
 * @param dayOfMonthCounter  Outer cycle iterator (used for identify month day number).
 * @param monthCounter  Second cycle iterator (used for identify month number).
 * @param currentMonth  Identifies name of current month.
 * @param changes  List with targetedChangesOfDay of current month.
 *                 It has late-cycle initialization (when variable saves value from the past iteration),
 *                 so it must save value between iterations and placed outside all blocks.
 * @param monthChanges  List of month-scoped targetedChangesOfDay. It's return value of function.
 */
class BaseIteratorModel(var dayOfMonthCounter: Int, var monthCounter: Int, var currentMonth: String,
                        var changes: MutableList<ChangeElement>, val monthChanges: MutableList<MonthChanges>)

