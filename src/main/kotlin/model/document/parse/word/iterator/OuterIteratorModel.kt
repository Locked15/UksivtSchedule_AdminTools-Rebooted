package model.document.parse.word.iterator

import model.element.schedule.base.Lesson


/**
 * Represents a second iteration level (outer) data model of Word document parse.
 * This one must be placed inside the second cycle.
 *
 * @param cellNumber  Index of currently parsing cell.
 * @param rawLessonNumbers  Raw lesson numbers, that may contain more than one lesson number in one string.
 *                          Use *.expandWrappedLesson()* function to expand possible wrapped lessons.
 * @param currentLesson  Variable with a currently generating lesson.
 */
class OuterIteratorModel(var cellNumber: Int, var rawLessonNumbers: String, var currentLesson: Lesson)
