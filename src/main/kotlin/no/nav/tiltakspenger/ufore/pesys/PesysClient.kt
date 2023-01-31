package no.nav.tiltakspenger.ufore.pesys

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.tiltakspenger.libs.ufore.UføregradDTO
import no.nav.tiltakspenger.ufore.Configuration

class PesysClient(private val client: HttpClient, private val getToken: suspend () -> String) {
    private val config = Configuration.PesysConfig()

    // Her kan det sikkert være fristende å gjøre om fom og tom til LocalDate, men jeg tenker at det ikke er nødvendig
    // De datoene kommer inn som String via R&R-meldingen, og gjøres igjen om til String før de sendes videre til Pesys
    // Validering av format foretas derfor flere steder allerede
    suspend fun hentUføre(ident: String, fom: String, tom: String, behovId: String): UføregradDTO = try {
        client.get(urlString = config.pesysUføreUrl) {
            bearerAuth(getToken())
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", behovId)
            header("fnr", ident)
            url {
                parameters.append("fom", fom)
                parameters.append("tom", tom)
                parameters.appendAll("uforeTyper", listOf("UFORE", "UF_M_YRKE"))
            }
        }.body()
    } catch (e: ClientRequestException) {
        if (e.response.status == HttpStatusCode.NotFound) {
            UføregradDTO(false, null, null)
        } else throw (e)
    }
}
