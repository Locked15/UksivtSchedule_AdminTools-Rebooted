package controller.db

enum class DBKind {

    POSTGRESQL,

    MYSQL,

    SQLITE;

    fun getSpecificUserSecretFileName() = when (this) {
        POSTGRESQL -> "postgres-connection.secret"
        MYSQL -> "mysql-connection.secret"
        SQLITE -> "sqlite-connection.secret"
    }

    fun getSpecificDataBaseAddressConnector() = when (this) {
        POSTGRESQL -> "jdbc:postgresql"
        MYSQL -> "jdbc:mysql"
        SQLITE -> "jdbc:sqlite"
    }

    fun getSpecificDataBaseDataDriver() = when (this) {
        POSTGRESQL -> "org.postgresql.Driver"
        MYSQL -> "com.mysql.cj.jdbc.Driver"
        SQLITE -> "org.sqlite.JDBC"
    }
}
