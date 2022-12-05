package no.nav.tiltakspenger.ufore

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asOptionalLocalDate
import java.time.LocalDate

class PesysUføreService(rapidsConnection: RapidsConnection, private val client: PesysClient) : River.PacketListener {
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
        log.info { "Mottok ${packet["@behov"]}" }
        val behovId = packet["@behovId"].asText()
        val ident = packet["ident"].asText()
        val fom: LocalDate = packet["fom"].asOptionalLocalDate() ?: LocalDate.MIN
        val tom: LocalDate = packet["tom"].asOptionalLocalDate() ?: LocalDate.MAX
        val response = runBlocking(MDCContext()) { client.hentUføre(ident, fom, tom, behovId) }
        secureLog.info { response }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.info { "onError: $problems" }
    }

    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        log.info { "onSevere: $error" }
    }
}
