package no.nav.tiltakspenger.ufore

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.jackson.*
import mu.KotlinLogging

class HttpClient(config: CIOEngineConfig.() -> Unit = {}) {
    private val log = KotlinLogging.logger {}
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                registerModule(JavaTimeModule())
            }
        }
        expectSuccess = true
        HttpResponseValidator {
            validateResponse { response ->
                val statusCode = response.status
                log.info { "Fikk $statusCode fra Pesys" }
            }
            handleResponseExceptionWithRequest { exception, _ ->
                when (exception) {
                    is ClientRequestException -> {
                        val exceptionResponse = exception.response
                        log.warn { "Fikk client ${exceptionResponse.status} fra Pesys" }
                    }

                    is ServerResponseException -> {
                        val exceptionResponse = exception.response
                        log.warn { "Fikk server ${exceptionResponse.status} fra Pesys" }
                    }

                    else -> {
                        log.warn { "Fikk uhåndtert feil fra Pesys: $exception" }
                        return@handleResponseExceptionWithRequest
                    }
                }
            }
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        engine(config)
    }
}
