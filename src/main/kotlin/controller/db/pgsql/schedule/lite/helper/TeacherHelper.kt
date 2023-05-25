package controller.db.pgsql.schedule.lite.helper

import controller.view.Logger
import model.data.schedule.base.Teacher
import model.environment.log.LogLevel
import org.jetbrains.exposed.dao.id.EntityID
import model.entity.schedule.lite.base.Teacher as TeacherEntity


fun getTeacherWithSideActions(teacherName: String?): Pair<Int?, Boolean> {
    return if (!teacherName.isNullOrBlank()) {
        //? To store data in program memory, we convert db-query to a stored list.
        val allAvailableTeachers = model.entity.schedule.lite.base.Teacher.all().toList()
        //? We must normalize the original sent teacher name, because commonly it has written vary.
        val normalizedTeacherName = Teacher.normalizeTeacherName(teacherName)

        val newTeacherModel = Teacher.createTeacherModelByNormalizedName(normalizedTeacherName)
        tryToFindPresenceEntryID(newTeacherModel, allAvailableTeachers)?.let { Pair(it.value, false) }
            ?: run {
                try {
                    // This one will be executed if the previous one isn't.
                    Pair(createNewTeacherInstance(newTeacherModel).value, true)
                }
                catch (exception: Exception) {
                    Logger.logException(exception, 1, "Object data: ${newTeacherModel.fullName}")
                    Pair(-1, false)
                }
            }
    }
    else {
        Pair(null, false)
    }
}

private fun tryToFindPresenceEntryID(newTeacher: Teacher,
                                     allTeachers: List<TeacherEntity>): EntityID<Int>? {
    val fullEqualEntries = allTeachers.filter { it.compareWithOtherModel(newTeacher) == 1 }
    val partialEqualEntries = allTeachers.filter { it.compareWithOtherModel(newTeacher) == 0 }

    //? At first, we will check full equality (to make it lazy-working: if there is another instance, other blocks willn't be executed).
    if (fullEqualEntries.isNotEmpty()) {
        if (fullEqualEntries.size > 1)
            Logger.logMessage(LogLevel.WARNING, "Teacher entity duplicates was found!\n\tInfo: ${newTeacher.fullName}")
        return fullEqualEntries[0].id
    }
    //? After the first block (if there are no full equality entries), we will make partial checking.
    if (partialEqualEntries.size == 1 && checkTeacherPropertiesState(newTeacher, partialEqualEntries[0])) {
        partialEqualEntries[0].updateSecondaryFields(newTeacher)
        return partialEqualEntries[0].id
    }

    //? Otherwise, there is no searching entry in DB, so we will return null.
    return null
}

private fun checkTeacherPropertiesState(newTeacherEntity: Teacher,
                                        existTeacherEntity: model.entity.schedule.lite.base.Teacher): Boolean {
    val isShortEntriesStateNotEqual = newTeacherEntity.isShortEntry() != existTeacherEntity.isShortEntry()
    val isAdditionalPropertiesAreReverseEqual = checkSecondaryInfoToReverseEquality(Pair(newTeacherEntity.name,
                                                                                         newTeacherEntity.patronymic),
                                                                                    Pair(existTeacherEntity.name,
                                                                                         existTeacherEntity.patronymic)
    )

    return isShortEntriesStateNotEqual || isAdditionalPropertiesAreReverseEqual
}

fun checkSecondaryInfoToReverseEquality(newTeacherEntity: Pair<String?, String?>,
                                        existingTeacherEntity: Pair<String?, String?>): Boolean {
    val nameToSurnameEquality = newTeacherEntity.first.equals(existingTeacherEntity.second, true)
    val patronymicToNameEquality = existingTeacherEntity.first.equals(newTeacherEntity.second, true)

    return nameToSurnameEquality && patronymicToNameEquality
}

private fun createNewTeacherInstance(newTeacher: Teacher) = model.entity.schedule.lite.base.Teacher.new {
    id
    surname = newTeacher.surname

    name = newTeacher.name
    patronymic = newTeacher.patronymic
}.id
