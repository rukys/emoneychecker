package com.emoneychecker.domain.model

import java.time.Instant

data class CardInfo(
    val bank: CardBank,
    val maskedPan: String,
    val balanceRupiah: Long,
    val readTimestamp: Instant = Instant.now()
)
