package model.entity.schedule.lite.base

import controller.db.pgsql.schedule.lite.ScheduleDataContext
import controller.db.pgsql.schedule.lite.ScheduleDataContext.FinalSchedules.index
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID


class Lesson(id: EntityID<Int>) : Entity<Int>(id) {

    var number: Int by ScheduleDataContext.Lessons.number

    var name: String by ScheduleDataContext.Lessons.name

    var teacherId: Int? by ScheduleDataContext.Lessons.teacherId

    var place: String? by ScheduleDataContext.Lessons.place

    var isChanged: Boolean? by ScheduleDataContext.Lessons.isChanged

    var basicScheduleId: Int? by ScheduleDataContext.Lessons.basicId

    var replacementId: Int? by ScheduleDataContext.Lessons.replacementId

    var finalScheduleId: Int? by ScheduleDataContext.Lessons.finalId

    init {
        index(false, ScheduleDataContext.Lessons.finalId, ScheduleDataContext.Lessons.replacementId)
    }

    companion object : EntityClass<Int, Lesson>(ScheduleDataContext.Lessons)
}
