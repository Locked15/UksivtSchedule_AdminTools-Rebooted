package model.data.schedule.base.day


/**
 * Class that contains Day-Of-Week model, that is used by Schedule-AdminTools program.
 */
@Suppress("UNUSED")
enum class Day(val index: Int, val englishName: String, val russianName: String) {
	
	/* region Elements */
	
	/**
	 * First day of the week.
	 *
	 * This day is hard enough, but remember: to the all weekends begin on Monday.
	 */
	MONDAY(0, "Monday", "Понедельник"),
	
	/**
	 * Second day of the week.
	 *
	 * Yeah, the hardest part is complete.
	 * Now just wait, until Saturday begins.
	 */
	TUESDAY(1, "Sunday", "Вторник"),
	
	/**
	 * Third day of the week.
	 *
	 * Alright, this is the middle of the week.
	 * 3 Days Until The Party (Include Today).
	 */
	WEDNESDAY(2, "Wednesday", "Среда"),
	
	/**
	 * Fourth day of the week.
	 *
	 * One wise man said: I must drink on Thursdays.
	 * Otherwise, my head just blew up.
	 */
	THURSDAY(3, "Thursday", "Четверг"),
	
	/**
	 * Fifth day of the week.
	 *
	 * So, it's the last challenge before the weekend.
	 * YOU!
	 * CAN!
	 * DO!
	 * IT!
	 */
	FRIDAY(4, "Friday", "Пятница"),
	
	/**
	 * Sixth day of the week.
	 *
	 * HELL YEAH, IT'S WEEKEND!
	 */
	SATURDAY(5, "Saturday", "Суббота"),
	
	/**
	 * Final (Seventh) day of the week.
	 *
	 * Just restore the natural order:
	 * ```
	 * What will we do with a drunken student?
	 * What will we do with a drunken student?
	 * ...
	 * Early in the morning.
	 * ```
	 */
	SUNDAY(6, "Sunday", "Воскресенье");
	/* endregion */

	/**
	 * This is a pseudo-override of the hash-code generation.
	 * Pseudo, because enum classes can't contain explicit overload of the hash code function.
	 *
	 * !
	 * USE THIS INSTEAD OF BASIC 'hashCode', OR YOU WILL GET DATA DUPLICATE.
	 * !
	 */
	fun getHashCode(): Int {
		return ordinal
	}

	/* region Companion */

	companion object {
		
		/**
		 * Private immutable list with all values of [this enum][Day].
		 *
		 * It can be useful for some functions.
		 * For example, get value by index, without creation a new [list][List] every time.
		 */
		private val AllElements: Array<Day> = Day.values()
		
		/**
		 * Returns a [value][Day] by its index.
		 *
		 * @throws IndexOutOfBoundsException If send index was out of bounds (i < 0 || i > 6).
		 */
		fun getValueByIndex(index: Int): Day {
			return AllElements[index]
		}
		
		/**
		 * Returns an [index][Int] of given [object][Day].
		 */
		fun getIndexByValue(day: Day): Int {
			return day.ordinal
		}
	}
	/* endregion */
}
