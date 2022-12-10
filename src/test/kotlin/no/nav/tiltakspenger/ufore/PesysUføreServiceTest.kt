package no.nav.tiltakspenger.ufore

import io.mockk.coEvery
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class PesysUføreServiceTest {
    private val testRapid: TestRapid = TestRapid()
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

    @AfterEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun happy() {
        val pesysClient = mockk<PesysClient>()
        val datoUfor = LocalDate.EPOCH
        val virkDato = LocalDate.MAX
        coEvery { pesysClient.hentUføre(ident, any(), any(), any()) }.returns(UføreResponse(true, datoUfor, virkDato))
        PesysUføreService(testRapid, pesysClient)
        testRapid.sendTestMessage(behov)
        with(testRapid.inspektør) {
            assertEquals(1, size)
        }
    }
}
