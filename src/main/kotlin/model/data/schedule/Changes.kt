package model.data.schedule

import model.data.schedule.base.Lesson


/**
 * Class, that encapsulates Changes for the schedule.
 *
 * Earlier, it wasn't a class, just a combination of list with changes and boolean with absolute determination.
 */
class Changes(val changedLessons: MutableList<Lesson>, var absolute: Boolean) {

    /* region Constructors */

    /**
     * Additional constructor, that write default values to properties.
     *
     * Info: [changedLessons] set to an empty list, [absolute] to false.
     */
    constructor() : this(mutableListOf<Lesson>(), false)
    /* endregion */

    /* region Functions */

    /**
     * Returns string representation of the current object.
     */
    override fun toString(): String {
        val builder = StringBuilder(changedLessons.size)
        for (lesson in changedLessons) {
            builder.append("${lesson.number} — ${lesson.name}.")
        }

        return String.format(STRING_BODY_TEMPLATE, absolute, builder.toString())
    }
    /* endregion */

    /* region Companion */

    companion object {

        /**
         * Contains template for string-formatting instances of [Changes] objects.
         */
        const val STRING_BODY_TEMPLATE =
            """
                IsAbsolute: %b;
                Changes:
                {
                    %s.
                }
            """
    }
    /* endregion */
}
