package com.emoneychecker.nfc.parser

import android.nfc.TagLostException
import com.emoneychecker.nfc.apdu.IsoDepWrapper
import com.emoneychecker.nfc.apdu.isSuccess
import com.emoneychecker.nfc.apdu.transceiveOrNull

object CardParserFactory {
    /**
     * Probes card to identify bank.
     * Probe sequence: Mandiri -> Flazz -> TapCash -> Brizzi -> Fallback
     */
    @Throws(TagLostException::class)
    fun detect(isoDep: IsoDepWrapper): CardParser {
        // Probe Mandiri (specific AIDs only)
        for (aid in MandiriParser.PRIMARY_AIDS) {
            val resp = isoDep.transceiveOrNull(aid)
            if (resp?.isSuccess() == true) {
                return MandiriParser()
            }
        }

        // Probe Flazz Gen 2
        for (aid in FlazzParser.CANDIDATE_AIDS) {
            val resp = isoDep.transceiveOrNull(aid)
            if (resp?.isSuccess() == true) {
                return FlazzParser()
            }
        }

        // Probe TapCash
        for (aid in TapCashParser.CANDIDATE_AIDS) {
            val resp = isoDep.transceiveOrNull(aid)
            if (resp?.isSuccess() == true) {
                return TapCashParser()
            }
        }

        // Probe Brizzi
        val brizziResp = isoDep.transceiveOrNull(BrizziParser.AID_PRIMARY)
        if (brizziResp?.isSuccess() == true) {
            return BrizziParser()
        }

        // Probe Fallback AID
        val fallbackResp = isoDep.transceiveOrNull(MandiriParser.AID_FALLBACK)
        if (fallbackResp?.isSuccess() == true) {
            return MandiriParser()
        }

        throw UnsupportedOperationException("No matching e-money card parser found")
    }
}
