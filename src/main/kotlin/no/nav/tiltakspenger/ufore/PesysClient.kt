package no.nav.tiltakspenger.ufore

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.time.LocalDate

class PesysClient(private val config: Configuration.PesysConfig, private val getToken: suspend () -> String) {
    private val client = HttpClient().client
    suspend fun hentUføre(ident: String, fom: LocalDate, tom: LocalDate, behovId: String): UføreResponse {
        val token = getToken()
        val response: UføreResponse = client.get(urlString = config.pesysUføreUrl) {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", behovId)
            header("fnr", ident)
            url {
                parameters.append("fom", fom.toString())
                parameters.append("tom", tom.toString())
                parameters.append("uforeTyper", "UFORE")
                parameters.append("uforeTyper", "UF_M_YRKE")
            }
        }.body()
        return response
    }
}

data class UføreResponse(val harUforegrad: Boolean, val datoUfor: LocalDate?, val virkDato: LocalDate?)
