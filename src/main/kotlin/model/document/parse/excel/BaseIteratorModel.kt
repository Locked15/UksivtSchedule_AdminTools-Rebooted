package model.document.parse.excel

import model.document.ColumnBorders
import model.document.DayColumnInfo
import model.element.schedule.DaySchedule


/**
 * Wrapper-Model for Excel document parse.
 * These properties must be placed outside all cycles.
 *
 * @param schedules List with filled schedules for week days.
 * @param dayColumnsIndices List with indices of columns, that contains day declarations.
 * @param targetBorders List with indices of target columns, that contains schedule info for the target group.
 */
class BaseIteratorModel(val schedules: MutableList<DaySchedule>, val dayColumnsIndices: List<DayColumnInfo>,
                        val targetBorders: List<ColumnBorders>)
