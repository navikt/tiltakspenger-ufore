package no.nav.tiltakspenger.ufore

import io.mockk.coEvery
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.libs.ufore.UføregradDTO
import no.nav.tiltakspenger.ufore.pesys.PesysClient
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate

internal class UføreServiceTest {
    private val ident = "42"

    @Language("JSON")
    private val behov = """
            {
              "@behov": [
                "uføre"
              ],
              "@id": "test",
              "@behovId": "behovId",
              "ident": "$ident",
              "fom": "2022-01-01",
              "tom": "2022-12-31",
              "testmelding": true,
              "@opprettet": "2022-12-10T15:39:15.723871242",
              "system_read_count": 0,
              "system_participating_services": [
                {
                  "id": "test",
                  "time": "2022-12-10T15:39:15.723871242",
                  "service": "tiltakspenger-ufore",
                  "instance": "tiltakspenger-ufore-5786f7846d-mdtdj",
                  "image": "ghcr.io/navikt/tiltakspenger-ufore:f1b04a34f22e29e2ed160391e5de765ba18549ac"
                }
              ]
            }
        """

    @Test
    fun happy() {
        val testRapid = TestRapid()
        val pesysClient = mockk<PesysClient>()
        val datoUfor = LocalDate.MIN
        val virkDato = LocalDate.EPOCH

        coEvery { pesysClient.hentUføre(ident, any(), any(), any()) }.returns(UføregradDTO(true, datoUfor, virkDato))
        UføreService(testRapid, pesysClient)
        testRapid.sendTestMessage(behov)
        with(testRapid.inspektør) {
            assertEquals(1, size)

            JSONAssert.assertEquals(
                svar,
                message(0).toPrettyString(), JSONCompareMode.LENIENT,
            )
        }
    }
    private val svar = """
            {
              "@løsning": {
                "uføre": {
                  "uføregrad":
                    {
                      "harUforegrad": true,
                      "datoUfor": "-999999999-01-01",
                      "virkDato": "1970-01-01"
                    },
                  "feil": null
                }
              }
            }
    """.trimIndent()
}
