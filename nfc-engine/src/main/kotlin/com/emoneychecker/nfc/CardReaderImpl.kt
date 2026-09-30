package com.emoneychecker.nfc

import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.domain.model.NfcError
import com.emoneychecker.domain.reader.CardReader
import com.emoneychecker.nfc.apdu.AndroidIsoDepWrapper
import com.emoneychecker.nfc.apdu.IsoDepWrapper
import com.emoneychecker.nfc.parser.CardParserFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class CardReaderImpl(
    private val isoDepFactory: ((Tag) -> IsoDepWrapper?)? = null
) : CardReader {

    override suspend fun read(tag: Any): Result<CardInfo> = withContext(Dispatchers.IO) {
        val nfcTag = tag as? Tag ?: return@withContext Result.failure(
            NfcError.Unknown(IllegalArgumentException("Invalid tag type: expected android.nfc.Tag"))
        )

        android.util.Log.d("EmoneyNfc", "Discovered tag: id=${nfcTag.id.joinToString("") { "%02X".format(it) }}, techs=${nfcTag.techList.joinToString(", ")}")

        val isoDepWrapper: IsoDepWrapper = if (isoDepFactory != null) {
            isoDepFactory.invoke(nfcTag) ?: return@withContext Result.failure(
                NfcError.UnsupportedCard("No IsoDep tech available")
            )
        } else {
            val rawIsoDep = IsoDep.get(nfcTag) ?: run {
                android.util.Log.w("EmoneyNfc", "Tag does not support IsoDep: ${nfcTag.techList.joinToString(", ")}")
                return@withContext Result.failure(
                    NfcError.UnsupportedCard("No IsoDep tech — likely MifareClassic (Flazz Gen 1 or non-bank)")
                )
            }
            rawIsoDep.timeout = 2000
            AndroidIsoDepWrapper(rawIsoDep)
        }

        try {
            isoDepWrapper.connect()
            android.util.Log.d("EmoneyNfc", "IsoDep connected successfully")
            val parser = CardParserFactory.detect(isoDepWrapper)
            android.util.Log.d("EmoneyNfc", "Detected parser: ${parser.bank}")
            val cardInfo = parser.parse(isoDepWrapper)
            android.util.Log.d("EmoneyNfc", "Parsed card: ${cardInfo.bank}, balance: Rp ${cardInfo.balanceRupiah}")
            Result.success(cardInfo)
        } catch (e: Throwable) {
            android.util.Log.e("EmoneyNfc", "Card read failed: ${e.javaClass.simpleName} - ${e.message}", e)
            Result.failure(e.toNfcError())
        } finally {
            runCatching { isoDepWrapper.close() }
        }
    }

    private fun Throwable.toNfcError(): NfcError = when (this) {
        is TagLostException -> NfcError.TagLost
        is IOException -> when {
            message?.contains("timeout", ignoreCase = true) == true -> NfcError.Timeout
            message?.contains("lost", ignoreCase = true) == true -> NfcError.TagLost
            else -> NfcError.Unknown(this)
        }
        is UnsupportedOperationException -> NfcError.UnsupportedCard(message ?: "Unsupported")
        is NfcError -> this
        else -> NfcError.Unknown(this)
    }
}
