package controller.db.pgsql.schedule.lite.helper

import model.data.schedule.base.Lesson as LessonModel
import model.entity.schedule.lite.base.Lesson as LessonEntity


fun createNewLessonInstances(lessons: List<LessonModel>,
                             basicId: Int? = null, replaceId: Int? = null, finalId: Int? = null,
                             exceptionOnEmptyLessonName: Boolean = true): Pair<Int, List<Pair<Int?, String?>>> {
    var createdTeachersCount = 0
    val newTeachersData = mutableListOf<Pair<Int?, String?>>()
    for (lesson in lessons) {
        val teacherInfo = getTeacherWithSideActions(lesson.teacher)
        if (teacherInfo.second) {
            createdTeachersCount++
            newTeachersData.add(Pair(teacherInfo.first, lesson.teacher))
        }

        LessonEntity.new {
            id

            number = lesson.number!!
            name = if (exceptionOnEmptyLessonName) lesson.name!! else lesson.name ?: "Нет"
            teacherId = teacherInfo.first
            place = lesson.place
            isChanged = lesson.isChanged

            basicScheduleId = basicId
            replacementId = replaceId
            finalScheduleId = finalId
        }
    }

    return Pair(createdTeachersCount, newTeachersData)
}

