package com.emoneychecker.domain.model

sealed class NfcError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    data object TagLost : NfcError("NFC tag was lost during communication")
    data class UnsupportedCard(val reason: String = "") : NfcError("Unsupported card: $reason")
    data object Timeout : NfcError("NFC transceive operation timed out")
    data object NfcDisabled : NfcError("NFC adapter is disabled")
    data class Unknown(override val cause: Throwable) : NfcError(cause.message, cause)
}
