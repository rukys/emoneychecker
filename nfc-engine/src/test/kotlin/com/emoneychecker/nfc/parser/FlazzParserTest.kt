package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.nfc.test.FakeIsoDepWrapper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FlazzParserTest {

    @Test
    fun `FlazzParser returns balance in rupiah directly`() = runTest {
        val fakeIsoDep = FakeIsoDepWrapper(
            responses = mapOf(
                "00 A4 04 00 07 A0 00 00 06 00 01 01" to byteArrayOf(0x90.toByte(), 0x00),
                "80 5C 00 02 04" to byteArrayOf(
                    0x00, 0x01, 0xE2.toByte(), 0x40.toByte(), // 123456
                    0x90.toByte(), 0x00
                )
            )
        )

        val parser = FlazzParser()
        val result = parser.parse(fakeIsoDep)

        assertEquals(CardBank.BCA_FLAZZ, result.bank)
        assertEquals(123456L, result.balanceRupiah)
    }
}
