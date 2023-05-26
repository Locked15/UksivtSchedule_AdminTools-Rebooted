package controller.db.pgsql.schedule.lite.helper.util

import controller.view.Logger
import model.data.schedule.base.Teacher
import model.environment.log.LogLevel
import model.data.schedule.base.Teacher as TeacherModel
import model.entity.schedule.lite.base.Teacher as TeacherEntity


/* region Short-State Check Functions */

fun isShortEntry(teacher: TeacherModel) = teacher.name == null || teacher.patronymic == null

fun isShortEntry(teacher: TeacherEntity) = teacher.name == null || teacher.patronymic == null
/* endregion */

/* region Main-Check Functions */

fun checkTeacherPropertiesState(newTeacher: TeacherModel, existTeacherEntity: TeacherEntity): Boolean {
    val isShortEntriesStateNotEqual = isShortEntry(newTeacher) != isShortEntry(existTeacherEntity)
    val isAdditionalPropertiesAreReverseEqual = checkSecondaryInfoToReverseEquality(Pair(newTeacher.name,
                                                                                         newTeacher.patronymic),
                                                                                    Pair(existTeacherEntity.name,
                                                                                         existTeacherEntity.patronymic)
    )

    return isShortEntriesStateNotEqual || isAdditionalPropertiesAreReverseEqual
}

private fun checkSecondaryInfoToReverseEquality(newTeacherEntity: Pair<String?, String?>,
                                                existingTeacherEntity: Pair<String?, String?>): Boolean {
    val nameToSurnameEquality = newTeacherEntity.first.equals(existingTeacherEntity.second, true)
    val patronymicToNameEquality = existingTeacherEntity.first.equals(newTeacherEntity.second, true)

    return nameToSurnameEquality && patronymicToNameEquality
}
/* endregion */

/* region Post-Processing Check Functions */

fun makePostProcessingChecks(newTeacherModel: TeacherModel, allTeachers: List<TeacherEntity>,
                             targetEqualityId: Int): Boolean {
    return checkUnGenderedTeachersList(newTeacherModel, allTeachers, targetEqualityId)
}

private fun checkUnGenderedTeachersList(newTeacherModel: Teacher, allTeachers: List<TeacherEntity>,
                                        targetEqualityId: Int): Boolean {
    /* But, sometimes in documents surname written in different gender (M -> F), so we should warn user about it.
       We can't say surely that this is a just mistake (after all, this may be really new teacher).
       So just make LOG and continue.

       Yeah, this is a reference to gender-bender.
       Ha-Ha. */
    val benderTeachers = filterTeachersListWithoutGenderInclude(newTeacherModel, allTeachers, targetEqualityId)
    if (benderTeachers.isNotEmpty()) {
        /**
         * Here we make a message, that contains information about target IDs.
         * * Old ID contains ID of the teacher that presented in the system, but has different gender.
         * * The New ID contains a new teacher, that will be created in the system.
         *
         * This allows user to easily replace incorrect IDs via procedures of the DB.
         */
        val idInfoMessage = "Old ID: ${benderTeachers[0].id} -> New ID: ${allTeachers.maxOf { it.id }.value + 1}"
        Logger.logMessage(LogLevel.WARNING, "Found teachers (${benderTeachers.size}) with same Surname, " +
                "but with different gender. Details:" +
                "\n\t($idInfoMessage)")

        return true
    }

    return false
}

private fun filterTeachersListWithoutGenderInclude(newTeacherModel: TeacherModel, entityList: List<TeacherEntity>,
                                                   targetEqualityId: Int = 1): List<TeacherEntity> {
    return entityList.filter {
        val unGenderedNewTeacherModel = newTeacherModel.createUnGenderedInstance()
        val unGenderedPresentedTeacherEntity = it.createUnGenderedInstance()

        val compareResult = unGenderedNewTeacherModel.compareWithOtherModel(unGenderedPresentedTeacherEntity)
        compareResult == targetEqualityId
    }
}
/* endregion */
