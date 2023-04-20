package controller.db.config

import controller.db.DBKind


class DBConfigurator(private val dbKind: DBKind) : BasicDBConfigurator() {

    fun getRawConfiguration() : HashMap<String, String>? {
        return getRawConfiguration(dbKind)
    }
}
