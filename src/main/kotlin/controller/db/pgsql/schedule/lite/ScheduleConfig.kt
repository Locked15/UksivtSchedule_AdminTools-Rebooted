package controller.db.pgsql.schedule.lite

import controller.db.DBKind
import globalState
import model.data.schedule.base.TargetCycle
import model.environment.db.DBConnectionModel


class ScheduleConfig(val targetCycle: TargetCycle, val connectionModel: DBConnectionModel) {

    companion object {

        fun getDBName() = globalState.dbNameParam

        fun getDBKind() = DBKind.POSTGRESQL
    }
}
