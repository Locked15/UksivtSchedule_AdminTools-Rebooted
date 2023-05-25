package controller.db.pgsql.schedule.lite.helper.main.base

import org.jetbrains.exposed.dao.id.EntityID

abstract class BaseMainHelper<T>(val newItem: T?) : BaseCommonHelper<T> {
    protected abstract fun createNewEntityEntry(creationEntry: T?, targetCycleId: Int?): EntityID<Int>
}
