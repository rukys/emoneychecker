package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.nfc.test.FakeIsoDepWrapper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TapCashParserTest {

    @Test
    fun `TapCashParser divides raw cents by 100`() = runTest {
        val fakeIsoDep = FakeIsoDepWrapper(
            responses = mapOf(
                "00 A4 04 00 08 A0 00 00 00 18 43 41 54" to byteArrayOf(0x90.toByte(), 0x00),
                "00 B0 00 00 04" to byteArrayOf(
                    0x00, 0x4C, 0x4B.toByte(), 0x40.toByte(), // 5,000,000 sen = Rp 50,000
                    0x90.toByte(), 0x00
                )
            )
        )

        val parser = TapCashParser()
        val result = parser.parse(fakeIsoDep)

        assertEquals(CardBank.BNI_TAPCASH, result.bank)
        assertEquals(50000L, result.balanceRupiah)
    }
}
