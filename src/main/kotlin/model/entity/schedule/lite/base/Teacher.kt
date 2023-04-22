package model.entity.schedule.lite.base

import model.data.schedule.base.Teacher as TeacherModel
import controller.db.pgsql.schedule.lite.ScheduleDataContext
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

    fun isShortEntry() = name == null && patronymic == null

    /**
     * Completes comparison with another teacher model (not entity, because entities are created at DB interaction level).
     *
     * Results may vary:
     * * -1 — Entries ain't equal at all (surnames are different);
     * * 0 — Entries partially equal (surnames are equal, but secondary info (name, patronymic) are different);
     * * 1 — Entries are fully equal.
     */
    fun compareWithOtherModel(another: TeacherModel): Int {
        val surnamesAreEqual = surname.equals(another.surname, true)
        val namesAndPatronymicsAreEqual = name.equals(another.name, true) && patronymic.equals(another.patronymic, true)

        return if (!surnamesAreEqual) -1
        else if (!namesAndPatronymicsAreEqual) 0
        else 1
    }

    fun updateSecondaryFields(template: TeacherModel) {
        if (template.name != null || template.patronymic != null) {
            println("INFO:\n\tTeacher ($surname) entry update:")
            if (template.name != null) {
                println("\t\tName is updated: $name -> ${template.name}.")
                name = template.name
            }
            if (template.patronymic != null) {
                println("\t\tPatronymic is updated: $patronymic —> ${template.name}.")
                patronymic = template.patronymic
            }
        }
    }
    /* endregion */

    companion object : EntityClass<Int, Teacher>(ScheduleDataContext.Teachers)
}
