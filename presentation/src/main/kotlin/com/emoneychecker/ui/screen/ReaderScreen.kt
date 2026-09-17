package com.emoneychecker.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.emoneychecker.presentation.R
import com.emoneychecker.ui.component.BottomInstructionSection
import com.emoneychecker.ui.component.CardDisplayView
import com.emoneychecker.ui.component.TopStatusHeader
import com.emoneychecker.ui.haptic.HapticEngine
import com.emoneychecker.ui.state.ReaderIntent
import com.emoneychecker.ui.state.ReaderUiState
import com.emoneychecker.ui.theme.EmoneyColors
import kotlinx.coroutines.launch

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copySuccessMsg = stringResource(R.string.copy_balance_toast)

    // Trigger tactile haptics on state transitions
    LaunchedEffect(state) {
        when (state) {
            is ReaderUiState.Reading -> HapticEngine.onCardDetected(context)
            is ReaderUiState.Success -> HapticEngine.onReadSuccess(context)
            is ReaderUiState.Error -> HapticEngine.onError(context)
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EmoneyColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopStatusHeader(nfcEnabled = state.isNfcEnabled)

            // Centered Hero Card Section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CardDisplayView(state = state)
            }

            BottomInstructionSection(
                state = state,
                onReset = {
                    viewModel.onIntent(ReaderIntent.Reset)
                },
                onOpenNfcSettings = {
                    context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                }
            )
        }
    }
}
