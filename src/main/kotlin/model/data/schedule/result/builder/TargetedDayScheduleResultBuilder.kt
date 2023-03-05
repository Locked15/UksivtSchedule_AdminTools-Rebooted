package model.data.schedule.result.builder

import model.data.changes.TargetedChangesOfDay
import model.data.schedule.origin.TargetedDaySchedule
import model.data.schedule.result.TargetedDayScheduleResult
import java.util.*


/**
 * Resulting class of merging basic schedule to the changes (AKA replacements).
 * This is builder for resulting class.
 *
 * I created this builder, to insert new functionality softly, without changing
 * [existing][TargetedDaySchedule.mergeWithChanges] [logic][controller.data.reader.word.Reader.getChangedSchedule].
 * It's not fluent builder by the way.
 */
class TargetedDayScheduleResultBuilder {

    /* region Properties */

    private var date: Calendar? = null

    private var isChanged: Boolean = false

    private var schedule: TargetedDaySchedule? = null

    private var changes: TargetedChangesOfDay? = null
    /* endregion */

    /* region Functions */

    fun setDate(date: Calendar?) {
        this.date = date
    }

    fun setChanged(isChanged: Boolean) {
        this.isChanged = isChanged
    }

    fun setSchedule(schedule: TargetedDaySchedule) {
        this.schedule = schedule
    }

    fun setChanges(changes: TargetedChangesOfDay) {
        this.changes = changes
    }

    fun bakeFinalSchedule() {
        schedule = schedule!!.mergeWithChanges(changes)

        if (changes == null) isChanged = false
        date = changes?.changesDate
    }

    fun build() = TargetedDayScheduleResult(date, isChanged, schedule!!)
    /* endregion */
}
