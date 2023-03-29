package model.data.schedule.common.origin.day

import model.data.change.day.TargetedChangesOfDay
import model.data.schedule.base.Lesson
import model.data.schedule.base.day.Day
import model.data.schedule.common.result.day.TargetedFinalDaySchedule
import model.data.schedule.common.result.day.builder.TargetedDayScheduleResultBuilder
import java.util.*


/**
 * Class, that represents one-day schedule.
 */
class TargetedDaySchedule(val day: Day, val lessons: MutableList<Lesson>) {

    /* region Constructors */

    /**
     * Initializes a new instance of TargetedDaySchedule only with given [day].
     * [lessons] initializes with an [empty list][mutableListOf].
     *
     * Uses by [] [manual extraction][] sub-functions.
     */
    constructor(day: Day) : this(day, mutableListOf())

    /**
     * Todo: Write docs.
     */
    constructor(day: Day, lessons: MutableList<Lesson>, isAllChanged: Boolean) : this(day, lessons) {
        lessons.forEach { lesson -> lesson.isChanged = isAllChanged }
    }
    /* endregion */

    /* region Functions */

    fun buildFinalSchedule(changes: TargetedChangesOfDay?): TargetedFinalDaySchedule {
        val builder = TargetedDayScheduleResultBuilder()
        builder.setSchedule(this)
        builder.setChanges(changes)
        builder.setTargetGroup(changes?.targetGroup)
        builder.setDate(changes?.changesDate)

        builder.bakeFinalSchedule()
        return builder.build()
    }

    fun buildFinalSchedule(targetGroup: String, targetDate: Calendar): TargetedFinalDaySchedule {
        val builder = TargetedDayScheduleResultBuilder()
        builder.setSchedule(this)
        builder.setTargetGroup(targetGroup)
        builder.setDate(targetDate)

        builder.bakeFinalSchedule()
        return builder.build()
    }

    /**
     * Fills all missing values (that called 'Empty Lessons') and return new schedule with filled values.
     * Base schedule is current object schedule.
     *
     * All non-empty lessons left not changed.
     */
    fun fillEmptyLessons(isAllChanged: Boolean): TargetedDaySchedule {
        val newSchedule = TargetedDaySchedule(day, lessons)
        for (i in 0..6) {
            var missing = true
            if (newSchedule.lessons.any { l -> l.number == i }) missing = false

            if (missing) {
                val newLesson = Lesson(i)
                newLesson.isChanged = isAllChanged

                newSchedule.lessons.add(newLesson)
            }
        }

        return newSchedule.restoreNaturalOrder()
    }

    /**
     * Restores a natural order of lessons in schedule (sorted by [Lesson Number][Lesson.number] property).
     *
     * Creates a new object in a process of work.
     * Yeah, to prevent troubles with links.
     */
    private fun restoreNaturalOrder(): TargetedDaySchedule {
        val newSchedule = TargetedDaySchedule(day, lessons)
        newSchedule.lessons.sort()

        return newSchedule
    }

    /**
     * Create a string representation of the schedule.
     * Overrides basic method.
     */
    override fun toString(): String {
        val toReturnBuilder: StringBuilder = StringBuilder(String.format(STRING_HEADER_TEMPLATE, day.englishName))
        for (lesson in lessons) {
            val formatString = String.format(STRING_BODY_TEMPLATE, lesson.number, lesson.name, lesson.place,
                                             lesson.teacher)
            toReturnBuilder.append(formatString)
        }

        return toReturnBuilder.toString()
    }
    /* endregion */

    /* region Companion */

    companion object {

        /* region Constants */

        /**
         * Template to generate a head of string representation of [TargetedDaySchedule] object.
         *
         * Use it with [String.format] function.
         */
        const val STRING_HEADER_TEMPLATE = "%s:\n"

        /**
         * Template to generate string representation of [TargetedDaySchedule] object.
         *
         * Use it with [String.format] function.
         */
        const val STRING_BODY_TEMPLATE =
            """
				{
					Lesson number: %d;
					Lesson name: %s;
					Place: %s;
					Teacher: %s;
                    IsChanged: %b.
				}
			"""
        /* endregion */
    }
    /* endregion */
}
