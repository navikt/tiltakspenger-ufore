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
fun httpClient(engine: HttpClientEngine = CIO.create(), useProxy: Boolean = false) =
    HttpClient(engine) {
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
        if (useProxy) {
            System.getenv("HTTP_PROXY")?.let {
                LOG.info("Setter opp proxy mot $it")
                engine {
                    proxy = ProxyBuilder.http(it)
                }
            }
        }
    }
