package controller.db.pgsql.schedule.lite

import controller.db.DBKind
import controller.db.config.DBConfiguration
import model.data.change.group.ScheduleChangesGroup
import model.data.schedule.common.result.group.FinalScheduleGroup
import model.entity.schedule.lite.base.Lesson
import org.jetbrains.exposed.dao.id.EntityID
import model.data.schedule.base.Lesson as LessonModel
import model.data.change.day.TargetedChangesOfDay as TargetedDayReplacementsModel
import model.data.change.day.GeneralChangesOfDay as GeneralDayReplacementsModel
import model.data.schedule.common.result.day.TargetedFinalDaySchedule as TargetedFinalDayScheduleModel
import model.data.schedule.common.result.day.GeneralFinalDaySchedule as GeneralFinalDayScheduleModel

import model.entity.schedule.lite.ScheduleReplacement as ReplacementEntity
import model.entity.schedule.lite.FinalSchedule as FinalScheduleEntity

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileNotFoundException

import java.time.LocalDate


class ScheduleDataContext private constructor() {

    private val dbConnection: Database

    init {
        val configuration = DBConfiguration(getDBKind()).getConfiguration()

        if (configuration != null) {
            val address = configuration.getOrElse("DB.${DB_Name}.Address") { "Unknown" }
            val user = configuration.getOrElse("DB.${DB_Name}.User") {
                configuration.getOrElse("DB.General.User") { "Unknown" }
            }
            val password = configuration.getOrElse("DB.${DB_Name}.Password") {
                configuration.getOrElse("DB.General.Password") { "Unknown" }
            }

            dbConnection = Database.connect(
                    "${getDBKind().getSpecificDataBaseAddressConnector()}://$address/$DB_Name",
                    driver = getDBKind().getSpecificDataBaseDataDriver(),
                    user = user,
                    password = password
            )
        }
        else {
            throw FileNotFoundException("Configuration file not found!\nIt's required for data synchronization.")
        }
    }

    /* region DB Synchronization Functions */

    fun syncChanges(changes: ScheduleChangesGroup): Boolean {
        var altered = 0
        changes.forEach {
            if (syncChanges(it)) altered++
        }

        return altered == changes.size
    }

    fun syncChanges(changes: GeneralDayReplacementsModel): Boolean {
        var altered = 0
        transaction {
            for (targetChange in changes.changes) {
                val hash = targetChange.hashCode()
                if (ScheduleReplacements.select { ScheduleReplacements.commitHash eq hash }.empty()) {
                    try {
                        if (insertNewChangeToDb(targetChange)) altered++
                    }
                    catch (exception: Exception) {
                        println("ERROR:\n\tObject Info: ${targetChange?.targetGroup}/${targetChange?.changesDate.toString()}." +
                                        "\n${exception.message}.")
                    }
                }
            }
        }

        val date = changes.getAtomicDateValues()
        println("Info:\n\tCreated $altered new replacement entries for ${date.first}.${date.second}.${date.third}!.")
        return altered > 0
    }

    private fun insertNewChangeToDb(change: TargetedDayReplacementsModel?): Boolean {
        if (change != null) {
            val newReplacementId = createNewReplacementInstance(change)
            createNewLessonInstances(change.changedLessons, newReplacementId.value, null, false)

            return true
        }
        return false
    }

    private fun createNewReplacementInstance(change: TargetedDayReplacementsModel) = ReplacementEntity.new {
        id
        commitHash = change.hashCode()

        targetGroup = change.targetGroup!!
        replacementDate = LocalDate.ofInstant(change.changesDate!!.toInstant(),
                                              change.changesDate!!.timeZone
                                                  .toZoneId())
        isAbsolute = change.isAbsolute
    }.id

    fun syncFinalSchedules(schedules: FinalScheduleGroup): Boolean {
        var altered = 0
        schedules.forEach { if (syncFinalSchedules(it)) altered++ }

        return altered == schedules.size
    }

    fun syncFinalSchedules(schedule: GeneralFinalDayScheduleModel): Boolean {
        var altered = 0
        transaction {
            for (targetSchedule in schedule) {
                val hash = targetSchedule.hashCode()
                if (FinalSchedules.select { FinalSchedules.commitHash eq hash }.empty()) {
                    try {
                        if (insertNewFinalScheduleToDb(targetSchedule))
                            altered++
                    }
                    catch (ex: Exception) {
                        print("ERROR:\n\tObject Info: ${targetSchedule.targetGroup}/${targetSchedule.scheduleDate.toString()}" +
                                      "\n\t${ex.message}.")
                    }
                }
            }
        }

        val date = schedule.getAtomicDateValues()
        println("Info:\n\tCreated $altered new final schedule entries for ${date.first}.${date.second}.${date.third}!.")
        return altered > 0
    }

    private fun insertNewFinalScheduleToDb(targetSchedule: TargetedFinalDayScheduleModel?): Boolean {
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
        return FinalScheduleEntity
            .new {
                id
                commitHash = hash

                targetGroup = targetSchedule.targetGroup!!
                scheduleDate = LocalDate.ofInstant(targetSchedule.scheduleDate!!.toInstant(),
                                                   targetSchedule.scheduleDate.timeZone
                                                       .toZoneId())
            }.id
    }

    private fun createNewLessonInstances(lessons: List<LessonModel>, newReplacementId: Int?, newFinalScheduleId: Int?,
                                         exceptionOnEmptyLessonName: Boolean = true) {
        for (lesson in lessons) {
            Lesson.new {
                id

                number = lesson.number!!
                name = if (exceptionOnEmptyLessonName) lesson.name!! else lesson.name ?: "Нет"
                teacher = lesson.teacher
                place = lesson.place
                isChanged = true // Hack: Old replacements (Changes) assets contain 'false' in this property.

                replacementId = newReplacementId
                scheduleId = newFinalScheduleId
            }
        }
    }
    /* endregion */

    /* region Data-Access Objects */

    object Lessons : IntIdTable("lesson") {
        var number = integer("number")

        var name = text("name")
        var teacher = text("teacher")
            .nullable()
        var place = text("place")
            .nullable()
        var isChanged = bool("is_changed")
            .nullable()

        var scheduleId = integer("schedule_id")
            .references(FinalSchedules.id)
            .nullable()
        var replacementId = integer("replacement_id")
            .references(ScheduleReplacements.id)
            .nullable()
    }

    object FinalSchedules : IntIdTable("final_schedule") {
        var commitHash = integer("commit_hash")

        var targetGroup = text("target_group")
        var scheduleDate = date("schedule_date")
    }

    object ScheduleReplacements : IntIdTable("schedule_replacement") {
        var commitHash = integer("commit_hash")

        var isAbsolute = bool("is_absolute")
        var targetGroup = text("target_group")
        var replacementDate = date("replacement_date")
    }
    /* endregion */

    /* region Companion */

    companion object {

        val instance = ScheduleDataContext()

        private const val DB_Name = "UksivtSchedule_Lite"

        private fun getDBKind() = DBKind.POSTGRESQL
    }
    /* endregion */
}
