package controller.db.pgsql.schedule.lite.helper.main

import controller.db.pgsql.schedule.lite.helper.createNewLessonInstances
import controller.db.pgsql.schedule.lite.helper.main.base.BaseMainHelper
import controller.view.Logger
import model.data.change.day.TargetedChangesOfDay
import model.entity.schedule.lite.ScheduleReplacement
import model.environment.log.LogLevel
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate


class ReplacementHelper(change: TargetedChangesOfDay?) : BaseMainHelper<TargetedChangesOfDay>(change) {

    override fun insertNewEntityIntoDB(targetCycleId: Int?): Boolean {
        if (newItem != null) {
            val newReplacementId = createNewEntityEntry(newItem, targetCycleId)
            val alteredTeachers = createNewLessonInstances(newItem.changedLessons,
                                                           replaceId = newReplacementId.value,
                                                           exceptionOnEmptyLessonName = false)

            if (alteredTeachers.first > 0)
                Logger.logMessage(LogLevel.DEBUG,
                                  "New teacher entries: ${alteredTeachers.first} for ${newItem.targetGroup}" +
                                          "\n\t(${alteredTeachers.second.joinToString(", ")})")
            return true
        }
        return false
    }

    override fun createNewEntityEntry(creationEntry: TargetedChangesOfDay?, targetCycleId: Int?): EntityID<Int> {
        val hash = creationEntry.hashCode()
        return ScheduleReplacement.new {
            id
            commitHash = hash
            this.targetCycleId = targetCycleId ?: -1

            targetGroup = creationEntry?.targetGroup!!
            replacementDate = LocalDate.ofInstant(creationEntry.changesDate!!.toInstant(),
                                                  creationEntry.changesDate!!.timeZone
                                                      .toZoneId())
            isAbsolute = creationEntry.isAbsolute
        }.id
    }
}
