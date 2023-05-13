package controller.db.pgsql.schedule.lite.helper

import controller.db.pgsql.schedule.lite.ScheduleConfig
import globalState
import model.data.schedule.base.TargetCycle
import model.environment.db.DBConnectionModel


private const val DEFAULT_ADDRESS = "localhost:5432"

private const val DEFAULT_USER_NAME = "postgres"

private const val DEFAULT_USER_PASSWORD = "Unknown"

fun getConfigurationModel(configurationParameters: HashMap<String, String>): ScheduleConfig {
    val defaultDBTargetCycleKey = "DB${globalState.dbTypeParam}.${ScheduleConfig.DB_Name}.TargetCycle"
    val targetCycle = configurationParameters.getOrElse(defaultDBTargetCycleKey) {
        configurationParameters.getOrElse("DB.General.TargetCycle") {
            null
        }
    }
    val connectionModel = getConnectionModel(configurationParameters)

    return ScheduleConfig(TargetCycle.createInstanceByStringQuery(targetCycle), connectionModel)
}

private fun getConnectionModel(configurationParameters: HashMap<String, String>): DBConnectionModel {
    val defaultDBAddressKey = "DB${globalState.dbTypeParam}.${ScheduleConfig.DB_Name}.Address"
    val defaultDBUserKey = "DB${globalState.dbTypeParam}.${ScheduleConfig.DB_Name}.User"
    val defaultDBPasswordKey = "DB${globalState.dbTypeParam}.${ScheduleConfig.DB_Name}.Password"

    //? Here we take DB address. Or use 'localhost:5432' if it's not available.
    val addressWithPort = configurationParameters.getOrElse(defaultDBAddressKey) {
        configurationParameters.getOrElse("DB.General.Address") {
            DEFAULT_ADDRESS
        }
    }
    //? Here we take DB user. Or use 'postgres' if it's not specified.
    val user = configurationParameters.getOrElse(defaultDBUserKey) {
        configurationParameters.getOrElse("DB.General.User") {
            DEFAULT_USER_NAME
        }
    }
    //? And here we take the user password. Or use 'Unknown' if it's not specified.
    val password = configurationParameters.getOrElse(defaultDBPasswordKey) {
        configurationParameters.getOrElse("DB.General.Password") {
            DEFAULT_USER_PASSWORD
        }
    }

    return DBConnectionModel(addressWithPort, ScheduleConfig.DB_Name, user, password)
}
