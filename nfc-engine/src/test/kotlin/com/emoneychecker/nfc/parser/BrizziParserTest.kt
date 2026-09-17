package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.nfc.test.FakeIsoDepWrapper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BrizziParserTest {

    @Test
    fun `BrizziParser extracts balance from record offset 2`() = runTest {
        val recordPayload = ByteArray(29) { 0 }
        // Offset 2..5: 250,000 sen = Rp 2,500 -> 0x00, 0x03, 0xD0, 0x90
        recordPayload[2] = 0x00
        recordPayload[3] = 0x03
        recordPayload[4] = 0xD0.toByte()
        recordPayload[5] = 0x90.toByte()

        val fullResponse = recordPayload + byteArrayOf(0x90.toByte(), 0x00)

        val fakeIsoDep = FakeIsoDepWrapper(
            responses = mapOf(
                "00 A4 04 00 07 D2 76 00 00 85 01 01" to byteArrayOf(0x90.toByte(), 0x00),
                "00 B2 01 0C 1D" to fullResponse
            )
        )

        val parser = BrizziParser()
        val result = parser.parse(fakeIsoDep)

        assertEquals(CardBank.BRI_BRIZZI, result.bank)
        assertEquals(2500L, result.balanceRupiah)
    }
}
