package com.emoneychecker.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emoneychecker.presentation.R
import com.emoneychecker.ui.state.ReaderUiState
import com.emoneychecker.ui.theme.EmoneyColors
import com.emoneychecker.ui.theme.EmoneyTypography

@Composable
fun BottomInstructionSection(
    state: ReaderUiState,
    onReset: () -> Unit,
    onOpenNfcSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onCopyBalance: ((Long) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state) {
            is ReaderUiState.Success -> {
                val resetCd = stringResource(R.string.cd_reset_button)
                // Primary Thumb-Zone Action Button: Reset / Bersihkan Layar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(EmoneyColors.SurfaceLevel2)
                        .border(1.2.dp, EmoneyColors.DividerStroke, RoundedCornerShape(14.dp))
                        .clickable { onReset() }
                        .semantics { contentDescription = resetCd },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "↺",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF3F4F6)
                        )
                        Text(
                            text = stringResource(R.string.action_reset),
                            style = EmoneyTypography.StatusTag.copy(
                                fontSize = 14.sp,
                                letterSpacing = 0.06.em,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFF3F4F6)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Tempel kartu kapan saja untuk membaca ulang",
                    style = EmoneyTypography.Instruction.copy(fontSize = 12.sp),
                    color = EmoneyColors.TextTertiary,
                    textAlign = TextAlign.Center
                )
            }

            is ReaderUiState.NfcDisabled -> {
                val settingsCd = stringResource(R.string.cd_nfc_settings_button)
                Button(
                    onClick = onOpenNfcSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmoneyColors.StatusError,
                        contentColor = EmoneyColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics { contentDescription = settingsCd }
                ) {
                    Text(
                        text = stringResource(R.string.error_nfc_cta_button),
                        style = EmoneyTypography.StatusTag.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }

            else -> {
                Text(
                    text = stringResource(R.string.idle_instruction),
                    style = EmoneyTypography.Instruction.copy(fontSize = 14.sp),
                    color = EmoneyColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.idle_supported_cards),
                    style = EmoneyTypography.StatusTag.copy(fontSize = 11.sp),
                    color = EmoneyColors.TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
