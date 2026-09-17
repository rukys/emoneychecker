package com.emoneychecker.ui.state

import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.domain.model.NfcError

sealed interface ReaderUiState {
    val isNfcEnabled: Boolean get() = true

    data object Idle : ReaderUiState
    data object Reading : ReaderUiState
    data class Success(val cardInfo: CardInfo) : ReaderUiState
    data class Error(val error: NfcError) : ReaderUiState
    data object NfcDisabled : ReaderUiState {
        override val isNfcEnabled: Boolean get() = false
    }
}
