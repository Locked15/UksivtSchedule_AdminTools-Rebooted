package controller.db.pgsql.schedule.lite.helper.util

import model.data.schedule.base.Teacher as TeacherModel
import model.entity.schedule.lite.base.Teacher as TeacherEntity

fun isShortEntry(teacher: TeacherModel) = teacher.name == null || teacher.patronymic == null

fun isShortEntry(teacher: TeacherEntity) = teacher.name == null || teacher.patronymic == null

fun checkTeacherPropertiesState(newTeacher: TeacherModel,
                                existTeacherEntity: TeacherEntity): Boolean {
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

fun filterTeachersListWithoutGenderInclude(newTeacherModel: TeacherModel, teacherEntitiesList: List<TeacherEntity>,
                                           targetEqualityId: Int = 1): List<TeacherEntity> {
    return teacherEntitiesList.filter {
        val unGenderedNewTeacherModel = newTeacherModel.createUnGenderedInstance()
        val unGenderedPresentedTeacherEntity = it.createUnGenderedInstance()

        val compareResult = unGenderedNewTeacherModel.compareWithOtherModel(unGenderedPresentedTeacherEntity)
        compareResult == targetEqualityId
    }
}
