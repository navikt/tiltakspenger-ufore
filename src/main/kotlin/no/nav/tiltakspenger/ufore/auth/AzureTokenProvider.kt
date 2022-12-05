package no.nav.tiltakspenger.ufore.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.tiltakspenger.ufore.Configuration
import no.nav.tiltakspenger.ufore.HttpClient

class AzureTokenProvider(private val config: Configuration.OauthConfig) {
    private val log = KotlinLogging.logger {}
    private val wellknown: WellKnown by lazy { runBlocking { client.get(config.wellknownUrl).body() } }
    private val tokenCache = TokenCache()
    private val client = HttpClient {
        System.getenv("HTTP_PROXY")?.let {
            log.info("Setter opp proxy mot $it")
            this.proxy = ProxyBuilder.http(it)
        }
    }.client

    suspend fun getToken(): String {
        val currentToken = tokenCache.token
        if (currentToken != null && !tokenCache.isExpired()) return currentToken
        val response: OAuth2AccessTokenResponse = client.submitForm(
            url = wellknown.tokenEndpoint,
            formParameters = Parameters.build {
                append("grant_type", "client_credentials")
                append("client_id", config.clientId)
                append("client_secret", config.clientSecret)
                append("scope", config.scope)
            }
        ).body()
        tokenCache.update(response.accessToken, response.expiresIn.toLong())
        return response.accessToken
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class WellKnown(
        @JsonProperty("token_endpoint")
        val tokenEndpoint: String
    )
}
