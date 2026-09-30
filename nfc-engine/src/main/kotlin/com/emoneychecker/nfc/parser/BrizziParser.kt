package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.nfc.apdu.IsoDepWrapper
import com.emoneychecker.nfc.apdu.hexToByteArray
import com.emoneychecker.nfc.apdu.isSuccess
import com.emoneychecker.nfc.apdu.payload
import com.emoneychecker.nfc.apdu.transceiveOrNull

class BrizziParser : CardParser {
    override val bank: CardBank = CardBank.BRI_BRIZZI

    companion object {
        val AID_PRIMARY = "00 A4 04 00 07 D2 76 00 00 85 01 01".hexToByteArray()
        val AID_FALLBACK = "00 A4 04 00 07 A0 00 00 00 03 00 00".hexToByteArray()

        val CANDIDATE_AIDS = listOf(AID_PRIMARY, AID_FALLBACK)

        val READ_PURSE_1D = "00 B2 01 0C 1D".hexToByteArray()
        val READ_PURSE_00 = "00 B2 01 0C 00".hexToByteArray()
        val READ_PURSE = READ_PURSE_1D
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
            throw UnsupportedOperationException("BRI Brizzi AID selection failed")
        }

        var recordResp = isoDep.transceiveOrNull(READ_PURSE_1D)
        if (recordResp?.isSuccess() != true) {
            recordResp = isoDep.transceiveOrNull(READ_PURSE_00)
        }

        if (recordResp?.isSuccess() != true) {
            throw UnsupportedOperationException("Failed to read Brizzi purse record")
        }

        val data = recordResp.payload()
        if (data.size < 6) {
            throw UnsupportedOperationException("Brizzi record payload too short: ${data.size} bytes")
        }

        // Extract 4-byte balance from bytes 2..5
        val raw = ((data[2].toLong() and 0xFF) shl 24) or
                ((data[3].toLong() and 0xFF) shl 16) or
                ((data[4].toLong() and 0xFF) shl 8) or
                (data[5].toLong() and 0xFF)

        val balanceRupiah = raw / 100

        var maskedPan = "5020 •••• •••• 1045"
        var fullPan: String? = null
        val recordHex = data.joinToString("") { "%02X".format(it) }
        val panMatch = Regex("(5020\\d{12}|\\d{16})").find(recordHex)
        if (panMatch != null) {
            val pan = panMatch.value
            maskedPan = "${pan.take(4)} •••• •••• ${pan.takeLast(4)}"
            fullPan = pan.chunked(4).joinToString(" ")
        }

        return CardInfo(
            bank = bank,
            maskedPan = maskedPan,
            balanceRupiah = balanceRupiah,
            fullPan = fullPan
        )
    }
}
