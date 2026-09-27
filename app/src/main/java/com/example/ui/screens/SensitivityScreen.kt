package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SensiZone
import com.example.ui.components.GamingSliderWithSteps
import com.example.ui.components.InteractiveTouchPad
import com.example.ui.theme.GamingBorderGlow
import com.example.ui.theme.GamingPanelBg
import com.example.ui.theme.GamingSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CompanionViewModel

@Composable
fun SensitivityScreen(
    viewModel: CompanionViewModel,
    onBack: () -> Unit
) {
    val config by viewModel.sensitivityConfig.collectAsState()
    val profiles by viewModel.sensitivityProfiles.collectAsState()
    val scrollState = rememberScrollState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var profileNameInput by remember { mutableStateOf("") }
    var profileDescInput by remember { mutableStateOf("") }
    var scope2xInput by remember { mutableFloatStateOf(1.0f) }
    var scope4xInput by remember { mutableFloatStateOf(1.0f) }
    var redDotInput by remember { mutableFloatStateOf(1.0f) }
    var gyroInput by remember { mutableFloatStateOf(1.0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = NeonGreen
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "PENGATURAN SENSI",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = "Touch curve coordinator & sensitivity companion",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Sensi Card (matching video 00:19)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(GamingPanelBg)
                .border(1.dp, GamingBorderGlow, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                // Title and description
                Text(
                    text = config.profileName,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mengatur sensitivity x y akan mengubah sentuhan menjadi curve atau lebih jauh dari titik coordinator sentuhan",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sensitivity Global Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sensitivity Global",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = config.globalEnabled,
                        onCheckedChange = { viewModel.updateSensitivity(config.copy(globalEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = GamingSurfaceVariant
                        ),
                        modifier = Modifier.testTag("sensi_global_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Zone Selector Tabs (Kiri, Semua, Kanan)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GamingSurfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SensiZone.entries.forEach { zone ->
                        val isSelected = config.zone == zone
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonGreen else Color.Transparent)
                                .clickable { viewModel.updateSensitivity(config.copy(zone = zone)) }
                                .padding(vertical = 8.dp)
                                .testTag("zone_${zone.name.lowercase()}")
                        ) {
                            Text(
                                text = zone.label,
                                color = if (isSelected) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sensitivity Y Slider
                GamingSliderWithSteps(
                    label = "Sensitivity Y:",
                    value = config.sensitivityY,
                    min = 1.00f,
                    max = 5.00f,
                    step = 0.05f,
                    formatString = "%.2f",
                    onValueChange = { viewModel.updateSensitivity(config.copy(sensitivityY = it)) },
                    onReset = { viewModel.updateSensitivity(config.copy(sensitivityY = 1.20f)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sensitivity X Slider
                GamingSliderWithSteps(
                    label = "Sensitivity X:",
                    value = config.sensitivityX,
                    min = 1.00f,
                    max = 5.00f,
                    step = 0.05f,
                    formatString = "%.2f",
                    onValueChange = { viewModel.updateSensitivity(config.copy(sensitivityX = it)) },
                    onReset = { viewModel.updateSensitivity(config.copy(sensitivityX = 1.00f)) }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rentang nilai precision (min 1.00 - max 5.00)",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Akselerasi Sentuhan Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Akselerasi Sentuhan",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Dinamis kurva sentuhan berdasarkan kecepatan swipe",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = config.touchAcceleration,
                        onCheckedChange = { viewModel.updateSensitivity(config.copy(touchAcceleration = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = GamingSurfaceVariant
                        ),
                        modifier = Modifier.testTag("sensi_accel_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Touch Tester Pad
        Text(
            text = "LIVE TOUCH PAD TESTER",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        InteractiveTouchPad(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            sensitivityX = config.sensitivityX,
            sensitivityY = config.sensitivityY,
            acceleration = config.touchAcceleration
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Saved Profiles
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(GamingPanelBg)
                .border(1.dp, GamingBorderGlow.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SENSI PROFILES",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showSaveDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Save",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "SAVE PROFILE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(profiles) { profile ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(GamingSurfaceVariant)
                                .border(1.dp, GamingBorderGlow.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.clickable { viewModel.applySensitivityProfile(profile) }) {
                                Text(
                                    text = profile.name,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "X: ${String.format("%.2f", profile.sensitivityX)} | Y: ${String.format("%.2f", profile.sensitivityY)}",
                                    color = NeonGreen,
                                    fontSize = 9.sp
                                )
                            }
                            if (!profile.isDefault) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.deleteSensitivityProfile(profile.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(text = "Save Sensitivity Profile to Room DB", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Store current precision curve (X: ${String.format("%.2f", config.sensitivityX)} | Y: ${String.format("%.2f", config.sensitivityY)}) to local database:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Profile Name", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = profileNameInput,
                        onValueChange = { profileNameInput = it },
                        placeholder = { Text("e.g. HEADSHOT CURVE PRO", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("input_sensi_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = GamingBorderGlow,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Description / Weapon Notes", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = profileDescInput,
                        onValueChange = { profileDescInput = it },
                        placeholder = { Text("e.g. Optimized for Free Fire drag headshot", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("input_sensi_desc"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = GamingBorderGlow,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveSensitivityProfile(
                            name = profileNameInput,
                            description = profileDescInput,
                            scope2x = scope2xInput,
                            scope4x = scope4xInput,
                            redDot = redDotInput,
                            gyro = gyroInput
                        )
                        profileNameInput = ""
                        profileDescInput = ""
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier.testTag("save_sensi_confirm_button")
                ) {
                    Text(text = "SAVE PROFILE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showSaveDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GamingSurfaceVariant)
                ) {
                    Text(text = "CANCEL", color = TextPrimary)
                }
            },
            containerColor = GamingPanelBg
        )
    }
}
