package model.entity.schedule.lite.base

import model.data.schedule.base.Teacher as TeacherModel
import controller.db.pgsql.schedule.lite.ScheduleDataContext
import controller.view.Logger
import model.environment.log.LogLevel
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID


class Teacher(id: EntityID<Int>) : Entity<Int>(id) {

    /* region Properties */

    var surname: String by ScheduleDataContext.Teachers.surname

    var name: String? by ScheduleDataContext.Teachers.name

    var patronymic: String? by ScheduleDataContext.Teachers.patronymic
    /* endregion */

    /* region Functions */

    /**
     * Completes comparison with another teacher model (not entity, because entities are created at DB interaction level).
     *
     * Results may vary:
     * * -1 — Entries ain't equal at all (surnames are different);
     * * 0 — Entries partially equal (surnames are equal, but secondary info (name, patronymic) are different);
     * * 1 — Entries are fully equal.
     */
    fun compareWithOtherModel(another: TeacherModel): Int {
        val normalizedSurnames = Pair(normalizeTeacherName(surname), normalizeTeacherName(another.surname))
        val surnamesAreEqual = normalizedSurnames.first.equals(normalizedSurnames.second, true)
        val namesAndPatronymicsAreEqual = name.equals(another.name, true) && patronymic.equals(another.patronymic, true)

        return if (!surnamesAreEqual) -1
        else if (!namesAndPatronymicsAreEqual) 0
        else 1
    }

    private fun normalizeTeacherName(target: String) = target.replace('ё', 'е')

    fun updateSecondaryFields(newTeacherData: TeacherModel) {
        val messages = mutableListOf("Teacher ($id: $surname) entry update:")
        if (newTeacherData.name != null || newTeacherData.patronymic != null) {
            val nameUpdateCheck = shouldUpdateSecondaryField(name, newTeacherData.name)
            if (nameUpdateCheck) {
                messages.add("Name is updated: $name -> ${newTeacherData.name}.")
                name = newTeacherData.name
            }

            val patronymicUpdateCheck = shouldUpdateSecondaryField(patronymic, newTeacherData.patronymic)
            if (patronymicUpdateCheck) {
                messages.add("Patronymic is updated: $patronymic —> ${newTeacherData.name}.")
                patronymic = newTeacherData.patronymic
            }

            if (messages.size > 1) Logger.logMessage(LogLevel.DEBUG, messages.joinToString("\n\t"))
        }
    }

    fun createUnGenderedInstance() = TeacherModel(surname.trimEnd('а'), name, patronymic)
    /* endregion */

    /* region Internal Functions */

    /**
     * Makes a checking process, to prevent updating teacher entity with Empty or Irrelevant data.
     *
     * Check is:
     * * [currentOne] and [newOne] ARE NOT equal, ignoring case sensitivity;
     * * [newOne] IS NOT null or empty.
     * * [currentOne] IS null or empty.
     */
    private fun shouldUpdateSecondaryField(currentOne: String?, newOne: String?) = !currentOne.equals(newOne, true) &&
            (!newOne.isNullOrEmpty() && currentOne.isNullOrEmpty())
    /* endregion */

    companion object : EntityClass<Int, Teacher>(ScheduleDataContext.Teachers)
}
