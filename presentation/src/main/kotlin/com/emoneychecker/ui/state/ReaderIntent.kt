package com.emoneychecker.ui.state

sealed interface ReaderIntent {
    data class TagDetected(val tag: Any) : ReaderIntent
    data object CopyBalance : ReaderIntent
    data object DismissError : ReaderIntent
    data object Reset : ReaderIntent
    data class NfcStatusChanged(val isEnabled: Boolean) : ReaderIntent
}
