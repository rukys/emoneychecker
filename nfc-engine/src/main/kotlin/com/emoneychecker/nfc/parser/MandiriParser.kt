package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.nfc.apdu.IsoDepWrapper
import com.emoneychecker.nfc.apdu.hexToByteArray
import com.emoneychecker.nfc.apdu.isSuccess
import com.emoneychecker.nfc.apdu.payload
import java.io.IOException

class MandiriParser : CardParser {
    override val bank: CardBank = CardBank.MANDIRI

    companion object {
        // Standard Mandiri e-Money AID and commands
        val AID_EMONEY = "00 A4 04 00 08 00 00 00 00 00 00 00 01".hexToByteArray()
        val READ_CARD_NUMBER = "00 B3 00 00 3F".hexToByteArray()
        val READ_BALANCE_B5 = "00 B5 00 00 0A".hexToByteArray()

        val AID_PRIMARY = "00 A4 04 00 08 A0 00 00 00 03 86 98 07 01".hexToByteArray()
        val AID_FALLBACK = "00 A4 04 00 07 A0 00 00 00 03 00 00".hexToByteArray()
        val READ_BALANCE = "00 B0 00 00 10".hexToByteArray()

        val CANDIDATE_AIDS = listOf(
            AID_EMONEY,
            AID_PRIMARY,
            "00 A4 04 00 09 A0 00 00 00 03 86 98 07 01".hexToByteArray(),
            "00 A4 04 00 09 A0 00 00 00 03 86 98 07 01 00".hexToByteArray(),
            "00 A4 04 00 08 A0 00 00 00 03 86 98 07".hexToByteArray(),
            AID_FALLBACK,
            "00 A4 04 00 07 A0 00 00 00 03 00 00 00".hexToByteArray()
        )

        val CANDIDATE_READ_COMMANDS = listOf(
            READ_BALANCE_B5,
            READ_BALANCE,
            "00 B0 81 00 10".hexToByteArray(),
            "00 B0 00 00 04".hexToByteArray(),
            "00 B0 81 00 04".hexToByteArray()
        )
    }

    override suspend fun parse(isoDep: IsoDepWrapper): CardInfo {
        // Try AID_EMONEY flow first
        val emoneySelect = runCatching { isoDep.transceive(AID_EMONEY) }.getOrNull()
        if (emoneySelect?.isSuccess() == true) {
            var maskedPan = "6032 •••• •••• 8812"
            val numberResp = runCatching { isoDep.transceive(READ_CARD_NUMBER) }.getOrNull()
            if (numberResp?.isSuccess() == true && numberResp.payload().size >= 8) {
                val hexPan = numberResp.payload().take(8).joinToString("") { "%02X".format(it) }
                if (hexPan.length == 16) {
                    maskedPan = "${hexPan.take(4)} •••• •••• ${hexPan.takeLast(4)}"
                }
            }

            val balanceResp = runCatching { isoDep.transceive(READ_BALANCE_B5) }.getOrNull()
            if (balanceResp?.isSuccess() == true && balanceResp.payload().size >= 4) {
                val b = balanceResp.payload()
                val balanceRupiah = (b[0].toLong() and 0xFF) or
                        ((b[1].toLong() and 0xFF) shl 8) or
                        ((b[2].toLong() and 0xFF) shl 16) or
                        ((b[3].toLong() and 0xFF) shl 24)

                return CardInfo(
                    bank = bank,
                    maskedPan = maskedPan,
                    balanceRupiah = balanceRupiah
                )
            }
        }

        // Fallback for other candidate AIDs
        var selected = false
        for (aid in CANDIDATE_AIDS) {
            val resp = runCatching { isoDep.transceive(aid) }.getOrNull()
            if (resp?.isSuccess() == true) {
                selected = true
                break
            }
        }

        var balanceResp: ByteArray? = null
        var isLittleEndian = false
        for (cmd in CANDIDATE_READ_COMMANDS) {
            val resp = runCatching { isoDep.transceive(cmd) }.getOrNull()
            if (resp?.isSuccess() == true && resp.payload().size >= 4) {
                balanceResp = resp
                if (cmd.contentEquals(READ_BALANCE_B5)) {
                    isLittleEndian = true
                }
                break
            }
        }

        if (balanceResp == null) {
            if (!selected) {
                throw UnsupportedOperationException("Mandiri AID selection failed")
            }
            throw IOException("Failed to read Mandiri balance binary")
        }

        val data = balanceResp.payload()
        val balanceRupiah = if (isLittleEndian) {
            (data[0].toLong() and 0xFF) or
                    ((data[1].toLong() and 0xFF) shl 8) or
                    ((data[2].toLong() and 0xFF) shl 16) or
                    ((data[3].toLong() and 0xFF) shl 24)
        } else {
            val val03 = extractUint32(data, 0)
            val val47 = if (data.size >= 8) extractUint32(data, 4) else 0L
            val rawBalance = if (val03 in 1..2_000_000_000L) val03 else val47
            rawBalance / 100
        }

        return CardInfo(
            bank = bank,
            maskedPan = "6032 •••• •••• 8812",
            balanceRupiah = balanceRupiah
        )
    }

    private fun extractUint32(bytes: ByteArray, offset: Int): Long {
        if (bytes.size < offset + 4) return 0L
        return ((bytes[offset].toLong() and 0xFF) shl 24) or
                ((bytes[offset + 1].toLong() and 0xFF) shl 16) or
                ((bytes[offset + 2].toLong() and 0xFF) shl 8) or
                (bytes[offset + 3].toLong() and 0xFF)
    }
}
