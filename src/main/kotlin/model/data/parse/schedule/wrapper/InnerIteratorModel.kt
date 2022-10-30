package model.data.parse.schedule.wrapper

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row


/**
 * Wrapper-Model for Excel document parse.
 * These properties must be placed inside the second cycle.
 *
 * @param dayEndingRow  Contains position of the current day final row.
 *                      Wrapped from long expression to prettify source code.
 * @param cells  Contains [all][Iterator] [cells][Cell] inside [current row][Row].
 *               Simplifies work with current row cells (and probably save memory).
 */
class InnerIteratorModel(val dayEndingRow: Int?, val cells: Iterator<Cell>)
