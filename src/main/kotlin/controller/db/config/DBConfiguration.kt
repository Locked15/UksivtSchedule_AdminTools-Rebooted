package controller.db.config

import controller.db.DBKind


class DBConfiguration(private val dbKind: DBKind) : BasicDBConfiguration() {

    fun getConfiguration() : HashMap<String, String>? {
        return getConfiguration(dbKind)
    }
}
