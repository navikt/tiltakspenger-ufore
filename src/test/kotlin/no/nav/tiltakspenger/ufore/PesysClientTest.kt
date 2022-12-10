package no.nav.tiltakspenger.ufore

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

internal class PesysClientTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun hentUføre() {
        val mockEngine = MockEngine {
            respond(
                content = """{"harUforegrad":true,"datoUfor":"2022-02-01","virkDato":"2022-09-01"}""".trimMargin(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val client = httpClientTest(mockEngine)
        val pesysClient = PesysClient(client) { "a token to be used for tests" }
        runTest {
            val response = pesysClient.hentUføre("ident", "fom", "tom", "behovId")
            assertTrue(response.harUforegrad)
            assertEquals(LocalDate.of(2022, Month.FEBRUARY, 1), response.datoUfor)
            assertEquals(LocalDate.of(2022, Month.SEPTEMBER, 1), response.virkDato)
        }
    }
}
