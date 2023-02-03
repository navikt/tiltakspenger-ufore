package no.nav.tiltakspenger.ufore

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

object Configuration {
    val rapidsAndRivers = mapOf(
        "RAPID_APP_NAME" to "tiltakspenger-ufore",
        "KAFKA_BROKERS" to System.getenv("KAFKA_BROKERS"),
        "KAFKA_CREDSTORE_PASSWORD" to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
        "KAFKA_TRUSTSTORE_PATH" to System.getenv("KAFKA_TRUSTSTORE_PATH"),
        "KAFKA_KEYSTORE_PATH" to System.getenv("KAFKA_KEYSTORE_PATH"),
        "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
        "KAFKA_RESET_POLICY" to "latest",
        "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-ufore-v1",
    )
    private val otherDefaultProperties = mapOf(
        "AZURE_APP_CLIENT_ID" to System.getenv("AZURE_APP_CLIENT_ID"),
        "AZURE_APP_CLIENT_SECRET" to System.getenv("AZURE_APP_CLIENT_SECRET"),
        "AZURE_APP_WELL_KNOWN_URL" to System.getenv("AZURE_APP_WELL_KNOWN_URL"),
        "HTTP_PROXY" to System.getenv("HTTP_PROXY")
    )
    private val defaultProps = ConfigurationMap(rapidsAndRivers + otherDefaultProperties)
    private val localProps = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.LOCAL.toString(),
            "PESYS_UFØRE_URL" to "",
            "PESYS_SCOPE" to "api://localhost:/.default",
        )
    )
    private val devProps = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.DEV.toString(),
            "PESYS_UFØRE_URL" to "http://pensjon-pen-q1.nais.preprod.local/pen/springapi/sak/harUforegrad",
            "PESYS_SCOPE" to "api://dev-fss.teampensjon.pensjon-pen-q1/.default",
        )
    )
    private val prodProps = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.PROD.toString(),
            "PESYS_UFØRE_URL" to "http://pensjon-pen.pensjondeployer/pen/springapi/sak/harUforegrad",
            "PESYS_SCOPE" to "api://prod-fss.pensjondeployer.pensjon-pen/.default",
        )
    )

    private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-fss" -> systemProperties() overriding EnvironmentVariables overriding devProps overriding defaultProps
        "prod-fss" -> systemProperties() overriding EnvironmentVariables overriding prodProps overriding defaultProps
        else -> systemProperties() overriding EnvironmentVariables overriding localProps overriding defaultProps
    }

    data class OauthConfig(
        val scope: String = config()[Key("PESYS_SCOPE", stringType)],
        val clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        val clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        val wellknownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)]
    )

    @JvmInline
    value class PesysConfig(val pesysUføreUrl: String = config()[Key("PESYS_UFØRE_URL", stringType)])

    fun httpProxy(): String? = config().getOrNull(Key("HTTP_PROXY", stringType))
}

enum class Profile {
    LOCAL, DEV, PROD
}
