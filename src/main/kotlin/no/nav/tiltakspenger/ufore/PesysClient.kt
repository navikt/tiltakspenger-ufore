package no.nav.tiltakspenger.ufore

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.tiltakspenger.ufore.HttpClient.client
import java.time.LocalDate

class PesysClient(private val config: Configuration.PesysConfig, private val getToken: suspend () -> String) {
    suspend fun hentUføre(ident: String, fom: LocalDate, tom: LocalDate, behovId: String): String {
        val token = getToken()
        val response = client.get(urlString = config.pesysUføreUrl) {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", behovId)
            header("fnr", ident)
            url {
                parameters.append("fom", fom.toString())
                parameters.append("tom", tom.toString())
                parameters.append("uforeTyper", "UFORE")
            }
        }
        return response.body()
    }
}
