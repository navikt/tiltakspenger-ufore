package no.nav.tiltakspenger.ufore.pesys

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
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

class PesysClient(private val client: HttpClient, private val getToken: suspend () -> String) {
    private val config = Configuration.PesysConfig()

    companion object {
        val objectmapper: ObjectMapper = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .setDefaultPrettyPrinter(
                DefaultPrettyPrinter().apply {
                    indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                    indentObjectsWith(DefaultIndenter("  ", "\n"))
                },
            )
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    suspend fun hentUføre(ident: String, fom: LocalDate, tom: LocalDate, behovId: String): UføregradDTO = try {
        client.get(urlString = config.pesysUføreUrl) {
            bearerAuth(getToken())
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", behovId)
            header("fnr", ident)
            url {
                parameters.append("fom", objectmapper.writeValueAsString(fom))
                parameters.append("tom", objectmapper.writeValueAsString(tom))
                parameters.appendAll("uforeTyper", listOf("UFORE", "UF_M_YRKE"))
            }
        }.body()
    } catch (e: ClientRequestException) {
        if (e.response.status == HttpStatusCode.NotFound) {
            UføregradDTO(false, null, null)
        } else throw (e)
    }
}
