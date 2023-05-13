package model.data.schedule.base

import controller.view.Logger
import model.environment.log.LogLevel
import model.entity.schedule.lite.base.TargetCycle as TargetCycleEntity
import org.jetbrains.exposed.dao.id.EntityID


class TargetCycle(private val year: Int, private val semester: Int) {

    fun getTargetCycleId(): EntityID<Int>? {
        val allCycles = TargetCycleEntity.all().toList()
        return allCycles.firstOrNull { it.year == year && it.semester == semester }?.id
    }

    companion object {

        private const val DEFAULT_YEAR = 2023

        private const val DEFAULT_SEMESTER = 2

        fun createInstanceByStringQuery(query: String?): TargetCycle {
            val values = query?.split('/')
            return if (values != null && values.size == 2) {
                val year = values[0].toIntOrNull()
                val semester = values[1].toIntOrNull()
                if (year == null || semester == null) {
                    Logger.logMessage(LogLevel.INFORMATION, "One of values on TargetCycle creation was 'NULL'." +
                            "\n\tDefault values will be used")
                }

                TargetCycle(year ?: DEFAULT_YEAR, semester ?: DEFAULT_SEMESTER)
            }
            else {
                Logger.logMessage(LogLevel.INFORMATION, "TargetCycle creation by string query was faulted." +
                                  "\n\tDefault values will be used")
                TargetCycle(DEFAULT_YEAR, DEFAULT_SEMESTER)
            }
        }
    }
}
