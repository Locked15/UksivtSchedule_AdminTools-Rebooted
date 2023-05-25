package controller.db.pgsql.schedule.lite.helper.main

import controller.db.pgsql.schedule.lite.helper.createNewLessonInstances
import controller.db.pgsql.schedule.lite.helper.main.base.BaseMainHelper
import controller.view.Logger
import model.data.schedule.common.origin.day.TargetedDaySchedule
import model.entity.schedule.lite.BasicSchedule
import model.environment.log.LogLevel
import org.jetbrains.exposed.dao.id.EntityID


class BasicScheduleHelper(basicSchedule: TargetedDaySchedule) : BaseMainHelper<TargetedDaySchedule>(basicSchedule) {

    override fun insertNewEntityIntoDB(targetCycleId: Int?) = insertNewEntityIntoDB(targetCycleId, 0, "")

    fun insertNewEntityIntoDB(targetCycleId: Int?, dayIndex: Int, groupName: String): Boolean {
        if (newItem != null) {
            // For memory optimization, we'll store in DB only actual lessons (without 'filler' ones).
            val newEntityEntryId = createNewEntityEntry(newItem, targetCycleId, dayIndex, groupName)
            val alteredTeachers = createNewLessonInstances(newItem.lessons.filter { !it.name.isNullOrBlank() },
                                                           basicId = newEntityEntryId.value)

            if (alteredTeachers.first > 0) {
                Logger.logMessage(LogLevel.DEBUG,
                                  "New teacher entries: ${alteredTeachers.first} for $groupName/${newItem.day}" +
                                          "\n\t(${alteredTeachers.second.joinToString(", ")})")
            }
            return true
        }
        return false
    }

    override fun createNewEntityEntry(creationEntry: TargetedDaySchedule?, targetCycleId: Int?) = createNewEntityEntry(
            creationEntry, targetCycleId, 0, ""
    )

    private fun createNewEntityEntry(creationEntry: TargetedDaySchedule?, cycleId: Int?,
                                     index: Int, groupName: String): EntityID<Int> {
        val hash = creationEntry.hashCode()
        return BasicSchedule.new {
            id
            dayIndex = index
            targetCycleId = cycleId ?: -1

            targetGroup = groupName
            commitHash = hash
        }.id
    }
}
