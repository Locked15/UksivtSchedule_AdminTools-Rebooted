package controller.db.pgsql.schedule.lite.helper.main.base

import org.jetbrains.exposed.dao.id.EntityID

abstract class BaseMainHelper<T>(val newItem: T?) : BaseCommonHelper<T> {

    protected fun getLogMessageForTeachersAltered(alteredTeachers: Pair<Int, List<Pair<Int?, String?>>>,
                                                  targetString: String): String {
        return """New teacher entries: ${alteredTeachers.first} for $targetString
            (${joinAlteredTeachersListToString(alteredTeachers.second)})
        """.trimIndent()
    }

    private fun joinAlteredTeachersListToString(info: List<Pair<Int?, String?>>): String {
        return info.joinToString(", ") {
            "${it.second} (ID: ${it.first})"
        }
    }

    protected abstract fun createNewEntityEntry(creationEntry: T?, targetCycleId: Int?): EntityID<Int>
}
