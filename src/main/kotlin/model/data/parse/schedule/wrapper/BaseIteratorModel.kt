package model.data.parse.schedule.wrapper

import model.data.parse.schedule.ColumnBorders
import model.data.parse.schedule.DayColumnInfo
import model.data.schedule.common.origin.day.TargetedDaySchedule


/**
 * Wrapper-Model for Excel document parse.
 * These properties must be placed outside all cycles.
 *
 * @param schedules List with filled schedules for week days.
 * @param dayColumnsIndices List with indices of columns, that contains day declarations.
 * @param targetBorders List with indices of target columns, that contains schedule info for the target group.
 */
class BaseIteratorModel(val schedules: MutableList<TargetedDaySchedule>, val dayColumnsIndices: List<DayColumnInfo>,
                        val targetBorders: List<ColumnBorders>)
