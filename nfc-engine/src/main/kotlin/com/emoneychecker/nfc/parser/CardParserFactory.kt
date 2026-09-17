package com.emoneychecker.nfc.parser

import com.emoneychecker.nfc.apdu.IsoDepWrapper
import com.emoneychecker.nfc.apdu.isSuccess

object CardParserFactory {
    /**
     * Probes card to identify bank.
     * Probe sequence: Mandiri -> Flazz -> TapCash -> Brizzi -> Fallback
     */
    fun detect(isoDep: IsoDepWrapper): CardParser {
        // Probe Mandiri
        for (aid in MandiriParser.CANDIDATE_AIDS) {
            val resp = runCatching { isoDep.transceive(aid) }.getOrNull()
            if (resp?.isSuccess() == true) {
                return MandiriParser()
            }
        }

        // Probe Flazz Gen 2
        val flazzResp = runCatching { isoDep.transceive(FlazzParser.AID_FLAZZ) }.getOrNull()
        if (flazzResp?.isSuccess() == true) {
            return FlazzParser()
        }

        // Probe TapCash
        val tapCashResp = runCatching { isoDep.transceive(TapCashParser.AID_TAPCASH) }.getOrNull()
        if (tapCashResp?.isSuccess() == true) {
            return TapCashParser()
        }

        // Probe Brizzi
        val brizziResp = runCatching { isoDep.transceive(BrizziParser.AID_PRIMARY) }.getOrNull()
        if (brizziResp?.isSuccess() == true) {
            return BrizziParser()
        }

        // Probe Fallback AID
        val fallbackResp = runCatching { isoDep.transceive(MandiriParser.AID_FALLBACK) }.getOrNull()
        if (fallbackResp?.isSuccess() == true) {
            return MandiriParser()
        }

        throw UnsupportedOperationException("No matching e-money card parser found")
    }
}
