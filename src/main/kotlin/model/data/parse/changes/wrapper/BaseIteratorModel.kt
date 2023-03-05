package model.data.parse.changes.wrapper

import model.data.changes.TargetedChangesOfDay


/**
 * Represents a first iteration level (base) data model of Word document parse.
 * This one must be placed outside cycles (before getting target table).
 *
 * @param cycleStopper  Used to break the cycle, when targetedChangesOfDay to a target group are find and fully parsed.
 * @param listenToChanges  Defines, when parser found target group declaration header inside a targetedChangesOfDay document.
 *                         If this sets to true, iterator will parse targetedChangesOfDay elements
 *                         (i.e. lesson names, teachers, places, etc).
 */
class BaseIteratorModel(var cycleStopper: Boolean, var listenToChanges: Boolean, var targetedChangesOfDay: TargetedChangesOfDay)
