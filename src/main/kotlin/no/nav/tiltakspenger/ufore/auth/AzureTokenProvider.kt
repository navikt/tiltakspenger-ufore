package no.nav.tiltakspenger.ufore.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.Parameters
import kotlinx.coroutines.runBlocking
import no.nav.tiltakspenger.ufore.Configuration

class AzureTokenProvider(private val httpClient: HttpClient) {
    private val config = Configuration.OauthConfig()
    private val wellknown: WellKnown by lazy { runBlocking { httpClient.get(config.wellknownUrl).body() } }
    private val tokenCache = TokenCache()

    suspend fun getToken(): String {
        val currentToken = tokenCache.token
        if (currentToken != null && !tokenCache.isExpired()) return currentToken
        val response: OAuth2AccessTokenResponse = httpClient.submitForm(
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
