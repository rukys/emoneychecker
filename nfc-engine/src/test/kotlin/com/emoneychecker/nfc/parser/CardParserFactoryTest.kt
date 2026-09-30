package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.nfc.test.FakeIsoDepWrapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CardParserFactoryTest {

    @Test
    fun `detect returns MandiriParser when Mandiri AID responds 90 00`() {
        val fakeIsoDep = FakeIsoDepWrapper(
            responses = mapOf(
                "00 A4 04 00 08 A0 00 00 00 03 86 98 07 01" to byteArrayOf(0x90.toByte(), 0x00)
            )
        )
        val parser = CardParserFactory.detect(fakeIsoDep)
        assertEquals(CardBank.MANDIRI, parser.bank)
    }

    @Test
    fun `detect returns FlazzParser when Flazz AID responds 90 00`() {
        val fakeIsoDep = FakeIsoDepWrapper(
            responses = mapOf(
                "00 A4 04 00 08 A0 00 00 00 03 86 98 07 01" to byteArrayOf(0x6A.toByte(), 0x82.toByte()),
                "00 A4 04 00 07 A0 00 00 06 00 01 01" to byteArrayOf(0x90.toByte(), 0x00)
            )
        )
        val parser = CardParserFactory.detect(fakeIsoDep)
        assertEquals(CardBank.BCA_FLAZZ, parser.bank)
    }

    @Test
    fun `detect throws UnsupportedOperationException when no AID matches`() {
        val fakeIsoDep = FakeIsoDepWrapper(
            responses = mapOf(
                "00 A4 04 00 08 A0 00 00 00 03 86 98 07 01" to byteArrayOf(0x6A.toByte(), 0x82.toByte()),
                "00 A4 04 00 07 A0 00 00 06 00 01 01" to byteArrayOf(0x6A.toByte(), 0x82.toByte()),
                "00 A4 04 00 08 A0 00 00 00 18 43 41 54" to byteArrayOf(0x6A.toByte(), 0x82.toByte()),
                "00 A4 04 00 07 D2 76 00 00 85 01 01" to byteArrayOf(0x6A.toByte(), 0x82.toByte()),
                "00 A4 04 00 07 A0 00 00 00 03 00 00" to byteArrayOf(0x6A.toByte(), 0x82.toByte())
            )
        )
        assertThrows(UnsupportedOperationException::class.java) {
            CardParserFactory.detect(fakeIsoDep)
        }
    }

    @Test
    fun `detect returns FlazzParser when Flazz AID with Le responds 90 00`() {
        val fakeIsoDep = FakeIsoDepWrapper(
            responses = mapOf(
                "00 A4 04 00 08 A0 00 00 00 03 86 98 07 01" to byteArrayOf(0x6A.toByte(), 0x82.toByte()),
                "00 A4 04 00 07 A0 00 00 06 00 01 01" to byteArrayOf(0x6A.toByte(), 0x82.toByte()),
                "00 A4 04 00 07 A0 00 00 06 00 01 01 00" to byteArrayOf(0x90.toByte(), 0x00)
            )
        )
        val parser = CardParserFactory.detect(fakeIsoDep)
        assertEquals(CardBank.BCA_FLAZZ, parser.bank)
    }

    @Test
    fun `detect returns TapCashParser when TapCash Gen 2 AID responds 90 00`() {
        val fakeIsoDep = FakeIsoDepWrapper(
            responses = mapOf(
                "00 A4 04 00 08 A0 00 42 4E 49 99 99 99" to byteArrayOf(0x90.toByte(), 0x00)
            )
        )
        val parser = CardParserFactory.detect(fakeIsoDep)
        assertEquals(CardBank.BNI_TAPCASH, parser.bank)
    }

    @Test
    fun `detect rethrows TagLostException immediately when tag is disconnected`() {
        val disconnectedIsoDep = FakeIsoDepWrapper(responses = emptyMap()).apply {
            connect()
            close()
        }
        assertThrows(android.nfc.TagLostException::class.java) {
            CardParserFactory.detect(disconnectedIsoDep)
        }
    }
}
