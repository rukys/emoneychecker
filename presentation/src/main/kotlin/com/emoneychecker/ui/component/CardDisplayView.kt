package com.emoneychecker.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.domain.model.NfcError
import com.emoneychecker.presentation.R
import com.emoneychecker.ui.state.ReaderUiState
import com.emoneychecker.ui.theme.EmoneyColors
import com.emoneychecker.ui.theme.EmoneyTypography
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CardDisplayView(
    state: ReaderUiState,
    modifier: Modifier = Modifier
) {
    val cardDescription = stringResource(R.string.cd_card_area)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f) // Standard ISO/IEC 7810 ID-1 aspect ratio
            .semantics { contentDescription = cardDescription }
            .clip(RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is ReaderUiState.Idle -> IdleCardContent()
            is ReaderUiState.Reading -> ReadingCardContent()
            is ReaderUiState.Success -> SuccessCardContent(cardInfo = state.cardInfo)
            is ReaderUiState.Error -> ErrorCardContent(error = state.error)
            is ReaderUiState.NfcDisabled -> ErrorCardContent(error = NfcError.NfcDisabled)
        }
    }
}

@Composable
private fun IdleCardContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radarScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarScale"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha"
    )
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1116))
            .border(
                width = 1.5.dp,
                color = EmoneyColors.DividerStroke.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle tactile radar wave in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.44f)
            val baseRadius = 46.dp.toPx()

            // Outer expanding radar circle
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = radarAlpha * 0.35f),
                radius = baseRadius * radarScale,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )
            // Inner radar circle
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = radarAlpha * 0.55f),
                radius = baseRadius * (0.6f + radarScale * 0.4f),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID-1 CONTACTLESS",
                    style = EmoneyTypography.StatusTag.copy(fontSize = 10.sp),
                    color = EmoneyColors.TextTertiary
                )
                ContactlessWaveIcon(color = EmoneyColors.TextTertiary)
            }

            // Center: Prompt & Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NfcTransceiverTarget(
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.idle_card_prompt),
                    style = EmoneyTypography.StatusTag.copy(fontSize = 13.sp, letterSpacing = 0.12.em),
                    color = EmoneyColors.TextPrimary
                )
            }

            // Bottom: Supported Bank Logos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_bank_mandiri),
                    contentDescription = "Mandiri",
                    modifier = Modifier.height(13.dp),
                    alpha = 0.55f
                )
                Image(
                    painter = painterResource(R.drawable.ic_bank_bca),
                    contentDescription = "BCA",
                    modifier = Modifier.height(13.dp),
                    alpha = 0.55f
                )
                Image(
                    painter = painterResource(R.drawable.ic_bank_bni),
                    contentDescription = "BNI",
                    modifier = Modifier.height(12.dp),
                    alpha = 0.55f
                )
                Image(
                    painter = painterResource(R.drawable.ic_bank_bri),
                    contentDescription = "BRI",
                    modifier = Modifier.height(14.dp),
                    alpha = 0.55f
                )
            }
        }
    }
}

@Composable
private fun NfcTransceiverTarget(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF10B981)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nfcPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(EmoneyColors.SurfaceLevel2.copy(alpha = 0.75f))
            .border(1.2.dp, color.copy(alpha = 0.25f * pulseAlpha), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(38.dp, 26.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val strokeW = 2.dp.toPx()

            // Center beacon aura + core
            drawCircle(
                color = color.copy(alpha = 0.2f * pulseAlpha),
                radius = 6.dp.toPx(),
                center = Offset(cx, cy)
            )
            drawCircle(
                color = color,
                radius = 3.dp.toPx(),
                center = Offset(cx, cy)
            )

            // Inner waves
            val r1 = 8.5.dp.toPx()
            // Left arc 1
            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(cx - r1, cy - r1),
                size = Size(r1 * 2, r1 * 2),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            // Right arc 1
            drawArc(
                color = color,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(cx - r1, cy - r1),
                size = Size(r1 * 2, r1 * 2),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // Outer waves (subtly pulsing)
            val r2 = 15.dp.toPx()
            // Left arc 2
            drawArc(
                color = color.copy(alpha = 0.5f + 0.4f * pulseAlpha),
                startAngle = 140f,
                sweepAngle = 80f,
                useCenter = false,
                topLeft = Offset(cx - r2, cy - r2),
                size = Size(r2 * 2, r2 * 2),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            // Right arc 2
            drawArc(
                color = color.copy(alpha = 0.5f + 0.4f * pulseAlpha),
                startAngle = -40f,
                sweepAngle = 80f,
                useCenter = false,
                topLeft = Offset(cx - r2, cy - r2),
                size = Size(r2 * 2, r2 * 2),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun ReadingCardContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laser"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF11141C))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF0066CC), Color(0xFFF59E0B))
                ),
                shape = RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Laser Scanline Effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = size.height * laserProgress
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF10B981).copy(alpha = 0.8f),
                        Color.White,
                        Color(0xFF10B981).copy(alpha = 0.8f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.reading_status),
                style = EmoneyTypography.BankNameHeader.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                color = EmoneyColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.reading_hold_still),
                style = EmoneyTypography.Instruction.copy(fontSize = 13.sp),
                color = EmoneyColors.TextSecondary
            )
        }
    }
}

@Composable
private fun SuccessCardContent(cardInfo: CardInfo) {
    val bankAccent = EmoneyColors.forBank(cardInfo.bank)
    val formattedBalance = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        .format(cardInfo.balanceRupiah)
    val context = LocalContext.current
    val copyPanToast = stringResource(R.string.copy_pan_toast)
    val togglePanDesc = stringResource(R.string.cd_toggle_pan)
    var isPanVisible by remember(cardInfo) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        bankAccent.copy(alpha = 0.16f),
                        Color(0xFF141720),
                        Color(0xFF0D0F14)
                    ),
                    center = Offset(Float.POSITIVE_INFINITY, 0f),
                    radius = 900f
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        bankAccent.copy(alpha = 0.8f),
                        EmoneyColors.DividerStroke.copy(alpha = 0.6f),
                        bankAccent.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        // Left Bank Accent Strip
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(4.5.dp)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                .background(bankAccent)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 22.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Row 1: Authentic Bank Logo on left, Contactless Wave on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BankLogo(bank = cardInfo.bank)
                ContactlessWaveIcon(color = bankAccent.copy(alpha = 0.85f))
            }

            // Row 2: EMV Chip + PAN Display with Toggle & Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardEmvChip()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isPanVisible) bankAccent.copy(alpha = 0.15f)
                            else EmoneyColors.SurfaceLevel2.copy(alpha = 0.5f)
                        )
                        .border(
                            width = 0.8.dp,
                            color = if (isPanVisible) bankAccent.copy(alpha = 0.5f) else EmoneyColors.DividerStroke.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable(enabled = cardInfo.fullPan != null) {
                            if (!isPanVisible) {
                                isPanVisible = true
                            } else {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val cleanPan = (cardInfo.fullPan ?: cardInfo.maskedPan).replace(" ", "")
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Card Number", cleanPan))
                                Toast.makeText(context, copyPanToast, Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .semantics { contentDescription = togglePanDesc }
                ) {
                    val panText = if (isPanVisible) (cardInfo.fullPan ?: cardInfo.maskedPan) else cardInfo.maskedPan
                    Text(
                        text = panText,
                        style = EmoneyTypography.CardNumberMasked.copy(
                            fontSize = 12.5.sp,
                            letterSpacing = 0.10.em,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isPanVisible) EmoneyColors.TextPrimary else EmoneyColors.TextSecondary
                    )

                    if (cardInfo.fullPan != null) {
                        if (isPanVisible) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val cleanPan = (cardInfo.fullPan ?: cardInfo.maskedPan).replace(" ", "")
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Card Number", cleanPan))
                                        Toast.makeText(context, copyPanToast, Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(2.dp)
                            ) {
                                CopyIcon(tint = bankAccent)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    isPanVisible = !isPanVisible
                                }
                                .padding(2.dp)
                        ) {
                            EyeToggleIcon(
                                isVisible = isPanVisible,
                                tint = if (isPanVisible) bankAccent else EmoneyColors.TextTertiary
                            )
                        }
                    }
                }
            }

            // Row 3: Hero Balance Display
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.currency_symbol),
                    style = EmoneyTypography.CurrencySymbol.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = EmoneyColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp, end = 8.dp)
                )
                Text(
                    text = formattedBalance,
                    style = EmoneyTypography.BalanceDisplay.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = EmoneyColors.TextPrimary
                )
            }

            // Row 4: Relative timestamp + Card Standard Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = stringResource(R.string.success_last_read_label),
                        style = EmoneyTypography.Instruction.copy(fontSize = 11.sp),
                        color = EmoneyColors.TextTertiary
                    )
                    Text(
                        text = formatRelativeTimestamp(cardInfo.readTimestamp),
                        style = EmoneyTypography.Instruction.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = EmoneyColors.TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(EmoneyColors.SurfaceLevel2.copy(alpha = 0.7f))
                        .border(0.5.dp, EmoneyColors.DividerStroke, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ISO 14443-4",
                        style = EmoneyTypography.StatusTag.copy(fontSize = 9.sp, letterSpacing = 0.05.em),
                        color = EmoneyColors.TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorCardContent(error: NfcError) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = 1.5.dp,
                color = EmoneyColors.StatusError,
                shape = RoundedCornerShape(18.dp)
            )
            .background(Color(0xFF161214)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(EmoneyColors.StatusError.copy(alpha = 0.15f))
                    .border(1.dp, EmoneyColors.StatusError.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmoneyColors.StatusError
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(errorTitleRes(error)),
                style = EmoneyTypography.BankNameHeader.copy(fontWeight = FontWeight.Bold),
                color = EmoneyColors.StatusError,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(errorSubtitleRes(error)),
                style = EmoneyTypography.Instruction.copy(fontSize = 12.sp),
                color = EmoneyColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun errorTitleRes(error: NfcError): Int = when (error) {
    is NfcError.TagLost -> R.string.error_tag_lost_title
    is NfcError.UnsupportedCard -> if (error.reason.contains("Flazz Gen 1", ignoreCase = true)) {
        R.string.error_unsupported_flazz_gen1_title
    } else {
        R.string.error_unsupported_title
    }
    is NfcError.Timeout -> R.string.error_timeout_title
    is NfcError.NfcDisabled -> R.string.error_nfc_disabled_title
    is NfcError.Unknown -> R.string.error_unknown_title
}

private fun errorSubtitleRes(error: NfcError): Int = when (error) {
    is NfcError.TagLost -> R.string.error_tag_lost_subtitle
    is NfcError.UnsupportedCard -> if (error.reason.contains("Flazz Gen 1", ignoreCase = true)) {
        R.string.error_unsupported_flazz_gen1_subtitle
    } else {
        R.string.error_unsupported_generic
    }
    is NfcError.Timeout -> R.string.error_timeout_subtitle
    is NfcError.NfcDisabled -> R.string.error_nfc_disabled_subtitle
    is NfcError.Unknown -> R.string.error_unknown_subtitle
}

@Composable
private fun formatRelativeTimestamp(readTime: Instant): String {
    val duration = Duration.between(readTime, Instant.now())
    val seconds = duration.seconds
    return when {
        seconds < 10 -> stringResource(R.string.success_last_read_just_now)
        seconds < 60 -> stringResource(R.string.success_last_read_seconds, seconds)
        seconds < 3600 -> stringResource(R.string.success_last_read_minutes, seconds / 60)
        else -> DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(readTime)
    }
}

@Composable
private fun EyeToggleIcon(
    isVisible: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(16.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()

        // Eye almond outline
        val eyePath = Path().apply {
            moveTo(0.06f * w, 0.5f * h)
            cubicTo(
                0.26f * w, 0.15f * h,
                0.74f * w, 0.15f * h,
                0.94f * w, 0.5f * h
            )
            cubicTo(
                0.74f * w, 0.85f * h,
                0.26f * w, 0.85f * h,
                0.06f * w, 0.5f * h
            )
            close()
        }

        drawPath(
            path = eyePath,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Center pupil
        drawCircle(
            color = tint,
            radius = 0.18f * w,
            center = Offset(0.5f * w, 0.5f * h)
        )

        // Slashed diagonal if hidden
        if (!isVisible) {
            drawLine(
                color = tint,
                start = Offset(0.12f * w, 0.88f * h),
                end = Offset(0.88f * w, 0.12f * h),
                strokeWidth = strokeWidth * 1.15f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun CopyIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(14.dp)) {
        val w = size.width
        val h = size.height
        val stroke = 1.3.dp.toPx()

        // Back page
        drawRoundRect(
            color = tint.copy(alpha = 0.6f),
            topLeft = Offset(0.28f * w, 0.06f * h),
            size = Size(0.62f * w, 0.72f * h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
            style = Stroke(width = stroke)
        )
        // Front page
        drawRoundRect(
            color = tint,
            topLeft = Offset(0.08f * w, 0.22f * h),
            size = Size(0.62f * w, 0.72f * h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
            style = Stroke(width = stroke)
        )
    }
}
