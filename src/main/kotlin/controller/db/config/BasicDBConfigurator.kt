package controller.db.config

import com.fasterxml.jackson.core.JsonFactoryBuilder
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import controller.db.DBKind
import controller.io.service.PathResolver
import controller.view.Logger
import java.io.IOException


abstract class BasicDBConfigurator {

    companion object {

        @JvmStatic
        protected fun getRawConfiguration(dbKind: DBKind): HashMap<String, String>? {
            val userSecretsFile = PathResolver.resolvePath(PathResolver.applicationResourcePath,
                                                           listOf(PathResolver.generalUserSecretsFolderName,
                                                                  PathResolver.secretConnectionsDataFolderName,
                                                                  dbKind.getSpecificUserSecretFileName())).toFile()
            return try {
                val serializer = getSerializer()
                return serializer.readValue<HashMap<String, String>>(userSecretsFile)
            }
            catch (exception: IOException) {
                Logger.logException(exception, 1, "Exception occurred on configuration file reading")
                null
            }
        }

        private fun getSerializer(): ObjectMapper {
            val builder = JsonFactoryBuilder()
                .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)

            return ObjectMapper(builder.build())
        }
    }
}
