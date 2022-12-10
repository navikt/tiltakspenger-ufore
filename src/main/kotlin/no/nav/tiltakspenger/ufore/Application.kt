package no.nav.tiltakspenger.ufore

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.ufore.auth.AzureTokenProvider

fun main() {
    System.setProperty("logback.configurationFile", "egenLogback.xml")
    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
    }
    val tokenProvider = AzureTokenProvider(httpClient = httpClient(useProxy = true))
    RapidApplication.create(Configuration.rapidsAndRivers).apply {
        PesysUføreService(rapidsConnection = this, pesysClient = PesysClient(httpClient(), tokenProvider::getToken))
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                log.info { "Starting tiltakspenger-ufore" }
            }

            override fun onShutdown(rapidsConnection: RapidsConnection) {
                log.info { "Stopping tiltakspenger-ufore" }
                super.onShutdown(rapidsConnection)
            }
        })
    }.start()
}
