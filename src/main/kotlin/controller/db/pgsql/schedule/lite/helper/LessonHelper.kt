package controller.db.pgsql.schedule.lite.helper

import org.jetbrains.exposed.dao.id.EntityID

import model.data.schedule.base.Lesson as LessonModel
import model.data.schedule.base.Teacher as TeacherModel

import model.entity.schedule.lite.base.Lesson as LessonEntity
import model.entity.schedule.lite.base.Teacher as TeacherEntity


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
            isChanged = true // Hack: Old replacements (changes) assets contain 'false' in this property.

            replacementId = newReplacementId
            scheduleId = newFinalScheduleId
        }
    }

    return alteredTeachersCount
}

private fun getTeacherWithSideActions(teacherName: String?): Pair<Int?, Boolean> {
    return if (!teacherName.isNullOrBlank()) {
        //? To store data in program memory, we convert db-query to a stored list.
        val allAvailableTeachers = TeacherEntity.all().toList()
        //? We must normalize the original sent teacher name, because commonly it has written vary.
        val normalizedTeacherName = TeacherModel.normalizeTeacherName(teacherName)

        val newTeacherModel = TeacherModel.createTeacherModelByNormalizedName(normalizedTeacherName)
        tryToFindPresenceEntryID(newTeacherModel, allAvailableTeachers)?.let { Pair(it.value, false) }
            ?: run {
                try {
                    // This one will be executed if the previous one isn't.
                    Pair(createNewTeacherInstance(newTeacherModel).value, true)
                }
                catch (exception: Exception) {
                    println("ERROR:\n\tOn new teacher creation error occurred: ${exception.message};" +
                                    "\n\tObject data: ${newTeacherModel.fullName}.")
                    Pair(-1, false)
                }
            }
    }
    else {
        Pair(null, false)
    }
}

private fun tryToFindPresenceEntryID(newTeacher: TeacherModel, allTeachers: List<TeacherEntity>): EntityID<Int>? {
    val fullEqualEntries = allTeachers.filter { it.compareWithOtherModel(newTeacher) == 1 }
    val partialEqualEntries = allTeachers.filter { it.compareWithOtherModel(newTeacher) == 0 }

    //? At first, we will check full equality (to make it lazy-working: if there is another instance, other blocks willn't be executed).
    if (fullEqualEntries.isNotEmpty()) {
        if (fullEqualEntries.size > 1) {
            println("WARNING:\n\tTeacher entity duplicates was found!" +
                            "\n\tInfo: ${newTeacher.fullName}.")
            }
        return fullEqualEntries[0].id
    }
    //? After the first block (if there are no full equality entries), we will make partial checking.
    if (partialEqualEntries.size == 1 && newTeacher.isShortEntry() != partialEqualEntries[0].isShortEntry()) {
        partialEqualEntries[0].updateSecondaryFields(newTeacher)
        return partialEqualEntries[0].id
    }

    //? Otherwise, there is no searching entry in DB, so we will return null.
    return null
}

private fun createNewTeacherInstance(newTeacher: TeacherModel) = TeacherEntity.new {
    id
    surname = newTeacher.surname

    name = newTeacher.name
    patronymic = newTeacher.patronymic
}.id
