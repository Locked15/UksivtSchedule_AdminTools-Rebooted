package controller.db.pgsql.schedule.lite.helper

import controller.view.Logger
import model.data.change.day.TargetedChangesOfDay
import model.entity.schedule.lite.ScheduleReplacement
import model.environment.log.LogLevel
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate


fun insertNewChangeToDB(change: TargetedChangesOfDay?, targetCycleId: Int?): Boolean {
    if (change != null) {
        val newReplacementId = createNewReplacementInstance(change, targetCycleId)
        val alteredTeachers = createNewLessonInstances(change.changedLessons, newReplacementId.value, null, false)

        if (alteredTeachers.first > 0)
            Logger.logMessage(LogLevel.DEBUG, "New teacher entries: $alteredTeachers for ${change.targetGroup}" +
                    "\n\t(${alteredTeachers.second.joinToString(" ")})")
        return true
    }
    return false
}

private fun createNewReplacementInstance(change: TargetedChangesOfDay, possibleTargetCycleId: Int?): EntityID<Int> {
    val hash = change.hashCode()
    return ScheduleReplacement.new {
        id
        commitHash = hash
        targetCycleId = possibleTargetCycleId ?: -1

        targetGroup = change.targetGroup!!
        replacementDate = LocalDate.ofInstant(change.changesDate!!.toInstant(),
                                              change.changesDate!!.timeZone
                                                  .toZoneId())
        isAbsolute = change.isAbsolute
    }.id
}
