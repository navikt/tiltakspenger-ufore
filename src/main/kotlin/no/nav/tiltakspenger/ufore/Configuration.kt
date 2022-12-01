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
        "SERVICEUSER_TPTS_USERNAME" to System.getenv("SERVICEUSER_TPTS_USERNAME"),
        "SERVICEUSER_TPTS_PASSWORD" to System.getenv("SERVICEUSER_TPTS_PASSWORD"),
    )
    private val defaultProps = ConfigurationMap(rapidsAndRivers + otherDefaultProperties)
    private val localProps = ConfigurationMap(
        mapOf(
            "STS_URL" to "",
            "application.profile" to Profile.LOCAL.toString(),
            "PESYS_UFØRE_URL" to ""
        )
    )
    private val devProps = ConfigurationMap(
        mapOf(
            "STS_URL" to "https://sts-q1.preprod.local/SecurityTokenServiceProvider/",
            "application.profile" to Profile.DEV.toString(),
            "PESYS_UFØRE_URL" to "http://pensjon-pen-q1.teampensjon/pen/springapi/sak/harUforegrad"
        )
    )
    private val prodProps = ConfigurationMap(
        mapOf(
            "STS_URL" to "https://sts.adeo.no/SecurityTokenServiceProvider/",
            "application.profile" to Profile.PROD.toString(),
            "PESYS_UFØRE_URL" to "http://pensjon-pen.pensjondeployer/pen/springapi/sak/harUforegrad"
        )
    )

    private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-fss" -> systemProperties() overriding EnvironmentVariables overriding devProps overriding defaultProps
        "prod-fss" -> systemProperties() overriding EnvironmentVariables overriding prodProps overriding defaultProps
        else -> systemProperties() overriding EnvironmentVariables overriding localProps overriding defaultProps
    }

    data class PesysConfig(
        val pesysUføreUrl: String = config()[Key("PESYS_UFØRE_URL", stringType)],
        val stsUrl: String = config()[Key("STS_URL", stringType)],
        val stsUsername: String = config()[Key("SERVICEUSER_TPTS_USERNAME", stringType)],
        val stsPassword: String = config()[Key("SERVICEUSER_TPTS_PASSWORD", stringType)],
    )
}

enum class Profile {
    LOCAL, DEV, PROD
}
