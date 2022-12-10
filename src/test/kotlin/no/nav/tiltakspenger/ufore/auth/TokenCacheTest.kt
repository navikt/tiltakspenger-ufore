package no.nav.tiltakspenger.ufore.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class TokenCacheTest {

    @Test
    fun `Token er utløpt om tiden har løpt fra det`() {
        val tokenCache = TokenCache()
        tokenCache.update(
            accessToken = "token",
            expiresIn = -1
        )
        assertTrue(tokenCache.isExpired())
    }

    @Test
    fun `Token er ikke utløpt om utløpsdatoen er langt fram i tid`() {
        val tokenCache = TokenCache()
        tokenCache.update(
            accessToken = "token",
            expiresIn = 100L
        )
        assertFalse(tokenCache.isExpired())
    }

    @Test
    fun `Mulig å hente token fra cache`() {
        val tokenCache = TokenCache()
        tokenCache.update(
            accessToken = "token",
            expiresIn = 100L
        )
        assertEquals("token", tokenCache.token)
    }
}
