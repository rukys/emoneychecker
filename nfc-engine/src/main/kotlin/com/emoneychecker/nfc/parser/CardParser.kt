package com.emoneychecker.nfc.parser

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.nfc.apdu.IsoDepWrapper

interface CardParser {
    val bank: CardBank
    suspend fun parse(isoDep: IsoDepWrapper): CardInfo
}
