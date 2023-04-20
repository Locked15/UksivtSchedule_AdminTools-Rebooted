package controller.db.pgsql.schedule.lite

import model.data.schedule.base.Teacher as TeacherModel
import model.data.schedule.base.Lesson as LessonModel
import model.data.change.day.TargetedChangesOfDay as TargetedChangesOfDayModel
import model.data.schedule.common.result.day.TargetedFinalDaySchedule as TargetedFinalDayScheduleModel

import model.entity.schedule.lite.base.Teacher as TeacherEntity
import model.entity.schedule.lite.base.Lesson as LessonEntity
import model.entity.schedule.lite.ScheduleReplacement as ScheduleReplacementEntity
import model.entity.schedule.lite.FinalSchedule as FinalScheduleEntity

import java.time.LocalDate
import org.jetbrains.exposed.dao.id.EntityID
import model.environment.db.DBConnectionModel


fun getConnectionModel(dbName: String, rawConfiguration: HashMap<String, String>): DBConnectionModel {
    //? Here we take DB address. Or use 'localhost' if it's not available.
    val addressWithPort = rawConfiguration.getOrElse("DB.$dbName.Address") {
        "localhost"
    }
    //? Here we take DB user. Or use 'postgres' if it's not specified.
    val user = rawConfiguration.getOrElse("DB.$dbName.User") {
        rawConfiguration.getOrElse("DB.General.User") {
            "postgres"
        }
    }
    //? And here we take the user password. Or use 'Unknown' if it's not specified.
    val password = rawConfiguration.getOrElse("DB.$dbName.Password") {
        rawConfiguration.getOrElse("DB.General.Password") {
            "Unknown"
        }
    }

    return DBConnectionModel(addressWithPort, dbName, user, password)
}

/* region Replacements Sync Functions */

fun insertNewChangeToDb(change: TargetedChangesOfDayModel?): Boolean {
    if (change != null) {
        val newReplacementId = createNewReplacementInstance(change)
        createNewLessonInstances(change.changedLessons, newReplacementId.value, null, false)

        return true
    }
    return false
}

private fun createNewReplacementInstance(change: TargetedChangesOfDayModel): EntityID<Int> {
    val hash = change.hashCode()
    return ScheduleReplacementEntity.new {
        id
        commitHash = hash

        targetGroup = change.targetGroup!!
        replacementDate = LocalDate.ofInstant(change.changesDate!!.toInstant(),
                                              change.changesDate!!.timeZone
                                                  .toZoneId())
        isAbsolute = change.isAbsolute
    }.id
}
/* endregion */

/* region Final Schedule Sync Functions */

fun insertNewFinalScheduleToDb(targetSchedule: TargetedFinalDayScheduleModel?): Boolean {
    if (targetSchedule != null) {
        // For memory optimization, we'll store in DB only actual lessons (without 'filler' ones).
        val newFinalScheduleId = createNewFinalScheduleInstance(targetSchedule)
        createNewLessonInstances(targetSchedule.schedule.lessons.filter { it.name != null },
                                 null, newFinalScheduleId.value)

        return true
    }
    return false
}

private fun createNewFinalScheduleInstance(targetSchedule: TargetedFinalDayScheduleModel): EntityID<Int> {
    val hash = targetSchedule.hashCode()
    return FinalScheduleEntity.new {
        id
        commitHash = hash

        targetGroup = targetSchedule.targetGroup!!
        scheduleDate = LocalDate.ofInstant(targetSchedule.scheduleDate!!.toInstant(),
                                           targetSchedule.scheduleDate.timeZone
                                               .toZoneId())
    }.id
}
/* endregion */

/* region Lessons Sync Functions */

private fun createNewLessonInstances(lessons: List<LessonModel>, newReplacementId: Int?, newFinalScheduleId: Int?,
                                     exceptionOnEmptyLessonName: Boolean = true) {
    for (lesson in lessons) {
        LessonEntity.new {
            id

            number = lesson.number!!
            name = if (exceptionOnEmptyLessonName) lesson.name!! else lesson.name ?: "Нет"
            teacherId = getTeacherIDWithSideActions(lesson.teacher)
            place = lesson.place
            isChanged = true // Hack: Old replacements (changes) assets contain 'false' in this property.

            replacementId = newReplacementId
            scheduleId = newFinalScheduleId
        }
    }
}

private fun getTeacherIDWithSideActions(teacherName: String?): Int? {
    return if (!teacherName.isNullOrBlank()) {
        //? To store data in program memory, we convert db-query to a stored list.
        val allAvailableTeachers = TeacherEntity.all().toList()
        //? We must normalize the original sent teacher name, because commonly it has written vary.
        val normalizedTeacherName = TeacherModel.normalizeTeacherName(teacherName)

        val newTeacherModel = TeacherModel.createTeacherModelByNormalizedName(normalizedTeacherName)
        tryToFindPresenceEntryId(newTeacherModel, allAvailableTeachers)?.value ?: run {
            // This one will be executed if the previous one isn't.
            createNewTeacherInstance(newTeacherModel).value
        }
    }
    else {
        null
    }
}

private fun tryToFindPresenceEntryId(newTeacher: TeacherModel, allTeachers: List<TeacherEntity>): EntityID<Int>? {
    //? At first, we will check full equality (to make it lazy-working: if there is another instance, other blocks willn't be executed).
    with (allTeachers.filter { it.compareWithOtherModel(newTeacher) == 1 }) {
        if (this.isNotEmpty()) {
            if (this.size > 1) {
                println("WARNING:\n\tTeacher entity duplicates was found!")
            }
            return this[0].id
        }
    }
    //? After the first block (if there are no full equality entries), we will make partial checking.
    with (allTeachers.filter { it.compareWithOtherModel(newTeacher) == 0 }) {
        if (this.size == 1 && (this[0].isShortEntry() && !newTeacher.isShortEntry())) {
            this[0].updateSecondaryFields(newTeacher)
            println("INFO:\n\tExisting entry will be updated with new data (${newTeacher.fullName}).")

            return this[0].id
        }
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
/* endregion */
