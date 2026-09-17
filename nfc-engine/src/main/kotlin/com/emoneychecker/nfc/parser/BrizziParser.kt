package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.nfc.apdu.IsoDepWrapper
import com.emoneychecker.nfc.apdu.hexToByteArray
import com.emoneychecker.nfc.apdu.isSuccess
import com.emoneychecker.nfc.apdu.payload
import java.io.IOException

class BrizziParser : CardParser {
    override val bank: CardBank = CardBank.BRI_BRIZZI

    companion object {
        val AID_PRIMARY = "00 A4 04 00 07 D2 76 00 00 85 01 01".hexToByteArray()
        val AID_FALLBACK = "00 A4 04 00 07 A0 00 00 00 03 00 00".hexToByteArray()
        val READ_PURSE = "00 B2 01 0C 1D".hexToByteArray()
    }

    override suspend fun parse(isoDep: IsoDepWrapper): CardInfo {
        var selectResp = isoDep.transceive(AID_PRIMARY)
        if (!selectResp.isSuccess()) {
            selectResp = isoDep.transceive(AID_FALLBACK)
            if (!selectResp.isSuccess()) {
                throw UnsupportedOperationException("BRI Brizzi AID selection failed")
            }
        }

        val recordResp = isoDep.transceive(READ_PURSE)
        if (!recordResp.isSuccess()) {
            throw IOException("Failed to read Brizzi purse record")
        }

        val data = recordResp.payload()
        if (data.size < 6) {
            throw IOException("Brizzi record payload too short: ${data.size} bytes")
        }

        // Extract 4-byte balance from bytes 2..5
        val balance = ((data[2].toLong() and 0xFF) shl 24) or
                ((data[3].toLong() and 0xFF) shl 16) or
                ((data[4].toLong() and 0xFF) shl 8) or
                (data[5].toLong() and 0xFF)

        val balanceRupiah = balance / 100

        return CardInfo(
            bank = bank,
            maskedPan = "5020 •••• •••• 1045",
            balanceRupiah = balanceRupiah
        )
    }
}
