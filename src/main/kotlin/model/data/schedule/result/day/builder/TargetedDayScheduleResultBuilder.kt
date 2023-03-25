package model.data.schedule.result.day.builder

import model.data.change.day.TargetedChangesOfDay
import model.data.schedule.base.Lesson
import model.data.schedule.origin.day.TargetedDaySchedule
import model.data.schedule.result.day.TargetedDayScheduleResult
import java.util.*


/**
 * Resulting class of merging basic schedule to the changes (AKA replacements).
 * This is builder for resulting class.
 *
 * I created this builder, to insert new functionality softly, without changing
 * [existing][TargetedDaySchedule.buildFinalSchedule] [logic][controller.data.reader.word.Reader.getChangedSchedule].
 * It's not fluent builder by the way.
 */
class TargetedDayScheduleResultBuilder {

    /* region Properties */

    private var date: Calendar? = null

    private var targetGroup: String? = null

    private var schedule: TargetedDaySchedule? = null

    private var changes: TargetedChangesOfDay? = null
    /* endregion */

    /* region Constructors */

    constructor() {
        // It's used to keep default class constructor.
    }

    constructor(schedule: TargetedDaySchedule) {
        setSchedule(schedule)
    }
    /* endregion */

    /* region Setters Functions */

    fun setDate(date: Calendar?): TargetedDayScheduleResultBuilder {
        this.date = date
        return this
    }

    fun setTargetGroup(target: String?): TargetedDayScheduleResultBuilder {
        targetGroup = target
        return this
    }

    fun setSchedule(schedule: TargetedDaySchedule): TargetedDayScheduleResultBuilder {
        this.schedule = schedule
        return this
    }

    fun setChanges(changes: TargetedChangesOfDay?): TargetedDayScheduleResultBuilder {
        this.changes = changes
        return this
    }
    /* endregion */

    /* region Building Functions */

    fun bakeFinalSchedule(): TargetedDayScheduleResultBuilder {
        schedule = mergeWithChanges()
        return this
    }

    fun build(): TargetedDayScheduleResult {
        setUpAdditionalValues()
        validateBuilderProperties()

        return TargetedDayScheduleResult(targetGroup, date, schedule!!)
    }
    /* endregion */

    /* region Side-Functions */

    private fun setUpAdditionalValues() {
        changes?.changesDate?.let { element ->
            setDate(element)
        }
        changes?.targetGroup?.let { element ->
            setTargetGroup(element)
        }
    }

    private fun validateBuilderProperties() {
        if ((schedule == null && (changes == null || !changes!!.isAbsolute))) {
            throw KotlinNullPointerException(
                    "You MUST set or default schedule OR absolute changes object.")
        }
        else if (date == null || targetGroup == null) {
            throw KotlinNullPointerException("You MUST set or changes object with all-available data (target, date)" +
                                                     "OR set this values manually.")
        }
    }
    /* endregion */

    /* region Former-Functions (from basic TargetedDaySchedule) */

    /**
     * Merge current schedule with [given changes][changes].
     * If sent changes are 'NULL', returns a [new object][TargetedDaySchedule] with the same schedule as current.
     *
     * Before calling this method check, that schedule is full (with empty lessons).
     * If you don't make it, the final schedule can be messed up (changes placed at the end of order).
     */
    private fun mergeWithChanges(): TargetedDaySchedule {
        schedule.let {
            val mergedSchedule = schedule!!.lessons.toMutableList()
            return if (changes == null) {
                // TODO: Make global app configuration to change showing warnings and errors logs.
                println("Found 'NULL' changes value on schedule merging. Base schedule will return.\nSkipping...")
                TargetedDaySchedule(schedule!!.day, schedule!!.lessons)
            }
            else if (changes!!.isAbsolute) {
                // ToDO: The same.
                println("Absolute changes found on schedule merging. New schedule will be applied.\nCalculation...")
                TargetedDaySchedule(schedule!!.day, changes!!.changedLessons, true).fillEmptyLessons(true)
            }
            else {
                for (lesson in changes!!.changedLessons) {
                    if (lesson.name?.lowercase() == "нет") {
                        lesson.name = null
                        lesson.isChanged = true
                    }
                    else if (lesson.number == null) println("Found 'NULL' lesson number.\nUsed default (0) value.")

                    try {
                        mergedSchedule[lesson.number ?: 0] = lesson
                        mergedSchedule[lesson.number ?: 0].isChanged = true
                    }
                    catch (exception: IndexOutOfBoundsException) {
                        mergedSchedule.add(Lesson(lesson.number, lesson.name, lesson.teacher, lesson.place, true))
                        println("While merging schedule with changes found missing lesson...")
                    }
                }

                TargetedDaySchedule(schedule!!.day, mergedSchedule)
            }
        }
    }

    companion object {

        /**
         * Generates a template schedule for practise days.
         *
         * Return value contains seven lessons (0..6), with 'Практика' value in lessons name.
         */
        fun getPractiseFinalSchedule(date: Calendar?, target: String?): TargetedDayScheduleResult {
            val builder = TargetedDayScheduleResultBuilder()
            builder.setChanges(TargetedChangesOfDay.getOnPractiseChanges(date, target))

            builder.bakeFinalSchedule()
            return builder.build()
        }

        /**
         * Generates a template schedule for debt liquidation days.
         *
         * Return value contains seven lessons (0..6), with 'Ликвидация Задолженностей' value in lessons name.
         */
        fun getDebtLiquidationFinalSchedule(date: Calendar?, target: String?): TargetedDayScheduleResult {
            val builder = TargetedDayScheduleResultBuilder()
            builder.setChanges(TargetedChangesOfDay.getDebtLiquidationChanges(date, target))

            builder.bakeFinalSchedule()
            return builder.build()
        }

    }
    /* endregion */
}
