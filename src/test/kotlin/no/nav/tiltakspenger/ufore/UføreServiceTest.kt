package no.nav.tiltakspenger.ufore

import io.mockk.coEvery
import io.mockk.mockk
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.ufore.pesys.PesysClient
import no.nav.tiltakspenger.ufore.pesys.UføreResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class UføreServiceTest {
    private val ident = "42"
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
        coEvery { pesysClient.hentUføre(ident, any(), any(), any()) }.returns(UføreResponse(true, datoUfor, virkDato))
        UføreService(testRapid, pesysClient)
        testRapid.sendTestMessage(behov)
        with(testRapid.inspektør) {
            val løsning = this.message(0)["@løsning"]
            assertEquals(1, size)
            assertTrue(løsning["harUforegrad"].asBoolean())
            assertEquals(virkDato, løsning["virkDato"].asLocalDate())
        }
    }
}
