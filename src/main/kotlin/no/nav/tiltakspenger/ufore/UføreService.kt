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
import no.nav.tiltakspenger.libs.ufore.Feilmelding
import no.nav.tiltakspenger.libs.ufore.UforeResponsDTO
import no.nav.tiltakspenger.ufore.pesys.PesysClient
import java.time.LocalDate

class UføreService(rapidsConnection: RapidsConnection, private val pesysClient: PesysClient) : River.PacketListener {
    private val log = KotlinLogging.logger {}
    private val secureLog = KotlinLogging.logger("tjenestekall")

    companion object {
        internal object BEHOV {
            const val UFØRE_YTELSER = "uføre"
        }
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAllOrAny("@behov", listOf(BEHOV.UFØRE_YTELSER))
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
                "behovId" to packet["@behovId"].asText(),
            ) {
                val behovId = packet["@behovId"].asText()
                val ident = packet["ident"].asText()
                secureLog.debug { "mottok ident $ident" }
                val fom: String = packet["fom"].asText("1970-01-01")
                val tom: String = packet["tom"].asText("9999-12-31")

                val fomFixed = try {
                    val tempFom: LocalDate = LocalDate.parse(fom)
                    if (tempFom == LocalDate.MIN) {
                        LocalDate.EPOCH
                    } else {
                        tempFom
                    }
                } catch (e: Exception) {
                    LOG.warn("Klarte ikke å parse fom $fom", e)
                    LocalDate.EPOCH
                }

                val tomFixed = try {
                    val tempTom: LocalDate = LocalDate.parse(tom)
                    if (tempTom == LocalDate.MAX) {
                        LocalDate.of(9999, 12, 31)
                    } else {
                        tempTom
                    }
                } catch (e: Exception) {
                    LOG.warn("Klarte ikke å parse tom $tom", e)
                    LocalDate.of(9999, 12, 31)
                }

                val respons = if (ident.equals("22900497588") or ident.equals("24910596609")) {
                    // disse er feil identer i dev som vi må skippe
                    secureLog.info { "Vi hopper over $ident fordi at vi har den på listen over de som ikke er gyldige" }
                    UforeResponsDTO(
                        uføregrad = null,
                        feil = Feilmelding.UgyldigIdent,
                    )
                } else {
                    val uføregrad = runBlocking(MDCContext()) { pesysClient.hentUføre(ident, fomFixed, tomFixed, behovId) }
                    log.info { "Fikk svar fra Pesys. Sjekk securelog for detaljer" }
                    secureLog.info { uføregrad }
                    UforeResponsDTO(
                        uføregrad = uføregrad,
                        feil = null,
                    )
                }

                packet["@løsning"] = mapOf(
                    BEHOV.UFØRE_YTELSER to respons,
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
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        secureLog.info(
            "løser uføre-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        secureLog.debug { "mottok melding: ${packet.toJson()}" }
    }

    private fun loggVedUtgang(packet: JsonMessage) {
        log.info(
            "har løst uføre-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        secureLog.info(
            "har løst uføre-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
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
            ex,
        )
    }
}
