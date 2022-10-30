package model.data.parse.schedule

import model.data.schedule.base.day.Day
import org.apache.poi.ss.util.CellAddress


class DayColumnInfo(val coordinates: CellAddress, val currentDay: Day) {

	/* region Constructors */
	
	/**
	 * Main constructor of the class.
	 *
	 * Uses send [x] and [y] points to create [address object][coordinates].
	 */
	constructor(x: Int, y: Int, day: Day) : this(CellAddress(x, y), day)
	/* endregion */
	
	/* region Companion */
	
	companion object {
		
		/**
		 * Tries to find [column info][DayColumnInfo] in [an available array][coordinates].
		 * Search by send [day] to function.
		 *
		 * Return 'NULL' if there is no match element.
		 */
		fun getInfoByDay(day: Day, coordinates: List<DayColumnInfo>): DayColumnInfo? {
			for (coordinate in coordinates) {
				if (day.index == coordinate.currentDay.index) {
					return coordinate
				}
			}
			
			// If we reach this point, so search doesn't find any matches.
			return null
		}
	}
	/* endregion */
}
