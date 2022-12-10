package no.nav.tiltakspenger.ufore

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import mu.KotlinLogging
import mu.withLoggingContext
import net.logstash.logback.argument.StructuredArguments
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
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
                it.requireKey("fom")
                it.requireKey("tom")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        runCatching {
            loggVedInngang(packet)
            withLoggingContext(
                "id" to packet["@id"].asText(),
                "behovId" to packet["@behovId"].asText()
            ) {
                val behovId = packet["@behovId"].asText()
                val ident = packet["ident"].asText()
                secureLog.debug { "mottok ident $ident" }
                val fom = packet["fom"].asText()
                val tom = packet["tom"].asText()
                val response: UføreResponse = runBlocking(MDCContext()) {
                    pesysClient.hentUføre(ident, fom, tom, behovId)
                }
                log.info { "Fikk svar fra Pesys. Sjekk securelog for detaljer" }
                secureLog.info { response }
                packet["@løsning"] = mapOf(
                    "harUforegrad" to response.harUforegrad,
                    "virkDato" to response.virkDato
                )
                loggVedUtgang(packet)
                context.publish(ident, packet.toJson())
            }
        }.onFailure {
            loggVedFeil(it, packet)
        }.getOrThrow()
    }

    private fun loggVedInngang(packet: JsonMessage) {
        log.info(
            "løser uføre-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText())
        )
        secureLog.info(
            "løser uføre-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText())
        )
        secureLog.debug { "mottok melding: ${packet.toJson()}" }
    }

    private fun loggVedUtgang(packet: JsonMessage) {
        log.info(
            "har løst uføre-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText())
        )
        secureLog.info(
            "har løst uføre-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText())
        )
        secureLog.debug { "publiserer melding: ${packet.toJson()}" }
    }

    private fun loggVedFeil(ex: Throwable, packet: JsonMessage) {
        log.error(
            "feil ved behandling av uføre-behov med {}, se securelogs for detaljer",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
        )
        secureLog.error(
            "feil \"${ex.message}\" ved behandling av uføre-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("packet", packet.toJson()),
            ex
        )
    }
}
