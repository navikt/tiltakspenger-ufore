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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PesysClient(private val client: HttpClient, private val getToken: suspend () -> String) {
    private val config = Configuration.PesysConfig()

    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }

    suspend fun hentUføre(ident: String, fom: LocalDate, tom: LocalDate, behovId: String): UføregradDTO = try {
        client.get(urlString = config.pesysUføreUrl) {
            bearerAuth(getToken())
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", behovId)
            header("fnr", ident)
            url {
                parameters.append("fom", DateTimeFormatter.ISO_LOCAL_DATE.format(fom))
                parameters.append("tom", DateTimeFormatter.ISO_LOCAL_DATE.format(tom))
                parameters.appendAll("uforeTyper", listOf("UFORE", "UF_M_YRKE"))
            }
        }.body()
    } catch (e: ClientRequestException) {
        if (e.response.status == HttpStatusCode.NotFound) {
            UføregradDTO(false, null, null)
        } else throw (e)
    }
}
