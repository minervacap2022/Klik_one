package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import io.github.fletchmckee.liquid.samples.app.di.BuildConfig
import io.github.fletchmckee.liquid.samples.app.ui.components.AvatarImage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.core.rememberViewModel
import io.github.fletchmckee.liquid.samples.app.domain.entity.DeviceType
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationInfo
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationProviders
import io.github.fletchmckee.liquid.samples.app.domain.entity.LiquidGlassPreferences
import io.github.fletchmckee.liquid.samples.app.presentation.profile.ProfileEvent
import io.github.fletchmckee.liquid.samples.app.presentation.profile.ProfileViewModel
import io.github.fletchmckee.liquid.samples.app.theme.BackgroundOption
import io.github.fletchmckee.liquid.samples.app.theme.BackgroundOptions
import io.github.fletchmckee.liquid.samples.app.theme.FontCollection
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.samples.app.theme.LocalBackgroundColor
import io.github.fletchmckee.liquid.samples.app.theme.LocalFontIndex
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.theme.LocalSetFontIndex
import io.github.fletchmckee.liquid.samples.app.theme.LocalSetLiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.theme.LocalFontSizeScale
import io.github.fletchmckee.liquid.samples.app.theme.LocalSetFontSizeScale
import io.github.fletchmckee.liquid.samples.app.theme.LocalLetterSpacingScale
import io.github.fletchmckee.liquid.samples.app.theme.LocalSetLetterSpacingScale
import io.github.fletchmckee.liquid.samples.app.theme.LocalLineHeightScale
import io.github.fletchmckee.liquid.samples.app.theme.LocalSetLineHeightScale
import io.github.fletchmckee.liquid.samples.app.theme.getFontCollections
import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.domain.entity.SUPPORTED_LANGUAGES
import io.github.fletchmckee.liquid.samples.app.domain.entity.findLanguageByCode
import io.github.fletchmckee.liquid.samples.app.platform.OAuthBrowser
import io.github.fletchmckee.liquid.samples.app.ui.icons.Archive
import io.github.fletchmckee.liquid.samples.app.ui.icons.CustomIcons
import liquid_root.samples.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = rememberViewModel { ProfileViewModel() },
    onNavigateToArchived: () -> Unit = {},
    subscription: io.github.fletchmckee.liquid.samples.app.domain.entity.Subscription? = null,
    onNavigateToPricing: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToAccountSecurity: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit = {}
) {
    val liquidState = rememberLiquidState()
    val uiState by viewModel.state.collectAsState()
    val events by viewModel.events.collectAsState()

    // Developer Mode State
    var showDevSettings by remember { mutableStateOf(false) }

    // Handle events
    LaunchedEffect(events) {
        events?.let { event ->
            when (event) {
                is ProfileEvent.ShowError -> { /* Handle error */ }
                is ProfileEvent.ShowSuccess -> { /* Handle success */ }
                is ProfileEvent.PreferencesSaved -> { /* Handle saved */ }
                is ProfileEvent.LoggedOut -> { /* Handle logout */ }
                is ProfileEvent.DeviceConnected -> { /* Handle device */ }
                is ProfileEvent.DeviceDisconnected -> { /* Handle device */ }
                is ProfileEvent.BackgroundChanged -> { /* Handle bg */ }
                is ProfileEvent.FontChanged -> { /* Handle font */ }
                is ProfileEvent.DefaultBackgroundSet -> { /* Handle default bg */ }
                is ProfileEvent.DefaultFontSet -> { /* Handle default font */ }
                is ProfileEvent.IntegrationAuthStarted -> { /* OAuth flow started */ }
                is ProfileEvent.IntegrationConnected -> { /* Integration connected */ }
                is ProfileEvent.IntegrationDisconnected -> { /* Integration disconnected */ }
            }
            viewModel.consumeEvent()
        }
    }

    // Main Container
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = KlikPrimary)
            return@Box
        }

        uiState.error?.let { error ->
            Text(error, Modifier.align(Alignment.Center), color = Color.Red)
            return@Box
        }

        val user = uiState.user ?: return@Box

        // Dialogs
        if (uiState.showEditProfile) {
            EditProfileDialog(uiState, viewModel)
        }
        if (uiState.showDeleteAccountConfirmation) {
            DeleteAccountDialog(uiState, viewModel)
        }
        if (uiState.showLogoutConfirmation) {
            LogoutDialog(uiState, viewModel)
        }

        if (showDevSettings) {
            DeveloperControlPanel(liquidState, uiState, viewModel) { showDevSettings = false }
        } else {
            // New PREMIUM "Profile" Layout
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 80.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Centered Header
                item {
                    ProfileHeader(
                        liquidState = liquidState,
                        user = user,
                        onEdit = { viewModel.showEditProfile() }
                    )
                }

                // 2. My Plan (Featured Card)
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SectionHeader(stringResource(Res.string.profile_my_plan))
                        YourPlanCard(liquidState, subscription, onNavigateToPricing)
                    }
                }

                // 3. Integrations (Horizontal Scroll)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(Modifier.padding(horizontal = 24.dp)) {
                            SectionHeader(stringResource(Res.string.profile_integrations))
                        }
                        
                        // Scrollable Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp), // Content padding for scroll
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (uiState.integrations.isNotEmpty()) {
                                uiState.integrations.forEach { integration ->
                                    IntegrationCard(
                                        liquidState = liquidState,
                                        integration = integration,
                                        onClick = {
                                            if (integration.connected) viewModel.disconnectIntegration(integration.providerId)
                                            else viewModel.authorizeIntegration(integration.providerId)
                                        }
                                    )
                                }
                            } else {
                                Text("Loading integrations...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }

                // 4. App Settings (Vertical List)
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionHeader(stringResource(Res.string.profile_app_settings))

                        SettingsItem(
                            liquidState = liquidState,
                            title = stringResource(Res.string.profile_privacy_data),
                            icon = Icons.Filled.Lock,
                            onClick = onNavigateToPrivacy
                        )
                        SettingsItem(liquidState, stringResource(Res.string.profile_account_security), Icons.Filled.Lock, onClick = onNavigateToAccountSecurity)
                        SettingsItem(liquidState, stringResource(Res.string.profile_notifications), Icons.Filled.Notifications, onClick = onNavigateToNotificationSettings)
                        LanguageSelectorItem(
                            liquidState = liquidState,
                            currentLanguageCode = uiState.preferences?.language ?: "en",
                            onLanguageSelected = { code -> viewModel.selectLanguage(code) }
                        )
                        SettingsItem(liquidState, stringResource(Res.string.profile_billing_payment), Icons.Filled.Star, onClick = onNavigateToPricing)
                        SettingsItem(
                            liquidState = liquidState,
                            title = if (uiState.emailVerificationSent) stringResource(Res.string.profile_verification_sent) else stringResource(Res.string.profile_verify_email),
                            icon = Icons.Filled.Email,
                            onClick = { viewModel.requestEmailVerification() }
                        )
                    }
                }

                // 5. Support & Legal
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionHeader(stringResource(Res.string.profile_support_legal))

                        SettingsItem(
                            liquidState = liquidState,
                            title = stringResource(Res.string.profile_terms_of_service),
                            icon = Icons.Filled.Person,
                            onClick = { OAuthBrowser.openUrl(ApiConfig.TERMS_URL) }
                        )
                        SettingsItem(
                            liquidState = liquidState,
                            title = stringResource(Res.string.profile_privacy_policy),
                            icon = Icons.Filled.Lock,
                            onClick = { OAuthBrowser.openUrl(ApiConfig.PRIVACY_URL) }
                        )
                        SettingsItem(
                            liquidState = liquidState,
                            title = stringResource(Res.string.profile_developer_options),
                            icon = Icons.Outlined.Settings,
                            onClick = { showDevSettings = true }
                        )
                        
                        ArchivedItemButton(liquidState, onNavigateToArchived)
                    }
                }

                // 6. Footer Actions
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LogoutButton(liquidState) { viewModel.showLogoutConfirmation() }
                        
                        Text(
                            text = stringResource(Res.string.profile_delete_account),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFE53935).copy(alpha = 0.6f),
                            modifier = Modifier.clickable { viewModel.showDeleteAccountConfirmation() }.padding(8.dp)
                        )
                        
                        Text(
                             stringResource(Res.string.profile_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                             style = MaterialTheme.typography.labelSmall,
                             color = KlikBlack.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

// --- Sub-components for Cleaner Code ---

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = KlikBlack.copy(alpha = 0.9f),
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
fun ProfileHeader(liquidState: LiquidState, user: io.github.fletchmckee.liquid.samples.app.domain.entity.User, onEdit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp) // Reduced from 16.dp
    ) {
        // Large Centered Avatar
        Box(
            modifier = Modifier
                .size(80.dp) // Reduced from 100.dp for better density
                .liquid(liquidState) {
                    edge = 0.02f
                    shape = CircleShape
                    tint = Color.White.copy(alpha = 0.5f)
                }
                .border(2.dp, Color.White, CircleShape)
                .clickable { onEdit() }, // Edit interaction on avatar
            contentAlignment = Alignment.Center
        ) {
            AvatarImage(
                avatarUrl = user.displayAvatarUrl,
                initials = user.initials,
                size = 80.dp,
                initialsStyle = MaterialTheme.typography.displaySmall
            )
            // Edit Badge
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp)
                    .size(28.dp).background(KlikPrimary, CircleShape).border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }

        // Name & Welcome
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = KlikBlack
            )
            Text(
                text = stringResource(Res.string.profile_welcome_back, user.name.split(" ").firstOrNull() ?: ""),
                style = MaterialTheme.typography.bodyLarge,
                color = KlikBlack.copy(alpha = 0.5f)
            )
        }
    }
}


@Composable
fun IntegrationCard(
    liquidState: LiquidState,
    name: String,
    isConnected: Boolean,
    onClick: (() -> Unit)? = null
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .width(100.dp)
            .height(110.dp)
            .background(
                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                cardShape
            )
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = if (isConnected) KlikPrimary.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.2f)
            }
            .clip(cardShape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
         Box(
            modifier = Modifier
                .size(40.dp)
                .background(KlikBlack.copy(alpha = 0.05f), CircleShape),
             contentAlignment = Alignment.Center
        ) {
             Text(
                text = name.take(1),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Column {
             Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
             Text(
                text = if (isConnected) stringResource(Res.string.active) else stringResource(Res.string.connect),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isConnected) KlikPrimary else Color.Gray
            )
        }
    }
}

/**
 * IntegrationCard with IntegrationInfo and click handler for dynamic integrations
 */
@Composable
fun IntegrationCard(
    liquidState: LiquidState,
    integration: IntegrationInfo,
    onClick: () -> Unit
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)
    val isConnected = integration.connected

    Column(
        modifier = Modifier
            .width(110.dp)
            .background(
                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                cardShape
            )
            .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = if (isConnected) KlikPrimary.copy(alpha = 0.05f) else Color.Transparent
            }
            .clip(cardShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isConnected) KlikPrimary.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
             Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = integration.displayName,
                tint = if (isConnected) KlikPrimary else Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }

        // Info
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = integration.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            // Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .background(
                        if (isConnected) KlikPrimary.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.08f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                if (isConnected) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = KlikPrimary,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Text(
                    text = if (isConnected) stringResource(Res.string.active) else stringResource(Res.string.connect),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isConnected) KlikPrimary else Color.Gray
                )
            }
        }
    }
}

@Composable
fun SettingsItem(liquidState: LiquidState, title: String, icon: ImageVector, onClick: (() -> Unit)? = null) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)
    
    // Standard "Flat Card" Style
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                cardShape
            )
            .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = Color.Transparent
            }
            .clip(cardShape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(icon, contentDescription = null, tint = KlikBlack.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
             Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge, // Reduced from titleMedium for better density
                fontWeight = FontWeight.Medium,
                color = KlikBlack
            )
        }
        Icon(Icons.Filled.KeyboardArrowRight, null, tint = KlikBlack.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeveloperControlPanel(
    liquidState: LiquidState,
    uiState: io.github.fletchmckee.liquid.samples.app.presentation.profile.ProfileUiState,
    viewModel: ProfileViewModel,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.95f)) // Solid background for clarity
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .padding(top = 48.dp), // Safe area
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Dev Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.dev_settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(Res.string.done),
                style = MaterialTheme.typography.titleMedium.copy(color = KlikPrimary),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onClose() }
            )
        }
        
         // Background Selection (Colors + Images)
        Column {
            Text(stringResource(Res.string.dev_background), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(
                stringResource(Res.string.dev_double_click_default),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
        val setBackgroundOption = LocalBackgroundColor.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BackgroundOptions.forEachIndexed { index, option ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(56.dp)
                ) {
                    when (option) {
                        is BackgroundOption.ColorBackground -> {
                            // Color circle
                            val isDefault = uiState.defaultBackgroundIndex == index
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(option.color)
                                        .combinedClickable(
                                            onClick = {
                                                setBackgroundOption?.invoke(option)
                                                viewModel.selectBackground(index)
                                            },
                                            onDoubleClick = {
                                                setBackgroundOption?.invoke(option)
                                                viewModel.setDefaultBackground(index)
                                            }
                                        )
                                        .border(
                                            width = if (uiState.selectedBackgroundIndex == index) 2.dp else 1.dp,
                                            color = if (uiState.selectedBackgroundIndex == index) KlikPrimary else Color.Gray.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                )
                                // Default indicator
                                if (isDefault) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(16.dp)
                                            .background(KlikPrimary, CircleShape)
                                            .border(1.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = stringResource(Res.string.default_label),
                                            modifier = Modifier.size(10.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        is BackgroundOption.GradientBackground -> {
                            // Gradient circle
                            val isDefault = uiState.defaultBackgroundIndex == index
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(option.brush)
                                        .combinedClickable(
                                            onClick = {
                                                setBackgroundOption?.invoke(option)
                                                viewModel.selectBackground(index)
                                            },
                                            onDoubleClick = {
                                                setBackgroundOption?.invoke(option)
                                                viewModel.setDefaultBackground(index)
                                            }
                                        )
                                        .border(
                                            width = if (uiState.selectedBackgroundIndex == index) 2.dp else 1.dp,
                                            color = if (uiState.selectedBackgroundIndex == index) KlikPrimary else Color.Gray.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                )
                                // Default indicator
                                if (isDefault) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(16.dp)
                                            .background(KlikPrimary, CircleShape)
                                            .border(1.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = stringResource(Res.string.default_label),
                                            modifier = Modifier.size(10.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        is BackgroundOption.ImageBackground -> {
                            // Image preview
                            val painter = when (option.resourceName) {
                                "gradient_peach.png" -> painterResource(Res.drawable.gradient_peach)
                                "gradient_lavender.png" -> painterResource(Res.drawable.gradient_lavender)
                                else -> painterResource(Res.drawable.moon_and_stars)
                            }
                            val isDefault = uiState.defaultBackgroundIndex == index
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .combinedClickable(
                                            onClick = {
                                                setBackgroundOption?.invoke(option)
                                                viewModel.selectBackground(index)
                                            },
                                            onDoubleClick = {
                                                setBackgroundOption?.invoke(option)
                                                viewModel.setDefaultBackground(index)
                                            }
                                        )
                                        .border(
                                            width = if (uiState.selectedBackgroundIndex == index) 2.dp else 1.dp,
                                            color = if (uiState.selectedBackgroundIndex == index) KlikPrimary else Color.Gray.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Image(
                                        painter = painter,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                // Default indicator
                                if (isDefault) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(16.dp)
                                            .background(KlikPrimary, CircleShape)
                                            .border(1.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = stringResource(Res.string.default_label),
                                            modifier = Modifier.size(10.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // Color name below the circle
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Font Selection
        Column {
            Text(stringResource(Res.string.dev_typography), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(Res.string.dev_double_click_default),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
        val currentFontIndex = LocalFontIndex.current
        val setFontIndex = LocalSetFontIndex.current
        val fontCollections = getFontCollections()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fontCollections.forEachIndexed { index, fontCollection ->
                    FontCard(
                        liquidState = liquidState,
                        fontCollection = fontCollection,
                        isSelected = currentFontIndex == index,
                        isDefault = uiState.defaultFontIndex == index,
                        onClick = {
                            setFontIndex?.invoke(index)
                            viewModel.selectFont(index)
                        },
                        onDoubleClick = {
                            setFontIndex?.invoke(index)
                            viewModel.setDefaultFont(index)
                        }
                    )
                }
            }
        }

        // Typography Adjustments
        Text(stringResource(Res.string.dev_typography_adjustments), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        val fontSizeScale = LocalFontSizeScale.current
        val setFontSizeScale = LocalSetFontSizeScale.current
        val letterSpacingScale = LocalLetterSpacingScale.current
        val setLetterSpacingScale = LocalSetLetterSpacingScale.current
        val lineHeightScale = LocalLineHeightScale.current
        val setLineHeightScale = LocalSetLineHeightScale.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Font Size Slider
            GlassSlider(
                label = stringResource(Res.string.dev_font_size_scale),
                value = fontSizeScale,
                valueRange = 0.5f..1.5f,
                displayValue = "${(fontSizeScale * 100).toInt()}%",
                onValueChange = { setFontSizeScale?.invoke(it) }
            )

            // Letter Spacing Slider
            GlassSlider(
                label = stringResource(Res.string.dev_letter_spacing_scale),
                value = letterSpacingScale,
                valueRange = 0.5f..2.0f,
                displayValue = "${(letterSpacingScale * 100).toInt()}%",
                onValueChange = { setLetterSpacingScale?.invoke(it) }
            )

            // Line Height Slider
            GlassSlider(
                label = stringResource(Res.string.dev_line_height_scale),
                value = lineHeightScale,
                valueRange = 0.8f..2.0f,
                displayValue = "${(lineHeightScale * 100).toInt()}%",
                onValueChange = { setLineHeightScale?.invoke(it) }
            )
        }

        // Liquid Glass Settings
        Text(stringResource(Res.string.dev_liquid_glass_engine), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        val glassSettings = LocalLiquidGlassSettings.current
        val setGlassSettings = LocalSetLiquidGlassSettings.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), RoundedCornerShape(24.dp))
                .liquid(liquidState) {
                    edge = 0.01f
                    shape = RoundedCornerShape(24.dp)
                    tint = Color.Transparent
                }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Transparency Slider
            GlassSlider(
                label = stringResource(Res.string.dev_transparency),
                value = glassSettings.transparency,
                valueRange = 0f..1f,
                displayValue = "${(glassSettings.transparency * 100).toInt()}%",
                onValueChange = { newValue ->
                    setGlassSettings?.invoke(glassSettings.copy(transparency = newValue))
                    viewModel.updateLiquidGlassPreferences(
                        LiquidGlassPreferences(
                            transparency = newValue,
                            frost = glassSettings.frost.value,
                            refraction = glassSettings.refraction,
                            curve = glassSettings.curve,
                            edge = glassSettings.edge,
                            applyToCards = glassSettings.applyToCards
                        )
                    )
                }
            )

            // Frost Slider
            GlassSlider(
                label = stringResource(Res.string.dev_frost),
                value = glassSettings.frost.value,
                valueRange = 0f..40f,
                displayValue = "${glassSettings.frost.value.toInt()}dp",
                onValueChange = { newValue ->
                    setGlassSettings?.invoke(glassSettings.copy(frost = newValue.dp))
                    viewModel.updateLiquidGlassPreferences(
                        LiquidGlassPreferences(
                            transparency = glassSettings.transparency,
                            frost = newValue,
                            refraction = glassSettings.refraction,
                            curve = glassSettings.curve,
                            edge = glassSettings.edge,
                            applyToCards = glassSettings.applyToCards
                        )
                    )
                }
            )

            // Refraction Slider
            GlassSlider(
                label = stringResource(Res.string.dev_refraction),
                value = glassSettings.refraction,
                valueRange = 0f..1f,
                displayValue = "${(glassSettings.refraction * 100).toInt()}%",
                onValueChange = { newValue ->
                    setGlassSettings?.invoke(glassSettings.copy(refraction = newValue))
                    viewModel.updateLiquidGlassPreferences(
                        LiquidGlassPreferences(
                            transparency = glassSettings.transparency,
                            frost = glassSettings.frost.value,
                            refraction = newValue,
                            curve = glassSettings.curve,
                            edge = glassSettings.edge,
                            applyToCards = glassSettings.applyToCards
                        )
                    )
                }
            )

            // Curve Slider
            GlassSlider(
                label = stringResource(Res.string.dev_curve),
                value = glassSettings.curve,
                valueRange = 0f..1f,
                displayValue = "${(glassSettings.curve * 100).toInt()}%",
                onValueChange = { newValue ->
                    setGlassSettings?.invoke(glassSettings.copy(curve = newValue))
                    viewModel.updateLiquidGlassPreferences(
                        LiquidGlassPreferences(
                            transparency = glassSettings.transparency,
                            frost = glassSettings.frost.value,
                            refraction = glassSettings.refraction,
                            curve = newValue,
                            edge = glassSettings.edge,
                            applyToCards = glassSettings.applyToCards
                        )
                    )
                }
            )

            // Edge Slider
            GlassSlider(
                label = stringResource(Res.string.dev_edge),
                value = glassSettings.edge,
                valueRange = 0f..0.1f,
                displayValue = "${(glassSettings.edge * 1000).toInt() / 1000f}",
                onValueChange = { newValue ->
                    setGlassSettings?.invoke(glassSettings.copy(edge = newValue))
                    viewModel.updateLiquidGlassPreferences(
                        LiquidGlassPreferences(
                            transparency = glassSettings.transparency,
                            frost = glassSettings.frost.value,
                            refraction = glassSettings.refraction,
                            curve = glassSettings.curve,
                            edge = newValue,
                            applyToCards = glassSettings.applyToCards
                        )
                    )
                }
            )

            Spacer(Modifier.height(8.dp))

            // Apply to Cards Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(Res.string.dev_apply_to_cards), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        stringResource(Res.string.dev_apply_to_cards_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = glassSettings.applyToCards,
                    onCheckedChange = { checked ->
                        setGlassSettings?.invoke(glassSettings.copy(applyToCards = checked))
                        viewModel.updateLiquidGlassPreferences(
                            LiquidGlassPreferences(
                                transparency = glassSettings.transparency,
                                frost = glassSettings.frost.value,
                                refraction = glassSettings.refraction,
                                curve = glassSettings.curve,
                                edge = glassSettings.edge,
                                applyToCards = checked
                            )
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = KlikPrimary,
                        checkedTrackColor = KlikPrimary.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}



@Composable
fun GlassSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(displayValue, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = KlikPrimary,
                activeTrackColor = KlikPrimary,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FontCard(
    liquidState: LiquidState,
    fontCollection: FontCollection,
    isSelected: Boolean,
    isDefault: Boolean = false,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit = {}
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                    cardShape
                )
                .border(
                    BorderStroke(
                        width = if (isSelected) 2.dp else 0.5.dp,
                        color = if (isSelected) KlikPrimary else Color.Black.copy(alpha = 0.12f)
                    ),
                    cardShape
                )
                .liquid(liquidState) {
                    edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                    shape = cardShape
                    if (glassSettings.applyToCards) {
                        tint = Color.Transparent
                    }
                }
                .clip(cardShape)
                .combinedClickable(
                    onClick = onClick,
                    onDoubleClick = onDoubleClick
                )
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Font name preview using the font's own family
            Text(
                text = "Aa",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = fontCollection.fontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isSelected) KlikPrimary else Color.Black
            )
            Text(
                text = fontCollection.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) KlikPrimary else Color.Gray
            )
            Text(
                text = fontCollection.description,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = Color.Gray.copy(alpha = 0.7f),
                maxLines = 2,
                lineHeight = 10.sp
            )
        }
        // Default indicator
        if (isDefault) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(16.dp)
                    .background(KlikPrimary, CircleShape)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = stringResource(Res.string.default_label),
                    modifier = Modifier.size(10.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun LogoutButton(
    liquidState: LiquidState,
    onLogout: () -> Unit
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFEBEE).copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f), cardShape)
            .border(BorderStroke(0.5.dp, Color(0xFFE53935).copy(alpha = 0.3f)), cardShape)
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = Color(0xFFE53935).copy(alpha = 0.05f)
            }
            .clip(cardShape)
            .clickable(onClick = onLogout)
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(Res.string.profile_sign_out),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE53935)
        )
    }
}

@Composable
fun ArchivedItemButton(
    liquidState: LiquidState,
    onClick: () -> Unit
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                cardShape
            )
            .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = Color.Transparent
            }
            .clip(cardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                CustomIcons.Archive,
                contentDescription = null,
                tint = KlikBlack.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = stringResource(Res.string.profile_archived_items),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = KlikBlack
                )
                Text(
                    text = stringResource(Res.string.profile_archived_items_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = KlikBlack.copy(alpha = 0.5f)
                )
            }
        }
        Icon(Icons.Filled.KeyboardArrowRight, null, tint = KlikBlack.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
    }
}

// --- Language Selector ---

@Composable
fun LanguageSelectorItem(
    liquidState: LiquidState,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    val currentLanguage = findLanguageByCode(currentLanguageCode)

    // The row card matching SettingsItem style
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                cardShape
            )
            .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = Color.Transparent
            }
            .clip(cardShape)
            .clickable { showLanguageDialog = true }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Outlined.Settings, contentDescription = null, tint = KlikBlack.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
            Column {
                Text(
                    text = stringResource(Res.string.profile_language),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = KlikBlack
                )
                Text(
                    text = currentLanguage.nativeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = KlikBlack.copy(alpha = 0.5f)
                )
            }
        }
        Icon(Icons.Filled.KeyboardArrowRight, null, tint = KlikBlack.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
    }

    // Language selection dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguageCode = currentLanguageCode,
            onLanguageSelected = { code ->
                onLanguageSelected(code)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    io.github.fletchmckee.liquid.samples.app.ui.components.LiquidGlassDialogScaffold(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.dialog_language),
        confirmText = stringResource(Res.string.done),
        onConfirm = onDismiss,
        dismissText = stringResource(Res.string.cancel)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SUPPORTED_LANGUAGES.forEach { language ->
                val isSelected = language.code == currentLanguageCode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) KlikPrimary.copy(alpha = 0.08f) else Color.Transparent
                        )
                        .clickable { onLanguageSelected(language.code) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = language.nativeName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) KlikPrimary else KlikBlack
                        )
                        if (language.englishName != language.nativeName) {
                            Text(
                                text = language.englishName,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) KlikPrimary.copy(alpha = 0.7f) else KlikBlack.copy(alpha = 0.5f)
                            )
                        }
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = stringResource(Res.string.selected),
                            tint = KlikPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- Helper Dialogs ---

@Composable
fun EditProfileDialog(
    uiState: io.github.fletchmckee.liquid.samples.app.presentation.profile.ProfileUiState,
    viewModel: ProfileViewModel
) {
    io.github.fletchmckee.liquid.samples.app.ui.components.LiquidGlassDialogScaffold(
        onDismissRequest = { viewModel.dismissEditProfile() },
        title = stringResource(Res.string.dialog_edit_profile),
        confirmText = if (uiState.isSavingProfile) stringResource(Res.string.dialog_saving) else stringResource(Res.string.save),
        confirmEnabled = !uiState.isSavingProfile,
        onConfirm = { viewModel.saveProfile() },
        dismissText = stringResource(Res.string.cancel)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = uiState.editName,
                onValueChange = { viewModel.updateEditName(it) },
                label = { Text(stringResource(Res.string.dialog_name)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KlikPrimary,
                    cursorColor = KlikPrimary,
                    focusedLabelColor = KlikPrimary,
                    unfocusedBorderColor = Color.Black.copy(alpha = 0.12f)
                )
            )
            OutlinedTextField(
                value = uiState.editEmail,
                onValueChange = { viewModel.updateEditEmail(it) },
                label = { Text(stringResource(Res.string.dialog_email)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KlikPrimary,
                    cursorColor = KlikPrimary,
                    focusedLabelColor = KlikPrimary,
                    unfocusedBorderColor = Color.Black.copy(alpha = 0.12f)
                )
            )
        }
    }
}

@Composable
fun DeleteAccountDialog(
    uiState: io.github.fletchmckee.liquid.samples.app.presentation.profile.ProfileUiState,
    viewModel: ProfileViewModel
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    io.github.fletchmckee.liquid.samples.app.ui.components.LiquidGlassDialogScaffold(
        onDismissRequest = { viewModel.dismissDeleteAccountConfirmation() },
        title = stringResource(Res.string.dialog_delete_account),
        confirmText = if (uiState.isDeletingAccount) stringResource(Res.string.dialog_deleting) else stringResource(Res.string.delete),
        confirmEnabled = !uiState.isDeletingAccount && password.isNotBlank(),
        isDestructive = true,
        onConfirm = { viewModel.confirmDeleteAccount(password) },
        dismissText = stringResource(Res.string.cancel)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(Res.string.dialog_delete_account_warning),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF555555)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(Res.string.dialog_enter_password)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.Lock else Icons.Filled.Lock,
                            contentDescription = if (passwordVisible) stringResource(Res.string.auth_hide_password) else stringResource(Res.string.auth_show_password)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE53935),
                    cursorColor = Color(0xFFE53935),
                    focusedLabelColor = Color(0xFFE53935),
                    unfocusedBorderColor = Color.Black.copy(alpha = 0.12f)
                )
            )
        }
    }
}

@Composable
fun LogoutDialog(
    uiState: io.github.fletchmckee.liquid.samples.app.presentation.profile.ProfileUiState,
    viewModel: ProfileViewModel
) {
    io.github.fletchmckee.liquid.samples.app.ui.components.LiquidGlassDialog(
        onDismissRequest = { viewModel.dismissLogoutConfirmation() },
        title = stringResource(Res.string.profile_sign_out),
        message = stringResource(Res.string.dialog_sign_out_message),
        confirmText = stringResource(Res.string.profile_sign_out),
        isDestructive = true,
        onConfirm = { viewModel.confirmLogout() },
        dismissText = stringResource(Res.string.cancel)
    )
}

// --- Cards & Items ---

@Composable
fun YourPlanCard(
    liquidState: LiquidState,
    subscription: io.github.fletchmckee.liquid.samples.app.domain.entity.Subscription? = null,
    onViewPlans: () -> Unit = {}
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    // Tier-appropriate accent color
    val tierColor = when (subscription?.planCode?.lowercase()) {
        "pro" -> Color(0xFFFFCC00)    // Gold for Pro
        "basic" -> Color(0xFF4A90D9)  // Blue for Basic
        else -> Color(0xFF8E8E93)     // Gray for Starter
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewPlans)
            .background(tierColor.copy(alpha = 0.1f), cardShape)
            .border(BorderStroke(1.dp, tierColor.copy(alpha = 0.5f)), cardShape)
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.02f
                shape = cardShape
                if (glassSettings.applyToCards) {
                   frost = glassSettings.frost
                   curve = glassSettings.curve
                   refraction = glassSettings.refraction
                }
                tint = tierColor.copy(alpha = 0.1f)
            }
            .clip(cardShape)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = subscription?.displayName ?: stringResource(Res.string.plan_loading),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KlikBlack
                    )
                    Text(
                        text = "${subscription?.billingCycle?.replaceFirstChar { it.uppercase() } ?: ""} · ${subscription?.status?.replaceFirstChar { it.uppercase() } ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = KlikBlack.copy(alpha = 0.7f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(tierColor.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, tierColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Plan",
                        tint = tierColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ASR Usage progress bar
            if (subscription != null && subscription.usage.asrMinutesLimit != 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val isUnlimited = subscription.usage.asrMinutesLimit == -1
                val progress = if (isUnlimited) 0f
                    else subscription.usage.asrMinutesUsed.toFloat() / subscription.usage.asrMinutesLimit.toFloat()

                Text(
                    text = if (isUnlimited) "ASR: ${subscription.usage.asrMinutesUsed} min used · Unlimited"
                        else "ASR: ${subscription.usage.asrMinutesUsed} / ${subscription.usage.asrMinutesLimit} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = KlikBlack.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (!isUnlimited) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = tierColor,
                        trackColor = tierColor.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}
