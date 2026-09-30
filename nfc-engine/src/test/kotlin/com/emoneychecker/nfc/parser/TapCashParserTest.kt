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

    @Test
    fun `TapCashParser reads balance and masked PAN using native command on Gen 2`() = runTest {
        val fakeIsoDep = FakeIsoDepWrapper(
            responses = mapOf(
                "00 A4 04 00 08 A0 00 42 4E 49 99 99 99" to byteArrayOf(0x90.toByte(), 0x00),
                "90 32 03 00 00" to byteArrayOf(
                    0x00, 0x00, 0x00, 0xC3.toByte(), 0x50, // Bytes 2..4 = 50,000 IDR
                    0x00, 0x00, 0x00,
                    0x75, 0x46, 0x02, 0x00, 0x02, 0x66, 0x22, 0x56, // Bytes 8..15 = PAN 7546020002662256
                    0x90.toByte(), 0x00
                )
            )
        )

        val parser = TapCashParser()
        val result = parser.parse(fakeIsoDep)

        assertEquals(CardBank.BNI_TAPCASH, result.bank)
        assertEquals(50000L, result.balanceRupiah)
        assertEquals("7546 •••• •••• 2256", result.maskedPan)
    }
}
