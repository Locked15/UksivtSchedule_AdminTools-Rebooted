package model.data.change.group

import model.data.change.BasicScheduleChanges
import model.data.change.day.GeneralChangesOfDay


/**
 * Wrapper-class for group of [GeneralChangesOfDay].
 */
class ScheduleDayChangesGroup(c: MutableCollection<out GeneralChangesOfDay>) : ArrayList<GeneralChangesOfDay>(c),
                                                                               BasicScheduleChanges
