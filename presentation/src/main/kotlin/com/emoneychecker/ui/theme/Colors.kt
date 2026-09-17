package com.emoneychecker.ui.theme

import androidx.compose.ui.graphics.Color
import com.emoneychecker.domain.model.CardBank

object EmoneyColors {
    val Background = Color(0xFF090A0D)
    val SurfaceLevel1 = Color(0xFF13161C)
    val SurfaceLevel2 = Color(0xFF1E222B)
    val DividerStroke = Color(0xFF2D323F)

    val TextPrimary = Color(0xFFF3F4F6)
    val TextSecondary = Color(0xFF9CA3AF)
    val TextTertiary = Color(0xFF6B7280)

    val Mandiri = Color(0xFFF59E0B)
    val BcaFlazz = Color(0xFF0066CC)
    val BniTapCash = Color(0xFFFF5500)
    val BriBrizzi = Color(0xFF00529C)

    val StatusError = Color(0xFFEF4444)
    val StatusActive = Color(0xFF10B981)
    val StatusInactive = Color(0xFFEF4444)

    fun forBank(bank: CardBank): Color = when (bank) {
        CardBank.MANDIRI -> Mandiri
        CardBank.BCA_FLAZZ -> BcaFlazz
        CardBank.BNI_TAPCASH -> BniTapCash
        CardBank.BRI_BRIZZI -> BriBrizzi
        CardBank.UNKNOWN -> DividerStroke
    }
}
