package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.db.GameProfileEntity
import com.example.data.model.PerformanceTier
import com.example.data.model.SensiZone
import com.example.ui.theme.GamingBorderGlow
import com.example.ui.theme.GamingPanelBg
import com.example.ui.theme.GamingSurface
import com.example.ui.theme.GamingSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CompanionViewModel
import com.example.ui.viewmodel.InstalledAppItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameProfileScreen(
    viewModel: CompanionViewModel,
    onBack: () -> Unit
) {
    val installedApps by viewModel.installedApps.collectAsState()
    val gameProfiles by viewModel.gameProfiles.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Configured Room Profiles, 1: Installed Apps

    // Dialog state for creating or editing user-defined game profiles
    var showEditDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<GameProfileEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                        text = "GAME PROFILES (ROOM DB)",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "User-defined game profiles & custom sensitivity",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Quick Add Custom Profile Button
            Button(
                onClick = {
                    editingProfile = null
                    showEditDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("add_custom_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "NEW",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: My Room Profiles vs Installed Apps
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GamingSurfaceVariant)
                .padding(4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 0) NeonGreen else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 8.dp)
                    .testTag("tab_my_profiles")
            ) {
                Text(
                    text = "MY PROFILES (${gameProfiles.size})",
                    color = if (selectedTab == 0) Color.Black else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 1) NeonGreen else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 8.dp)
                    .testTag("tab_installed_apps")
            ) {
                Text(
                    text = "INSTALLED APPS (${installedApps.size})",
                    color = if (selectedTab == 1) Color.Black else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Room DB Configured Game Profiles
            if (gameProfiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Games",
                            tint = TextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No user game profiles stored in Room yet.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                editingProfile = null
                                showEditDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "CREATE CUSTOM GAME PROFILE", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(gameProfiles, key = { it.id }) { profile ->
                        val matchingApp = installedApps.find { it.packageName == profile.packageName }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(GamingSurface)
                                .border(1.dp, GamingBorderGlow.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                                .testTag("game_profile_card_${profile.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (matchingApp?.icon != null) {
                                        val bitmap = matchingApp.icon.toBitmap(96, 96)
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = profile.appName,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(GamingSurfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Games,
                                                contentDescription = null,
                                                tint = NeonGreen
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = profile.appName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = profile.packageName,
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            editingProfile = profile
                                            showEditDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Profile",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteGameProfileById(profile.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Profile",
                                            tint = Color.Red.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Badges for Custom Sensitivity & Performance
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GamingSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "X: ${String.format("%.2f", profile.customSensitivityX)} | Y: ${String.format("%.2f", profile.customSensitivityY)}",
                                        color = NeonGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GamingSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Accel: ${if (profile.touchAcceleration) "ON" else "OFF"}",
                                        color = if (profile.touchAcceleration) NeonCyan else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GamingSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Zone: ${profile.sensitivityZone}",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GamingSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${profile.performanceTier} • ${profile.targetFps} FPS",
                                        color = TextPrimary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            if (profile.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Notes: ${profile.notes}",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    maxLines = 2
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action button: Apply sensitivity settings and Launch
                            Button(
                                onClick = { viewModel.applyGameProfile(profile, launchNow = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Apply & Launch",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "APPLY SENSI & LAUNCH GAME",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Installed Apps List from PackageManager
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(installedApps) { appItem ->
                    val isAlreadyProfile = gameProfiles.any { it.packageName == appItem.packageName }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(GamingSurface)
                            .border(1.dp, GamingBorderGlow.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            if (appItem.icon != null) {
                                val bitmap = appItem.icon.toBitmap(96, 96)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = appItem.appName,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GamingSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Games,
                                        contentDescription = null,
                                        tint = NeonGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = appItem.appName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = appItem.packageName,
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    editingProfile = GameProfileEntity(
                                        packageName = appItem.packageName,
                                        appName = appItem.appName,
                                        customSensitivityX = viewModel.sensitivityConfig.value.sensitivityX,
                                        customSensitivityY = viewModel.sensitivityConfig.value.sensitivityY,
                                        touchAcceleration = viewModel.sensitivityConfig.value.touchAcceleration,
                                        sensitivityZone = viewModel.sensitivityConfig.value.zone.name,
                                        performanceTier = viewModel.performanceTier.value.name,
                                        targetFps = 60,
                                        notes = "Profile for ${appItem.appName}"
                                    )
                                    showEditDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GamingSurfaceVariant),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (isAlreadyProfile) "+ ADD VARIANT" else "+ CONFIGURE",
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = { viewModel.launchApp(appItem.packageName) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Launch",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for creating or updating a user-defined game profile with custom sensitivity
    if (showEditDialog) {
        UserGameProfileDialog(
            initialProfile = editingProfile,
            onDismiss = { showEditDialog = false },
            onSave = { profileToSave ->
                if (profileToSave.id == 0L) {
                    viewModel.addUserDefinedGameProfile(profileToSave)
                } else {
                    viewModel.updateGameProfile(profileToSave)
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun UserGameProfileDialog(
    initialProfile: GameProfileEntity?,
    onDismiss: () -> Unit,
    onSave: (GameProfileEntity) -> Unit
) {
    var appName by remember { mutableStateOf(initialProfile?.appName ?: "") }
    var packageName by remember { mutableStateOf(initialProfile?.packageName ?: "") }
    var sensiX by remember { mutableFloatStateOf(initialProfile?.customSensitivityX ?: 1.20f) }
    var sensiY by remember { mutableFloatStateOf(initialProfile?.customSensitivityY ?: 1.50f) }
    var touchAccel by remember { mutableStateOf(initialProfile?.touchAcceleration ?: false) }
    var zone by remember { mutableStateOf(initialProfile?.sensitivityZone ?: "Semua") }
    var performanceTier by remember { mutableStateOf(initialProfile?.performanceTier ?: "PERFORMANCE") }
    var targetFps by remember { mutableIntStateOf(initialProfile?.targetFps ?: 60) }
    var notes by remember { mutableStateOf(initialProfile?.notes ?: "") }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialProfile == null || initialProfile.id == 0L) "New Game Profile" else "Edit Game Profile",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "Game / App Name",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    placeholder = { Text("e.g. Free Fire MAX Pro", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("input_game_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = GamingBorderGlow,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Package Name (ID)",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    placeholder = { Text("e.g. com.dts.freefiremax", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("input_package_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = GamingBorderGlow,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Sensitivity X Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Custom Sensitivity X:", color = TextPrimary, fontSize = 12.sp)
                    Text(text = String.format("%.2f", sensiX), color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Slider(
                    value = sensiX,
                    onValueChange = { sensiX = it },
                    valueRange = 1.0f..5.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonGreen,
                        activeTrackColor = NeonGreen,
                        inactiveTrackColor = GamingSurfaceVariant
                    )
                )

                // Custom Sensitivity Y Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Custom Sensitivity Y (Vertical):", color = TextPrimary, fontSize = 12.sp)
                    Text(text = String.format("%.2f", sensiY), color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Slider(
                    value = sensiY,
                    onValueChange = { sensiY = it },
                    valueRange = 1.0f..5.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonGreen,
                        activeTrackColor = NeonGreen,
                        inactiveTrackColor = GamingSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Touch Acceleration Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Touch Acceleration", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Dynamic curve swipe multiplier", color = TextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = touchAccel,
                        onCheckedChange = { touchAccel = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = GamingSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sensi Zone
                Text(text = "Sensitivity Zone", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GamingSurfaceVariant)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Kiri", "Semua", "Kanan").forEach { z ->
                        val isSel = zone.equals(z, ignoreCase = true)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) NeonGreen else Color.Transparent)
                                .clickable { zone = z }
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = z,
                                color = if (isSel) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Performance Tier Selection
                Text(text = "Performance Mode", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GamingSurfaceVariant)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("BALANCED", "PERFORMANCE", "BATTERY_SAVER").forEach { t ->
                        val isSel = performanceTier.equals(t, ignoreCase = true)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) NeonGreen else Color.Transparent)
                                .clickable { performanceTier = t }
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = when (t) {
                                    "PERFORMANCE" -> "BOOST"
                                    "BATTERY_SAVER" -> "ECO"
                                    else -> "BALANCED"
                                },
                                color = if (isSel) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target FPS
                Text(text = "Target Frame Rate", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GamingSurfaceVariant)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(60, 90, 120).forEach { fps ->
                        val isSel = targetFps == fps
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) NeonGreen else Color.Transparent)
                                .clickable { targetFps = fps }
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = "$fps FPS",
                                color = if (isSel) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Custom Notes / Config Tag", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("e.g. Headshot aim drag configuration", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
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
                    val finalName = appName.ifBlank { "Custom Game" }
                    val finalPkg = packageName.ifBlank { "com.custom.game" }
                    val entity = (initialProfile ?: GameProfileEntity(
                        appName = finalName,
                        packageName = finalPkg
                    )).copy(
                        appName = finalName,
                        packageName = finalPkg,
                        customSensitivityX = sensiX,
                        customSensitivityY = sensiY,
                        touchAcceleration = touchAccel,
                        sensitivityZone = zone,
                        performanceTier = performanceTier,
                        targetFps = targetFps,
                        notes = notes,
                        isUserDefined = true,
                        lastLaunchedTimestamp = System.currentTimeMillis()
                    )
                    onSave(entity)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                modifier = Modifier.testTag("save_game_profile_confirm_button")
            ) {
                Text(text = "SAVE TO DATABASE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GamingSurfaceVariant)
            ) {
                Text(text = "CANCEL", color = TextPrimary)
            }
        },
        containerColor = GamingPanelBg
    )
}
