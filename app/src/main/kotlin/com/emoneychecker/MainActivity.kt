package com.emoneychecker

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import com.emoneychecker.nfc.adapter.NfcReaderLifecycle
import com.emoneychecker.ui.screen.ReaderScreen
import com.emoneychecker.ui.screen.ReaderViewModel
import com.emoneychecker.ui.state.ReaderIntent
import com.emoneychecker.ui.theme.EmoneyTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val viewModel: ReaderViewModel by inject()

    private val nfcLifecycle by lazy {
        val adapter = NfcAdapter.getDefaultAdapter(this)
        NfcReaderLifecycle(adapter) { tag ->
            viewModel.onIntent(ReaderIntent.TagDetected(tag))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            EmoneyTheme {
                ReaderScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcLifecycle.enable(this)
        viewModel.onIntent(ReaderIntent.NfcStatusChanged(nfcLifecycle.isNfcEnabled))
    }

    override fun onPause() {
        super.onPause()
        nfcLifecycle.disable(this)
    }
}
