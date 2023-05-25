package controller.db.pgsql.schedule.lite.helper.main.base

interface BaseCommonHelper<T> {
    fun insertNewEntityIntoDB(targetCycleId: Int?) : Boolean
}
