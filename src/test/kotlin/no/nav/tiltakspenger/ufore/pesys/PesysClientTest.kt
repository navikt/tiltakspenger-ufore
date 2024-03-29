package no.nav.tiltakspenger.ufore.pesys

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import no.nav.tiltakspenger.ufore.httpClientGeneric
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

internal class PesysClientTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Pesys svarer 200 OK og personen har uføregrad`() {
        val mockEngine = MockEngine {
            respond(
                content = """{"harUforegrad":true,"datoUfor":"2022-02-01","virkDato":"2022-09-01"}""".trimMargin(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = httpClientGeneric(mockEngine)
        val pesysClient = PesysClient(client) { "a token to be used for tests" }
        runTest {
            val response = pesysClient.hentUføre("ident", LocalDate.EPOCH, LocalDate.of(9999, 12, 31), "behovId")
            assertTrue(response.harUforegrad)
            assertEquals(LocalDate.of(2022, Month.FEBRUARY, 1), response.datoUfor)
            assertEquals(LocalDate.of(2022, Month.SEPTEMBER, 1), response.virkDato)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Pesys svarer 404 Not Found og personen får ikke uføregrad`() {
        val mockEngine = MockEngine {
            respond(
                content = "Personen fantes ikke i Pesys og vi setter uføregrad=false",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = httpClientGeneric(mockEngine)
        val pesysClient = PesysClient(client) { "a token to be used for tests" }
        runTest {
            val response = pesysClient.hentUføre("ident", LocalDate.EPOCH, LocalDate.of(9999, 12, 31), "behovId")
            assertFalse(response.harUforegrad)
            assertNull(response.datoUfor)
            assertNull(response.virkDato)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Pesys svarer 400 Bad Request og det kastes exception`() {
        val mockEngine = MockEngine {
            respond(
                content = "Fikk 400 fra Pesys",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = httpClientGeneric(mockEngine)
        val pesysClient = PesysClient(client) { "a token to be used for tests" }
        assertThrows(ClientRequestException::class.java) {
            runTest { pesysClient.hentUføre("ident", LocalDate.EPOCH, LocalDate.of(9999, 12, 31), "behovId") }
        }
    }
}
