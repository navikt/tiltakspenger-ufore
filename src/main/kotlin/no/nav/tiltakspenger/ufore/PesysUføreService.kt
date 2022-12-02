package no.nav.tiltakspenger.ufore

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class PesysUføreService(rapidsConnection: RapidsConnection,): River.PacketListener {
    private val log = KotlinLogging.logger {}
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
        TODO("Not yet implemented")
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.info { "Problems: $problems" }
    }
}
