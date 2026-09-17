package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.nfc.apdu.IsoDepWrapper
import com.emoneychecker.nfc.apdu.hexToByteArray
import com.emoneychecker.nfc.apdu.isSuccess
import com.emoneychecker.nfc.apdu.payload
import java.io.IOException

class TapCashParser : CardParser {
    override val bank: CardBank = CardBank.BNI_TAPCASH

    companion object {
        val AID_TAPCASH = "00 A4 04 00 08 A0 00 00 00 18 43 41 54".hexToByteArray()
        val READ_BALANCE = "00 B0 00 00 04".hexToByteArray()
    }

    override suspend fun parse(isoDep: IsoDepWrapper): CardInfo {
        val selectResp = isoDep.transceive(AID_TAPCASH)
        if (!selectResp.isSuccess()) {
            throw UnsupportedOperationException("BNI TapCash AID selection failed")
        }

        val balanceResp = isoDep.transceive(READ_BALANCE)
        if (!balanceResp.isSuccess()) {
            throw IOException("Failed to read TapCash balance")
        }

        val data = balanceResp.payload()
        if (data.size < 4) {
            throw IOException("TapCash balance payload too short: ${data.size} bytes")
        }

        val balance = ((data[0].toLong() and 0xFF) shl 24) or
                ((data[1].toLong() and 0xFF) shl 16) or
                ((data[2].toLong() and 0xFF) shl 8) or
                (data[3].toLong() and 0xFF)

        val balanceRupiah = balance / 100

        return CardInfo(
            bank = bank,
            maskedPan = "7546 •••• •••• 9923",
            balanceRupiah = balanceRupiah
        )
    }
}
