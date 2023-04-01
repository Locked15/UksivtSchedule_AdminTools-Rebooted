package model.data.change.group

import model.data.change.BasicScheduleChanges
import model.data.change.day.GeneralChangesOfDay


class ScheduleChangesGroup(c: MutableCollection<out GeneralChangesOfDay>) : ArrayList<GeneralChangesOfDay>(c),
                                                                            BasicScheduleChanges
