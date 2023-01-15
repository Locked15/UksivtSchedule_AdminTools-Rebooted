package model.data.parse.schedule.wrapper

import model.data.schedule.base.Lesson
import model.data.schedule.base.day.Day
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import model.data.schedule.WeekSchedule
import model.data.schedule.DaySchedule


/**
 * Wrapper-Model for Excel document parse.
 * These properties must be placed inside the first cycle.

 * @param lessonNameIsAdded  Determine [lesson name][Lesson.name] is added to a current generating [lesson][Lesson].
 * @param lessonPlaceIsAdded  Determine [lesson place][Lesson.place] is added to a current generating [lesson][Lesson].
 * @param lessonIterator  It used inside the second cycle and must save value between iterations.
 *                       Used to define a lesson-change statement.
 *                       **Prior 'firstCycleIterator' variable**.
 * @param dayIterator  It used inside the second cycle and must save value between iterations.
 *                        Contains index of the current day and used in the corresponding statements.
 *                        **Prior 'i' variable**.
 * @param lessonNumber  Used to initialize lessons (represents number of the future lessons).
 *                      TargetChangesOfDay between iterations of the second cycle and must save itself value.
 * @param lesson  Lesson, that will be added to a [schedule collection][DaySchedule], then to [WeekSchedule] object.
 *                It placed inside the first cycle, cause some fields initializes on the second iteration.
 * @param currentDay  Contains a current day object. Set 'NULL', if something went wrong (or it's the first iteration).
 * @param lessons  Collection of lessons, that will be constructed to the [final object][DaySchedule].
 *                 Placed inside the first cycle and resets value every iteration.
 * @param rows  Iterator of [Row] objects, that contains all rows of the [document][XSSFWorkbook].
 *              Iteration process repeated two times, cause schedule divided on two parts:
 *              Monday -> Wednesday, Thursday -> Saturday.
 */
class OuterIteratorModel(var lessonNameIsAdded: Boolean, var lessonPlaceIsAdded: Boolean, var lessonIterator: Int,
                         var dayIterator: Int, var lessonNumber: Int, var lesson: Lesson, var currentDay: Day?,
                         var lessons: MutableList<Lesson>, val rows: Iterator<Row>)
