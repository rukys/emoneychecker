package com.emoneychecker.domain.usecase

import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.domain.reader.CardReader

class ReadCardUseCase(private val cardReader: CardReader) {
    suspend operator fun invoke(tag: Any): Result<CardInfo> {
        return cardReader.read(tag)
    }
}
