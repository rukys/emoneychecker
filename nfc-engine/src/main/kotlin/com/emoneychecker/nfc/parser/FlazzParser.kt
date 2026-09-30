package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.nfc.apdu.IsoDepWrapper
import com.emoneychecker.nfc.apdu.hexToByteArray
import com.emoneychecker.nfc.apdu.isSuccess
import com.emoneychecker.nfc.apdu.payload
import com.emoneychecker.nfc.apdu.transceiveOrNull
import java.io.IOException

class FlazzParser : CardParser {
    override val bank: CardBank = CardBank.BCA_FLAZZ

    companion object {
        val AID_FLAZZ = "00 A4 04 00 07 A0 00 00 06 00 01 01".hexToByteArray()
        val AID_FLAZZ_LE = "00 A4 04 00 07 A0 00 00 06 00 01 01 00".hexToByteArray()
        val AID_FLAZZ_ALT = "00 A4 04 00 0B A0 00 00 00 18 0F 00 00 01 80 01".hexToByteArray()

        val CANDIDATE_AIDS = listOf(AID_FLAZZ, AID_FLAZZ_LE, AID_FLAZZ_ALT)

        val GET_BALANCE = "80 5C 00 02 04".hexToByteArray()
        val READ_RECORD = "00 B2 01 0C 00".hexToByteArray()
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
            throw UnsupportedOperationException("BCA Flazz AID selection failed")
        }

        val balanceResp = isoDep.transceiveOrNull(GET_BALANCE)
        if (balanceResp?.isSuccess() != true) {
            throw UnsupportedOperationException("Failed to get Flazz balance")
        }

        val data = balanceResp.payload()
        if (data.size < 4) {
            throw UnsupportedOperationException("Flazz balance payload too short: ${data.size} bytes")
        }

        val balance = ((data[0].toLong() and 0xFF) shl 24) or
                ((data[1].toLong() and 0xFF) shl 16) or
                ((data[2].toLong() and 0xFF) shl 8) or
                (data[3].toLong() and 0xFF)

        var maskedPan = "5221 •••• •••• 4521"
        var fullPan: String? = null
        val recordResp = isoDep.transceiveOrNull(READ_RECORD)
        if (recordResp?.isSuccess() == true) {
            val hex = recordResp.payload().joinToString("") { "%02X".format(it) }
            val match = Regex("(5221\\d{12}|6013\\d{12}|\\d{16})").find(hex)
            if (match != null) {
                val pan = match.value
                maskedPan = "${pan.take(4)} •••• •••• ${pan.takeLast(4)}"
                fullPan = pan.chunked(4).joinToString(" ")
            }
        }

        return CardInfo(
            bank = bank,
            maskedPan = maskedPan,
            balanceRupiah = balance,
            fullPan = fullPan
        )
    }
}
