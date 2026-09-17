package com.emoneychecker.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emoneychecker.presentation.R
import com.emoneychecker.ui.theme.EmoneyColors
import com.emoneychecker.ui.theme.EmoneyTypography

@Composable
fun TopStatusHeader(
    nfcEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotPulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // NFC Status Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (nfcEnabled) Color(0xFF0F1E19) else Color(0xFF241416)
                )
                .border(
                    width = 1.dp,
                    color = if (nfcEnabled) Color(0xFF10B981).copy(alpha = 0.35f) else Color(0xFFEF4444).copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (nfcEnabled) {
                            Color(0xFF10B981).copy(alpha = dotAlpha)
                        } else {
                            Color(0xFFEF4444)
                        }
                    )
            )
            Text(
                text = stringResource(
                    if (nfcEnabled) R.string.nfc_status_active else R.string.nfc_status_inactive
                ),
                style = EmoneyTypography.StatusTag.copy(fontSize = 11.sp),
                color = if (nfcEnabled) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        }

        // Tactical Version Tag
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(EmoneyColors.SurfaceLevel1)
                .border(0.5.dp, EmoneyColors.DividerStroke, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.version_label),
                style = EmoneyTypography.StatusTag.copy(fontSize = 10.sp),
                color = EmoneyColors.TextTertiary
            )
        }
    }
}
