package model.data.schedule.origin

import model.data.changes.TargetedChangesOfDay
import model.data.schedule.base.Lesson
import model.data.schedule.base.day.Day


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
    /* endregion */

    /* region Functions */

    /**
     * Merge current schedule with [given changes][changes].
     * If sent changes are 'NULL', returns a [new object][TargetedDaySchedule] with the same schedule as current.
     *
     * Before calling this method check, that schedule is full (with empty lessons).
     * If you don't make it, the final schedule can be messed up (changes placed at the end of order).
     */
    fun mergeWithChanges(changes: TargetedChangesOfDay?): TargetedDaySchedule {
        val mergedSchedule = lessons.toMutableList()
        return if (changes == null) {
            println("Found 'NULL' changes value on schedule merging. Base schedule will return.\nSkipping...")
            TargetedDaySchedule(day, lessons)
        }
        else if (changes.isAbsolute) {
            println("Absolute changes found on schedule merging. New schedule will be applied.\nCalculation...")
            TargetedDaySchedule(day, changes.changedLessons).fillEmptyLessons()
        }
        else {
            for (lesson in changes.changedLessons) {
                if (lesson.name?.lowercase() == "нет") lesson.name = null
                else if (lesson.number == null) println("Found 'NULL' lesson number.\nUsed default (0) value.")

                try {
                    mergedSchedule[lesson.number ?: 0] = lesson
                }
                catch (exception: IndexOutOfBoundsException) {
                    mergedSchedule.add(Lesson(lesson.number, lesson.name, lesson.teacher, lesson.place))
                    println("While merging schedule with changes found missing lesson...")
                }
            }

            TargetedDaySchedule(day, mergedSchedule)
        }
    }

    /**
     * Fills all missing values (that called 'Empty Lessons') and return new schedule with filled values.
     * Base schedule is current object schedule.
     *
     * All non-empty lessons left not changed.
     */
    private fun fillEmptyLessons(): TargetedDaySchedule {
        val newSchedule = TargetedDaySchedule(day, lessons)
        for (i in 0..6) {
            var missing = true
            if (newSchedule.lessons.any { l -> l.number == i }) missing = false

            if (missing) newSchedule.lessons.add(Lesson(i))
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
					Teacher: %s.
				}
			"""
        /* endregion */

        /* region Static Functions */

        /**
         * Generates a template schedule for practise days.
         *
         * Return value contains seven lessons (0..6), with 'Практика' value in lessons name.
         */
        fun getOnPractiseSchedule(day: Day): TargetedDaySchedule {
            val lessons = mutableListOf<Lesson>()
            for (i in 0..6) {
                lessons.add(Lesson(i, "Практика"))
            }

            return TargetedDaySchedule(day, lessons)
        }

        /**
         * Generates a template schedule for debt liquidation days.
         *
         * Return value contains seven lessons (0..6), with 'Ликвидация Задолженностей' value in lessons name.
         */
        fun getDebtLiquidationSchedule(day: Day): TargetedDaySchedule {
            val lessons = mutableListOf<Lesson>()
            for (i in 0 .. 6) {
                lessons.add(Lesson(i, "Ликвидация Задолженностей"))
            }

            return TargetedDaySchedule(day, lessons)
        }
        /* endregion */
    }
    /* endregion */
}