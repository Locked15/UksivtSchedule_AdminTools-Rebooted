package controller.db.pgsql.schedule.lite

import controller.db.DBKind
import model.data.schedule.base.TargetCycle
import model.environment.db.DBConnectionModel


class ScheduleConfig(val targetCycle: TargetCycle, val connectionModel: DBConnectionModel) {

    companion object {

        const val DB_Name = "UksivtSchedule_Lite"

        fun getDBKind() = DBKind.POSTGRESQL
    }
}
