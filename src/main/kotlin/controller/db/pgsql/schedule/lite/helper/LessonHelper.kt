package controller.db.pgsql.schedule.lite.helper

import model.data.schedule.base.Lesson as LessonModel
import model.entity.schedule.lite.base.Lesson as LessonEntity


fun createNewLessonInstances(lessons: List<LessonModel>, newReplacementId: Int?, newFinalScheduleId: Int?,
                             exceptionOnEmptyLessonName: Boolean = true): Int {
    var alteredTeachersCount = 0
    for (lesson in lessons) {
        val teacherInfo = getTeacherWithSideActions(lesson.teacher)
        if (teacherInfo.second) alteredTeachersCount++

        LessonEntity.new {
            id

            number = lesson.number!!
            name = if (exceptionOnEmptyLessonName) lesson.name!! else lesson.name ?: "Нет"
            teacherId = teacherInfo.first
            place = lesson.place
            isChanged = lesson.isChanged

            replacementId = newReplacementId
            scheduleId = newFinalScheduleId
        }
    }

    return alteredTeachersCount
}

