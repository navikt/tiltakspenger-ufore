package no.nav.tiltakspenger.ufore

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.jackson.*
import mu.KotlinLogging

val LOG = KotlinLogging.logger {}
fun httpClientCIO(config: CIOEngineConfig.() -> Unit = {}) =
    HttpClient(CIO) { engine(config) }.medDefaultConfig()

fun httpClientMedProxy() = httpClientCIO {
    Configuration.httpProxy()?.let {
        LOG.info("Setter opp proxy mot $it")
        this.proxy = ProxyBuilder.http(it)
    }
}

fun httpClientGeneric(engine: HttpClientEngine) = HttpClient(engine).medDefaultConfig()

private fun HttpClient.medDefaultConfig() = this.config {
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerModule(JavaTimeModule())
        }
    }
    expectSuccess = true
    install(Logging) {
        level = LogLevel.INFO
    }
}
