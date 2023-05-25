package model.entity.schedule.lite

import controller.db.pgsql.schedule.lite.ScheduleDataContext
import controller.db.pgsql.schedule.lite.ScheduleDataContext.BasicSchedules.index
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID


class BasicSchedule(id: EntityID<Int>): Entity<Int>(id) {
    var targetGroup: String by ScheduleDataContext.BasicSchedules.targetGroup

    var dayIndex: Int by ScheduleDataContext.BasicSchedules.dayIndex

    var targetCycleId: Int by ScheduleDataContext.BasicSchedules.targetCycleId

    var commitHash: Int by ScheduleDataContext.BasicSchedules.commitHash

    init {
        index(false, ScheduleDataContext.BasicSchedules.targetGroup)
    }

    companion object : EntityClass<Int, BasicSchedule>(ScheduleDataContext.BasicSchedules)
}
