package model.entity.schedule.lite

import controller.db.pgsql.schedule.lite.ScheduleDataContext
import controller.db.pgsql.schedule.lite.ScheduleDataContext.FinalSchedules.index
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate


class ScheduleReplacement(id: EntityID<Int>) : Entity<Int>(id) {

    var commitHash: Int by ScheduleDataContext.ScheduleReplacements.commitHash

    var isAbsolute: Boolean by ScheduleDataContext.ScheduleReplacements.isAbsolute

    var targetGroup: String by ScheduleDataContext.ScheduleReplacements.targetGroup

    var replacementDate: LocalDate by ScheduleDataContext.ScheduleReplacements.replacementDate

    init {
        index(false, ScheduleDataContext.ScheduleReplacements.targetGroup)
    }

    companion object : EntityClass<Int, ScheduleReplacement>(ScheduleDataContext.ScheduleReplacements)
}
