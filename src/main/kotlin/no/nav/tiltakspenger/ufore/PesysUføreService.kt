package no.nav.tiltakspenger.ufore

import io.ktor.client.plugins.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class PesysUføreService(
    rapidsConnection: RapidsConnection,
    private val pesysClient: PesysClient
) : River.PacketListener {
    private val log = KotlinLogging.logger {}
    private val secureLog = KotlinLogging.logger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAllOrAny("@behov", listOf("uføre"))
                it.forbid("@løsning")
                it.requireKey("@id", "@behovId")
                it.requireKey("ident")
                it.interestedIn("fom")
                it.interestedIn("tom")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        log.info { "Mottok $packet" }
        try {
            val behovId = packet["@behovId"].asText()
            val ident = packet["ident"].asText()
            val fom = packet["fom"].asText()
            val tom = packet["tom"].asText()
            val response: UføreResponse = runBlocking(MDCContext()) { pesysClient.hentUføre(ident, fom, tom, behovId) }
            log.info { "Fikk svar fra Pesys. Sjekk securelog for detaljer" }
            secureLog.info { response }
        } catch (e: ClientRequestException) {
            log.info { "Svelger og går videre: $e" }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.info { "onError: $problems" }
    }
}
