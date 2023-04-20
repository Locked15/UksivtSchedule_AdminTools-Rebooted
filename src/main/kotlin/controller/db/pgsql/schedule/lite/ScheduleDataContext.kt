package controller.db.pgsql.schedule.lite

import controller.db.DBKind
import controller.db.config.DBConfigurator
import model.data.change.group.ScheduleChangesGroup
import model.data.schedule.common.result.group.FinalScheduleGroup
import model.data.change.day.GeneralChangesOfDay as GeneralDayReplacementsModel
import model.data.schedule.common.result.day.GeneralFinalDaySchedule as GeneralFinalDayScheduleModel

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileNotFoundException


class ScheduleDataContext private constructor() {

    private val dbConnection: Database

    init {
        val rawConfiguration = DBConfigurator(getDBKind()).getRawConfiguration()

        if (rawConfiguration != null) {
            val connectionModel = getConnectionModel(DB_Name, rawConfiguration)
            dbConnection = Database.connect(
                    "${getDBKind().getSpecificDataBaseAddressConnector()}://${connectionModel.dbAddress}/$DB_Name",
                    driver = getDBKind().getSpecificDataBaseDataDriver(),
                    user = connectionModel.userName,
                    password = connectionModel.userPassword
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
    /* endregion */

    /* region Data-Access Objects */

    object Teachers : IntIdTable("teachers") {
        var surname = text("surname")

        var name = text("name")
            .nullable()
        var patronymic = text("patronymic")
            .nullable()
    }

    object Lessons : IntIdTable("lesson") {
        var number = integer("number")

        var name = text("name")
        var teacherId = integer("teacher_id")
            .references(Teachers.id)
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

    object TargetCycles : IntIdTable("target_cycle") {
        var year = integer("year")
        var semester = integer("semester")
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
