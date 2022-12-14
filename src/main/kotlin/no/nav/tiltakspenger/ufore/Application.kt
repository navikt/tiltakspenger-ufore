package no.nav.tiltakspenger.ufore

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.ufore.auth.AzureTokenProvider
import no.nav.tiltakspenger.ufore.pesys.PesysClient

fun main() {
    System.setProperty("logback.configurationFile", "egenLogback.xml")
    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
    }
    val tokenProvider = AzureTokenProvider(httpClientMedProxy())
    RapidApplication.create(Configuration.rapidsAndRivers).apply {
        UføreService(rapidsConnection = this, pesysClient = PesysClient(httpClientCIO(), tokenProvider::getToken))
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
