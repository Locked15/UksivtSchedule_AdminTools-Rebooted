package model.data.parse.schedule.wrapper

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import model.data.schedule.base.Lesson


/**
 * Wrapper-Model for Excel document parse.
 * These properties must be placed inside the second cycle.
 *
 * @param lessonNameIsAdded  Determine [lesson name][Lesson.name] is added to a current generating [lesson][Lesson].
 * @param lessonPlaceIsAdded  Determine [lesson place][Lesson.place] is added to a current generating [lesson][Lesson].
 * @param dayEndingRow  Contains position of the current day final row.
 *                      Wrapped from long expression to prettify source code.
 * @param cells  Contains [all][Iterator] [cells][Cell] inside [current row][Row].
 *               Simplifies work with current row cells (and probably save memory).
 */
class InnerIteratorModel(var lessonNameIsAdded: Boolean, var lessonPlaceIsAdded: Boolean, val dayEndingRow: Int?,
                         val cells: Iterator<Cell>)
