package model.entity.schedule.lite

import controller.db.pgsql.schedule.lite.ScheduleDataContext
import controller.db.pgsql.schedule.lite.ScheduleDataContext.FinalSchedules.index
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate


class FinalSchedule(id: EntityID<Int>) : Entity<Int>(id) {

    var commitHash: Int by ScheduleDataContext.FinalSchedules.commitHash

    var targetCycleId : Int by ScheduleDataContext.FinalSchedules.targetCycleId

    var targetGroup: String by ScheduleDataContext.FinalSchedules.targetGroup

    var scheduleDate: LocalDate by ScheduleDataContext.FinalSchedules.scheduleDate

    init {
        index(false, ScheduleDataContext.FinalSchedules.targetGroup)
    }

    companion object : EntityClass<Int, FinalSchedule>(ScheduleDataContext.FinalSchedules)
}
