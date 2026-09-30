package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.nfc.apdu.IsoDepWrapper
import com.emoneychecker.nfc.apdu.hexToByteArray
import com.emoneychecker.nfc.apdu.isSuccess
import com.emoneychecker.nfc.apdu.payload
import com.emoneychecker.nfc.apdu.transceiveOrNull

class TapCashParser : CardParser {
    override val bank: CardBank = CardBank.BNI_TAPCASH

    companion object {
        // BNI TapCash Gen 2 (Modern cards)
        val AID_TAPCASH_GEN2 = "00 A4 04 00 08 A0 00 42 4E 49 99 99 99".hexToByteArray()
        // BNI TapCash Gen 1
        val AID_TAPCASH_GEN1 = "00 A4 04 00 08 A0 00 42 4E 49 10 00 01".hexToByteArray()
        // TapCash Legacy / Specification AID
        val AID_TAPCASH_LEGACY = "00 A4 04 00 08 A0 00 00 00 18 43 41 54".hexToByteArray()

        val CANDIDATE_AIDS = listOf(
            AID_TAPCASH_GEN2,
            AID_TAPCASH_GEN1,
            AID_TAPCASH_LEGACY
        )

        // Legacy compatibility reference
        val AID_TAPCASH = AID_TAPCASH_LEGACY

        // Native TapCash command to read balance & card info
        val READ_BALANCE_NATIVE = "90 32 03 00 00".hexToByteArray()
        // Standard Read Binary balance APDU
        val READ_BALANCE = "00 B0 00 00 04".hexToByteArray()
    }

    override suspend fun parse(isoDep: IsoDepWrapper): CardInfo {
        var selected = false
        for (aid in CANDIDATE_AIDS) {
            val selectResp = isoDep.transceiveOrNull(aid)
            if (selectResp?.isSuccess() == true) {
                selected = true
                break
            }
        }

        if (!selected) {
            throw UnsupportedOperationException("BNI TapCash AID selection failed")
        }

        // 1. Try Native TapCash command (90 32 03 00 00)
        val nativeResp = isoDep.transceiveOrNull(READ_BALANCE_NATIVE)
        if (nativeResp?.isSuccess() == true && nativeResp.payload().size >= 5) {
            val payload = nativeResp.payload()
            val balance = ((payload[2].toLong() and 0xFF) shl 16) or
                    ((payload[3].toLong() and 0xFF) shl 8) or
                    (payload[4].toLong() and 0xFF)

            var maskedPan = "7546 •••• •••• 9923"
            var fullPan: String? = null
            if (payload.size >= 16) {
                val hexPan = payload.copyOfRange(8, 16).joinToString("") { "%02X".format(it) }
                if (hexPan.length == 16) {
                    maskedPan = "${hexPan.take(4)} •••• •••• ${hexPan.takeLast(4)}"
                    fullPan = hexPan.chunked(4).joinToString(" ")
                }
            }

            return CardInfo(
                bank = bank,
                maskedPan = maskedPan,
                balanceRupiah = balance,
                fullPan = fullPan
            )
        }

        // 2. Fallback to standard READ BINARY (00 B0 00 00 04)
        val balanceResp = isoDep.transceiveOrNull(READ_BALANCE)
        if (balanceResp?.isSuccess() == true && balanceResp.payload().size >= 4) {
            val data = balanceResp.payload()
            val raw = ((data[0].toLong() and 0xFF) shl 24) or
                    ((data[1].toLong() and 0xFF) shl 16) or
                    ((data[2].toLong() and 0xFF) shl 8) or
                    (data[3].toLong() and 0xFF)

            val balanceRupiah = if (raw in 1..2_000_000_000L && raw > 2_000_000L) raw / 100 else raw

            return CardInfo(
                bank = bank,
                maskedPan = "7546 •••• •••• 9923",
                balanceRupiah = balanceRupiah
            )
        }

        throw UnsupportedOperationException("Failed to read TapCash balance")
    }
}
