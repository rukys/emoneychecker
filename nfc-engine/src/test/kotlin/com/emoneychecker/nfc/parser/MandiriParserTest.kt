package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.nfc.test.FakeIsoDepWrapper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MandiriParserTest {

    @Test
    fun `MandiriParser returns correct balance from primary AID`() = runTest {
        val fakeIsoDep = FakeIsoDepWrapper(
            responses = mapOf(
                "00 A4 04 00 08 A0 00 00 00 03 86 98 07 01" to byteArrayOf(0x90.toByte(), 0x00),
                "00 B0 00 00 10" to byteArrayOf(
                    0x00, 0x01, 0xEB.toByte(), 0xB8.toByte(), // 125880 sen = Rp 1258
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x90.toByte(), 0x00
                )
            )
        )

        val parser = MandiriParser()
        val result = parser.parse(fakeIsoDep)

        assertEquals(CardBank.MANDIRI, result.bank)
        assertEquals(1258L, result.balanceRupiah)
        assertTrue(result.maskedPan.contains("••••"))
    }

    @Test
    fun `MandiriParser falls back to secondary AID when primary fails`() = runTest {
        val fakeIsoDep = FakeIsoDepWrapper(
            responses = mapOf(
                "00 A4 04 00 08 A0 00 00 00 03 86 98 07 01" to byteArrayOf(0x6A.toByte(), 0x82.toByte()),
                "00 A4 04 00 07 A0 00 00 00 03 00 00" to byteArrayOf(0x90.toByte(), 0x00),
                "00 B0 00 00 10" to byteArrayOf(
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x01, 0x86.toByte(), 0xA0.toByte(), // 100,000 sen = Rp 1000
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x90.toByte(), 0x00
                )
            )
        )

        val parser = MandiriParser()
        val result = parser.parse(fakeIsoDep)

        assertEquals(CardBank.MANDIRI, result.bank)
        assertEquals(1000L, result.balanceRupiah)
    }
}
