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
fun httpClient(engine: HttpClientEngine = CIO.create(), config: HttpClientEngineConfig.() -> Unit = {}) =
    HttpClient(engine) {
        LOG.info { "Setting up httpclient with config: $config" }
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
        engine(config)
        LOG.info { "Returning httpclient $this" }
    }
