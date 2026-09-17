package com.emoneychecker.domain.reader

import com.emoneychecker.domain.model.CardInfo

interface CardReader {
    /**
     * Membaca informasi kartu dari NFC tag yang terdeteksi.
     * @param tag opaque object (android.nfc.Tag di Android runtime)
     * @return Result.success(CardInfo) atau Result.failure(Exception wrapping NfcError)
     */
    suspend fun read(tag: Any): Result<CardInfo>
}
