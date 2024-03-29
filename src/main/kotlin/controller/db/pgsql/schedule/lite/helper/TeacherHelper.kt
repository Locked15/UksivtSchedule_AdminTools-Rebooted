package controller.db.pgsql.schedule.lite.helper

import controller.db.pgsql.schedule.lite.helper.util.checkTeacherPropertiesState
import controller.db.pgsql.schedule.lite.helper.util.makePostProcessingChecks
import controller.view.Logger
import model.environment.log.LogLevel
import org.jetbrains.exposed.dao.id.EntityID
import model.data.schedule.base.Teacher as TeacherModel
import model.entity.schedule.lite.base.Teacher as TeacherEntity


fun getTeacherWithSideActions(teacherName: String?): Pair<Int?, Boolean> {
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
                    Logger.logException(exception, 1, "Object data: ${newTeacherModel.fullName}")
                    Pair(-1, false)
                }
            }
    }
    else {
        Pair(null, false)
    }
}

private fun tryToFindPresenceEntryID(newTeacher: TeacherModel,
                                     allTeachers: List<TeacherEntity>): EntityID<Int>? {
    val fullEqualEntries = allTeachers.filter { it.compareWithOtherModel(newTeacher) == 1 }
    val partialEqualEntries = allTeachers.filter { it.compareWithOtherModel(newTeacher) == 0 }

    //? At first, we will check full equality (to make it lazy-working: if there is another instance, other blocks willn't be executed).
    return if (fullEqualEntries.isNotEmpty()) {
        if (fullEqualEntries.size > 1)
            Logger.logMessage(LogLevel.WARNING, "Teacher entity duplicates was found!\n\tInfo: ${newTeacher.fullName}")
        fullEqualEntries[0].id
    }
    //? After the first block (if there are no full equality entries), we will make partial checking.
    else if (partialEqualEntries.size == 1 && checkTeacherPropertiesState(newTeacher, partialEqualEntries[0])) {
        partialEqualEntries[0].updateSecondaryFields(newTeacher)
        partialEqualEntries[0].id
    }
    //? In all other cases, we got non-occurred surname (so, we predicate this is new teacher).
    else {
        //? After all, there is no searching entry in DB, so we will make some actions and actually return null.
        makePostProcessingChecks(newTeacher, allTeachers, 1)
        null
    }
}

private fun createNewTeacherInstance(newTeacher: TeacherModel) = TeacherEntity.new {
    id
    surname = newTeacher.surname

    name = newTeacher.name
    patronymic = newTeacher.patronymic
}.id
