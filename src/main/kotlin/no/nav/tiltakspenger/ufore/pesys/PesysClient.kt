package no.nav.tiltakspenger.ufore.pesys

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.tiltakspenger.ufore.Configuration
import java.time.LocalDate

class PesysClient(private val client: HttpClient, private val getToken: suspend () -> String) {
    private val config = Configuration.PesysConfig()

    // Her kan det sikkert være fristende å gjøre om fom og tom til LocalDate, men jeg tenker at det ikke er nødvendig
    // De datoene kommer inn som String via R&R-meldingen, og gjøres igjen om til String før de sendes videre til Pesys
    // Validering av format foretas derfor flere steder allerede
    suspend fun hentUføre(ident: String, fom: String, tom: String, behovId: String): UføreResponse = try {
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
            UføreResponse(false, null, null)
        } else throw (e)
    }
}

data class UføreResponse(val harUforegrad: Boolean, val datoUfor: LocalDate?, val virkDato: LocalDate?)
