package com.emoneychecker.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.presentation.R
import com.emoneychecker.ui.theme.EmoneyColors

/**
 * High-fidelity vector bank logos for the 4 supported Indonesian e-money issuers:
 * Mandiri e-Money, BCA Flazz, BNI TapCash, and BRI Brizzi.
 */
@Composable
fun BankLogo(
    bank: CardBank,
    modifier: Modifier = Modifier
) {
    when (bank) {
        CardBank.MANDIRI -> MandiriLogo(modifier = modifier)
        CardBank.BCA_FLAZZ -> BcaFlazzLogo(modifier = modifier)
        CardBank.BNI_TAPCASH -> BniTapCashLogo(modifier = modifier)
        CardBank.BRI_BRIZZI -> BriBrizziLogo(modifier = modifier)
        CardBank.UNKNOWN -> UnknownBankLogo(modifier = modifier)
    }
}

/**
 * Bank Mandiri Logo: Official Vector + "e-money" product label
 */
@Composable
fun MandiriLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_bank_mandiri),
            contentDescription = "Bank Mandiri",
            modifier = Modifier.height(20.dp),
            contentScale = ContentScale.Fit
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(14.dp)
                .background(Color.White.copy(alpha = 0.25f))
        )
        Text(
            text = "e-money",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF59E0B),
            letterSpacing = 0.08.sp
        )
    }
}

/**
 * BCA Flazz Logo: Official Vector + "Flazz Gen 2" product label
 */
@Composable
fun BcaFlazzLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_bank_bca),
            contentDescription = "BCA",
            modifier = Modifier.height(20.dp),
            contentScale = ContentScale.Fit
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(14.dp)
                .background(Color.White.copy(alpha = 0.25f))
        )
        Text(
            text = "Flazz Gen 2",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8),
            letterSpacing = 0.06.sp
        )
    }
}

/**
 * BNI TapCash Logo: Official Vector + "TapCash" product label
 */
@Composable
fun BniTapCashLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_bank_bni),
            contentDescription = "BNI",
            modifier = Modifier.height(19.dp),
            contentScale = ContentScale.Fit
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(14.dp)
                .background(Color.White.copy(alpha = 0.25f))
        )
        Text(
            text = "TapCash",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF7A33),
            letterSpacing = 0.06.sp
        )
    }
}

/**
 * BRI Brizzi Logo: Official Vector + "BRIZZI" product label
 */
@Composable
fun BriBrizziLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_bank_bri),
            contentDescription = "BRI",
            modifier = Modifier.height(22.dp),
            contentScale = ContentScale.Fit
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(14.dp)
                .background(Color.White.copy(alpha = 0.25f))
        )
        Text(
            text = "BRIZZI",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8),
            letterSpacing = 0.08.sp
        )
    }
}

@Composable
fun UnknownBankLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(EmoneyColors.SurfaceLevel2),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = EmoneyColors.TextSecondary
            )
        }
        Text(
            text = "KARTU E-MONEY",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = EmoneyColors.TextSecondary
        )
    }
}

/**
 * Authentic Smart Card EMV Chip graphic with circuitry etched lines
 */
@Composable
fun CardEmvChip(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.size(38.dp, 28.dp)
    ) {
        val w = size.width
        val h = size.height
        val corner = 4.dp.toPx()

        // Metallic golden/brushed bronze base
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE5C07B),
                    Color(0xFFD19A66),
                    Color(0xFFC69055),
                    Color(0xFFE5C07B)
                ),
                start = Offset.Zero,
                end = Offset(w, h)
            ),
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = CornerRadius(corner, corner)
        )

        // Chip border
        drawRoundRect(
            color = Color(0xFF8C6633),
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = 0.8.dp.toPx())
        )

        // Center contact rectangle
        val centerW = w * 0.44f
        val centerH = h * 0.52f
        drawRoundRect(
            color = Color(0xFF8C6633),
            topLeft = Offset((w - centerW) / 2f, (h - centerH) / 2f),
            size = Size(centerW, centerH),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = 0.8.dp.toPx())
        )

        // Horizontal middle lines to left and right edges
        drawLine(
            color = Color(0xFF8C6633),
            start = Offset(0f, h / 2f),
            end = Offset((w - centerW) / 2f, h / 2f),
            strokeWidth = 0.8.dp.toPx()
        )
        drawLine(
            color = Color(0xFF8C6633),
            start = Offset((w + centerW) / 2f, h / 2f),
            end = Offset(w, h / 2f),
            strokeWidth = 0.8.dp.toPx()
        )

        // Vertical lines to top and bottom
        drawLine(
            color = Color(0xFF8C6633),
            start = Offset(w * 0.35f, 0f),
            end = Offset(w * 0.35f, (h - centerH) / 2f),
            strokeWidth = 0.8.dp.toPx()
        )
        drawLine(
            color = Color(0xFF8C6633),
            start = Offset(w * 0.65f, 0f),
            end = Offset(w * 0.65f, (h - centerH) / 2f),
            strokeWidth = 0.8.dp.toPx()
        )
        drawLine(
            color = Color(0xFF8C6633),
            start = Offset(w * 0.35f, (h + centerH) / 2f),
            end = Offset(w * 0.35f, h),
            strokeWidth = 0.8.dp.toPx()
        )
        drawLine(
            color = Color(0xFF8C6633),
            start = Offset(w * 0.65f, (h + centerH) / 2f),
            end = Offset(w * 0.65f, h),
            strokeWidth = 0.8.dp.toPx()
        )
    }
}

/**
 * Contactless / NFC Wave Emblem (((
 */
@Composable
fun ContactlessWaveIcon(
    color: Color = Color(0xFF9CA3AF),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp, 16.dp)) {
        val w = size.width
        val h = size.height
        val strokeW = 1.5.dp.toPx()

        // 3 Concentric curved arcs
        // Arc 1 (inner)
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(w * 0.45f, h * 0.2f),
            size = Size(w * 0.35f, h * 0.6f),
            style = Stroke(width = strokeW)
        )

        // Arc 2 (middle)
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.05f),
            size = Size(w * 0.6f, h * 0.9f),
            style = Stroke(width = strokeW)
        )

        // Arc 3 (outer)
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(-w * 0.15f, -h * 0.1f),
            size = Size(w * 0.85f, h * 1.2f),
            style = Stroke(width = strokeW)
        )
    }
}
