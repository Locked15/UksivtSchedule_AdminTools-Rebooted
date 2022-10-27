package model.site.parse

import model.element.parse.ChangeElement
import model.element.parse.MonthChanges
import controller.data.getter.SiteParser


/**
 * Base model for site [parsing function][SiteParser.getAvailableNodes].
 *
 * @param dayCounter  Outer cycle iterator (used for identify day number).
 * @param monthCounter  Second cycle iterator (used for identify month number).
 * @param currentMonth  Identifies name of current month.
 * @param changes  List with changes of current month.
 *                 It has late-cycle initialization (when variable saves value from the past iteration),
 *                 so it must save value between iterations and placed outside all blocks.
 * @param monthChanges  List of month-scoped changes. It's return value of function.
 */
class BaseIteratorModel(var dayCounter: Int, var monthCounter: Int, var currentMonth: String,
                        var changes: MutableList<ChangeElement>, val monthChanges: MutableList<MonthChanges>)
