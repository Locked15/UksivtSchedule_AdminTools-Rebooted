package controller.db.pgsql.schedule.lite.helper

import controller.db.pgsql.schedule.lite.ScheduleConfig
import model.data.schedule.base.TargetCycle
import model.environment.db.DBConnectionModel


fun getConfigurationModel(configurationParameters: HashMap<String, String>): ScheduleConfig {
    val targetCycle = configurationParameters.getOrElse("DB.${ScheduleConfig.DB_Name}.TargetCycle") {
        configurationParameters.getOrElse("DB.General.TargetCycle") {
            null
        }
    }
    val connectionModel = getConnectionModel(configurationParameters)

    return ScheduleConfig(TargetCycle.createInstanceByStringQuery(targetCycle), connectionModel)
}

private fun getConnectionModel(configurationParameters: HashMap<String, String>): DBConnectionModel {
    //? Here we take DB address. Or use 'localhost:5432' if it's not available.
    val addressWithPort = configurationParameters.getOrElse("DB.${ScheduleConfig.DB_Name}.Address") {
        configurationParameters.getOrElse("DB.General.Address") {
            "localhost:5432"
        }
    }
    //? Here we take DB user. Or use 'postgres' if it's not specified.
    val user = configurationParameters.getOrElse("DB.${ScheduleConfig.DB_Name}.User") {
        configurationParameters.getOrElse("DB.General.User") {
            "postgres"
        }
    }
    //? And here we take the user password. Or use 'Unknown' if it's not specified.
    val password = configurationParameters.getOrElse("DB.${ScheduleConfig.DB_Name}.Password") {
        configurationParameters.getOrElse("DB.General.Password") {
            "Unknown"
        }
    }

    return DBConnectionModel(addressWithPort, ScheduleConfig.DB_Name, user, password)
}
