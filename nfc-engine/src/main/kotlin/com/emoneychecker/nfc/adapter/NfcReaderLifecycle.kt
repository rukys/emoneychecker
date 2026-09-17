package com.emoneychecker.nfc.adapter

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag

class NfcReaderLifecycle(
    private val nfcAdapter: NfcAdapter?,
    private val onTagDiscovered: (Tag) -> Unit
) {
    private val readerFlags =
        NfcAdapter.FLAG_READER_NFC_A or
        NfcAdapter.FLAG_READER_NFC_B or
        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

    val isNfcSupported: Boolean
        get() = nfcAdapter != null

    val isNfcEnabled: Boolean
        get() = nfcAdapter?.isEnabled == true

    fun enable(activity: Activity) {
        val options = android.os.Bundle().apply {
            putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 300)
        }
        nfcAdapter?.enableReaderMode(activity, onTagDiscovered, readerFlags, options)
    }

    fun disable(activity: Activity) {
        nfcAdapter?.disableReaderMode(activity)
    }
}
