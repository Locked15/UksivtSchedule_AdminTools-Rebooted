package controller.db.pgsql.schedule.lite.helper

import controller.view.Logger
import model.data.schedule.common.result.day.TargetedFinalDaySchedule
import model.entity.schedule.lite.FinalSchedule
import model.environment.log.LogLevel
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate


fun insertNewFinalScheduleToDB(targetSchedule: TargetedFinalDaySchedule?, targetCycleId: Int?): Boolean {
    if (targetSchedule != null) {
        // For memory optimization, we'll store in DB only actual lessons (without 'filler' ones).
        val newFinalScheduleId = createNewFinalScheduleInstance(targetSchedule, targetCycleId)
        val alteredTeachers = createNewLessonInstances(targetSchedule.schedule.lessons.filter { it.name != null },
                                                       null, newFinalScheduleId.value)

        if (alteredTeachers > 0) {
            Logger.logMessage(LogLevel.DEBUG,
                              "New teacher entries: $alteredTeachers for ${targetSchedule.targetGroup}.", 1)
        }
        return true
    }
    return false
}

private fun createNewFinalScheduleInstance(targetSchedule: TargetedFinalDaySchedule,
                                           possibleTargetCycleId: Int?): EntityID<Int> {
    val hash = targetSchedule.hashCode()
    return FinalSchedule.new {
        id
        commitHash = hash
        targetCycleId = possibleTargetCycleId ?: -1

        targetGroup = targetSchedule.targetGroup!!
        scheduleDate = LocalDate.ofInstant(targetSchedule.scheduleDate!!.toInstant(),
                                           targetSchedule.scheduleDate.timeZone
                                               .toZoneId())
    }.id
}
