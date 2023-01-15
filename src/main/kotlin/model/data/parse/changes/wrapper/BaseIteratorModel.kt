package model.data.parse.changes.wrapper

import model.data.schedule.TargetChangesOfDay


/**
 * Represents a first iteration level (base) data model of Word document parse.
 * This one must be placed outside cycles (before getting target table).
 *
 * @param cycleStopper  Used to break the cycle, when targetChangesOfDay to a target group are find and fully parsed.
 * @param listenToChanges  Defines, when parser found target group declaration header inside a targetChangesOfDay document.
 *                         If this sets to true, iterator will parse targetChangesOfDay elements
 *                         (i.e. lesson names, teachers, places, etc).
 */
class BaseIteratorModel(var cycleStopper: Boolean, var listenToChanges: Boolean, var targetChangesOfDay: TargetChangesOfDay)
