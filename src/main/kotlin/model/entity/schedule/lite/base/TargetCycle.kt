package model.entity.schedule.lite.base

import controller.db.pgsql.schedule.lite.ScheduleDataContext
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID



class TargetCycle(id: EntityID<Int>) : Entity<Int>(id) {

    var year: Int by ScheduleDataContext.TargetCycles.year

    var semester : Int by ScheduleDataContext.TargetCycles.semester

    companion object : EntityClass<Int, TargetCycle>(ScheduleDataContext.TargetCycles)
}
