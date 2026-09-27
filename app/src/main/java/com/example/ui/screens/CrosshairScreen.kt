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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.model.CrosshairStyle
import com.example.ui.components.CrosshairRenderer
import com.example.ui.components.DpadPositionController
import com.example.ui.components.GamingSliderWithSteps
import com.example.ui.theme.GamingBorderGlow
import com.example.ui.theme.GamingPanelBg
import com.example.ui.theme.GamingSurface
import com.example.ui.theme.GamingSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CompanionViewModel

@Composable
fun CrosshairScreen(
    viewModel: CompanionViewModel,
    onBack: () -> Unit
) {
    val config by viewModel.crosshairConfig.collectAsState()
    val presets by viewModel.crosshairPresets.collectAsState()
    val scrollState = rememberScrollState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NeonGreen
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Crosshair Assist",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Custom style, posisi, dan ukuran aiming",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Master Switch
            Switch(
                checked = config.isEnabled,
                onCheckedChange = { viewModel.updateCrosshair(config.copy(isEnabled = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = NeonGreen,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = GamingSurfaceVariant
                ),
                modifier = Modifier.testTag("crosshair_master_switch")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Live Preview Arena
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(GamingSurface)
                .border(1.dp, GamingBorderGlow, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Background Grid Lines
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = androidx.compose.ui.geometry.Offset(0f, center.y),
                    end = androidx.compose.ui.geometry.Offset(size.width, center.y),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = androidx.compose.ui.geometry.Offset(center.x, 0f),
                    end = androidx.compose.ui.geometry.Offset(center.x, size.height),
                    strokeWidth = 1f
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = 40.dp.toPx(),
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                )
            }

            // The Actual Crosshair
            CrosshairRenderer(
                config = config,
                modifier = Modifier.size(160.dp)
            )

            Text(
                text = if (config.isEnabled) "ACTIVE PREVIEW" else "CROSSHAIR DISABLED",
                color = if (config.isEnabled) NeonGreen else TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Position & D-pad Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(GamingPanelBg)
                .border(1.dp, GamingBorderGlow.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "POSITION",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.resetCrosshairX() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(text = "RESET X", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { viewModel.resetCrosshairY() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(text = "RESET Y", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                DpadPositionController(
                    offsetX = config.offsetX,
                    offsetY = config.offsetY,
                    onStepChange = { dx, dy -> viewModel.updateCrosshairOffset(dx * 2, dy * 2) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Style Selection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(GamingPanelBg)
                .border(1.dp, GamingBorderGlow.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "STYLE",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CrosshairStyle.entries.forEach { style ->
                        val isSelected = config.style == style
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NeonGreen.copy(alpha = 0.25f) else GamingSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonGreen else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.updateCrosshair(config.copy(style = style)) }
                                .testTag("style_${style.name.lowercase()}")
                        ) {
                            CrosshairRenderer(
                                config = config.copy(
                                    style = style,
                                    offsetX = 0,
                                    offsetY = 0,
                                    sizePercent = 60
                                ),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Color Selection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(GamingPanelBg)
                .border(1.dp, GamingBorderGlow.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "COLOR",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val colors = listOf("#00FF88", "#00E5FF", "#FFE600", "#FF3366", "#FFFFFF")
                    colors.forEach { hex ->
                        val isSelected = config.colorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    2.dp,
                                    if (isSelected) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { viewModel.updateCrosshair(config.copy(colorHex = hex)) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sliders Section: Size, Opacity, Thickness, Rotation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(GamingPanelBg)
                .border(1.dp, GamingBorderGlow.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                // Size
                GamingSliderWithSteps(
                    label = "SIZE CROSSHAIR",
                    value = config.sizePercent.toFloat(),
                    min = 10f,
                    max = 100f,
                    step = 5f,
                    formatString = "%.0f%%",
                    onValueChange = { viewModel.updateCrosshair(config.copy(sizePercent = it.toInt())) },
                    onReset = { viewModel.resetCrosshairSize() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Opacity
                GamingSliderWithSteps(
                    label = "OPACITY",
                    value = config.opacityPercent.toFloat(),
                    min = 10f,
                    max = 100f,
                    step = 5f,
                    formatString = "%.0f%%",
                    onValueChange = { viewModel.updateCrosshair(config.copy(opacityPercent = it.toInt())) },
                    onReset = { viewModel.updateCrosshair(config.copy(opacityPercent = 100)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Thickness
                GamingSliderWithSteps(
                    label = "THICKNESS",
                    value = config.thicknessDp,
                    min = 1.0f,
                    max = 8.0f,
                    step = 0.5f,
                    formatString = "%.1f dp",
                    onValueChange = { viewModel.updateCrosshair(config.copy(thicknessDp = it)) },
                    onReset = { viewModel.updateCrosshair(config.copy(thicknessDp = 2.5f)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rotation
                GamingSliderWithSteps(
                    label = "ROTATION",
                    value = config.rotationDeg,
                    min = 0f,
                    max = 360f,
                    step = 5f,
                    formatString = "%.0f°",
                    onValueChange = { viewModel.updateCrosshair(config.copy(rotationDeg = it)) },
                    onReset = { viewModel.updateCrosshair(config.copy(rotationDeg = 0f)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Presets List & Save
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
                        text = "SAVED PRESETS",
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
                        Text(text = "SAVE PRESET", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(presets) { preset ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(GamingSurfaceVariant)
                                .border(1.dp, GamingBorderGlow.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.clickable { viewModel.applyCrosshairPreset(preset) }) {
                                Text(
                                    text = preset.name,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${preset.style} • ${preset.sizePercent}%",
                                    color = NeonGreen,
                                    fontSize = 9.sp
                                )
                            }
                            if (!preset.isDefault) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.deleteCrosshairPreset(preset.id) },
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
            title = { Text(text = "Save Crosshair Preset", color = Color.White) },
            text = {
                Column {
                    Text(text = "Enter a name for this custom crosshair configuration:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        placeholder = { Text("e.g. SNIPER CYAN") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = GamingBorderGlow,
                            focusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCrosshairPreset(presetNameInput)
                        presetNameInput = ""
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text(text = "SAVE", color = Color.Black, fontWeight = FontWeight.Bold)
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
