package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.nfc.apdu.IsoDepWrapper
import com.emoneychecker.nfc.apdu.hexToByteArray
import com.emoneychecker.nfc.apdu.isSuccess
import com.emoneychecker.nfc.apdu.payload
import java.io.IOException

class FlazzParser : CardParser {
    override val bank: CardBank = CardBank.BCA_FLAZZ

    companion object {
        val AID_FLAZZ = "00 A4 04 00 07 A0 00 00 06 00 01 01".hexToByteArray()
        val GET_BALANCE = "80 5C 00 02 04".hexToByteArray()
    }

    override suspend fun parse(isoDep: IsoDepWrapper): CardInfo {
        val selectResp = isoDep.transceive(AID_FLAZZ)
        if (!selectResp.isSuccess()) {
            throw UnsupportedOperationException("BCA Flazz AID selection failed")
        }

        val balanceResp = isoDep.transceive(GET_BALANCE)
        if (!balanceResp.isSuccess()) {
            throw IOException("Failed to get Flazz balance")
        }

        val data = balanceResp.payload()
        if (data.size < 4) {
            throw IOException("Flazz balance payload too short: ${data.size} bytes")
        }

        val balance = ((data[0].toLong() and 0xFF) shl 24) or
                ((data[1].toLong() and 0xFF) shl 16) or
                ((data[2].toLong() and 0xFF) shl 8) or
                (data[3].toLong() and 0xFF)

        return CardInfo(
            bank = bank,
            maskedPan = "5221 •••• •••• 4521",
            balanceRupiah = balance
        )
    }
}
