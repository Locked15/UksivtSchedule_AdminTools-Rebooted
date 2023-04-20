package model.data.schedule.base

class Teacher(var surname: String, var name: String?, var patronymic: String?) {

    val fullName: String
        get() {
            return "$surname $name $patronymic"
        }

    constructor() : this("", null, null)

    fun isShortEntry() = name == null || patronymic == null

    companion object {

        fun normalizeTeacherName(rawName: String) = rawName.replace('.', ' ').replace(',', ' ').trim()

        fun createTeacherModelByNormalizedName(normalizedName: String) : Teacher {
            val atomicNameValues = normalizedName.split(' ')
            val toReturn = Teacher()
            toReturn.surname = atomicNameValues[0]

            return when (atomicNameValues.size) {
                //? In this case, we have only surname data (i.e. 'Голуб').
                1 -> {
                    toReturn
                }
                //? In this case, we have two values. Second value can be varied (only name, or 'name + patron') w-out space).
                2 -> {
                    toReturn.name = atomicNameValues[1][0].toString()

                    //? The first case (only name, i.e. 'Голуб И') can be checked with second value size check (it'll be 1).
                    if (atomicNameValues[1].length == 1) {
                        toReturn
                    }
                    //? Otherwise, second value is just a short variant of 'name + patron' (i.e. 'Голуб ИА').
                    else {
                        toReturn.patronymic = atomicNameValues[1][2].toString()
                        toReturn
                    }
                }

                //? And, we also can have full value.
                else -> {
                    //! But it also calls if we have more than 3, so warn about it.
                    if (atomicNameValues.size > 3)
                        println("WARNING:\n\tTeacher atomic name values have length more than 3!")

                    toReturn.name = atomicNameValues[1]
                    toReturn.patronymic = atomicNameValues[2]
                    toReturn
                }
            }
        }
    }
}
