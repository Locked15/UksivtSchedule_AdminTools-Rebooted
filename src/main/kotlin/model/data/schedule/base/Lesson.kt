package model.data.schedule.base


/**
 * Represents one lesson of the schedule.
 *
 * @param number This lesson number.
 * @param name This lesson name.
 * @param teacher This lesson teacher (written in a short format if it possible).
 * @param place Place, where this lesson would be arranged (in a string format).
 */
class Lesson(var number: Int?, var name: String?, var teacher: String?, var place: String?, var isChanged: Boolean) :
        Comparable<Lesson> {

    /* region Constructors */

    /**
     * Default constructor of the class.
     * Created to make code simpler to understand.
     *
     * Write all properties as 'NULL'.
     */
    constructor() : this(null, null, null, null, false)

    /**
     * Constructor, that initialize lesson number.
     * Can be found useful, when you need late property initialization.
     *
     * Sets the [number] property to argument value.
     * Other properties are set to 'NULL'.
     */
    constructor(number: Int?) : this(number, null, null, null, false)

    /**
     * Constructor that initializes lesson number and name.
     * Can be useful, when you need to work with 'Practise' or 'Credit' schedules.
     *
     * Sets the [number] and [name] property to arguments value.
     * Other properties are set to 'NULL'.
     */
    constructor(number: Int, name: String) : this(number, name, null, null, false)

    /**
     * Back compatibility.
     * To not update all instances with 'false' in 'isChanged' property.
     *
     * Sets the [number] and [name] property to arguments value.
     * Other properties are set to 'NULL'.
     */
    constructor(number: Int?, name: String?, teacher: String?, place: String?) : this(number, name, teacher, place,
                                                                                      false)
    /* endregion */

    /* region Functions */

    /**
     * Allow comparing two instances of this class.
     *
     * @param other Another instance to compare with current.
     */
    override fun compareTo(other: Lesson): Int {
        return if (number!! > other.number!!) {
            1
        }
        else if (number!! == other.number!!) {
            0
        }
        else {
            -1
        }
    }
    /* endregion */
}
