package controller.db.pgsql.schedule.lite.helper.main

import controller.db.pgsql.schedule.lite.helper.createNewLessonInstances
import controller.db.pgsql.schedule.lite.helper.main.base.BaseMainHelper
import controller.view.Logger
import model.data.schedule.common.result.day.TargetedFinalDaySchedule
import model.entity.schedule.lite.FinalSchedule
import model.environment.log.LogLevel
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate


class FinalScheduleHelper(schedule: TargetedFinalDaySchedule?) : BaseMainHelper<TargetedFinalDaySchedule>(schedule) {

    override fun insertNewEntityIntoDB(targetCycleId: Int?): Boolean {
        if (newItem != null) {
            // For memory optimization, we'll store in DB only actual lessons (without 'filler' ones).
            val newFinalScheduleId = createNewEntityEntry(newItem, targetCycleId)
            val alteredTeachers = createNewLessonInstances(newItem.schedule.lessons.filter { !it.name.isNullOrBlank() },
                                                           finalId = newFinalScheduleId.value)

            if (alteredTeachers.first > 0) {
                Logger.logMessage(LogLevel.DEBUG, getLogMessageForTeachersAltered(alteredTeachers,
                                                                                  newItem.targetGroup ?: ""))
            }
            return true
        }
        return false
    }

    override fun createNewEntityEntry(creationEntry: TargetedFinalDaySchedule?, targetCycleId: Int?): EntityID<Int> {
        val hash = creationEntry.hashCode()
        return FinalSchedule.new {
            id
            commitHash = hash
            this.targetCycleId = targetCycleId ?: -1

            targetGroup = creationEntry?.targetGroup!!
            scheduleDate = LocalDate.ofInstant(creationEntry.scheduleDate!!.toInstant(),
                                               creationEntry.scheduleDate.timeZone
                                                   .toZoneId())
        }.id
    }
}
