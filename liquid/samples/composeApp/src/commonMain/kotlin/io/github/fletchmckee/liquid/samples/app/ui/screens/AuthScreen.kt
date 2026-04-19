// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.core.rememberViewModel
import io.github.fletchmckee.liquid.samples.app.presentation.auth.AuthEvent
import io.github.fletchmckee.liquid.samples.app.presentation.auth.AuthViewModel
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.theme.assistantFontFamily
import io.github.fletchmckee.liquid.samples.app.theme.montserratFontFamily
import androidx.compose.ui.text.font.FontFamily
import io.github.fletchmckee.liquid.samples.app.ui.icons.CustomIcons
import io.github.fletchmckee.liquid.samples.app.ui.icons.Visibility
import io.github.fletchmckee.liquid.samples.app.ui.icons.VisibilityOff
import liquid_root.samples.composeapp.generated.resources.Res
import liquid_root.samples.composeapp.generated.resources.klik_logo
import liquid_root.samples.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = rememberViewModel { AuthViewModel() },
    onAuthSuccess: () -> Unit
) {
    val liquidState = rememberLiquidState()
    val glassSettings = LocalLiquidGlassSettings.current
    val uiState by viewModel.state.collectAsState()
    val events by viewModel.events.collectAsState()
    val focusManager = LocalFocusManager.current

    var passwordVisible by remember { mutableStateOf(false) }
    var ageConfirmed by remember { mutableStateOf(false) }

    // Handle auth events
    LaunchedEffect(events) {
        when (events) {
            is AuthEvent.LoginSuccess, is AuthEvent.SignupSuccess -> {
                onAuthSuccess()
                viewModel.consumeEvent()
            }
            is AuthEvent.ShowError -> {
                viewModel.consumeEvent()
            }
            else -> {}
        }
    }

    // Breathing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "background_anim")
    
    // Breathing Radius for Gradient
    val breatheRadius by infiniteTransition.animateFloat(
        initialValue = 1000f,
        targetValue = 1600f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_radius"
    )

    // Moving Orbs Animation
    val orb1OffsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1_x"
    )
    val orb1OffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1_y"
    )

     // Dynamic gradient colors
    val bgColors = listOf(
        Color(0xFFE0F7FA), // Light Cyan
        Color(0xFFE1F5FE), // Light Blue
        Color(0xFFF3E5F5), // Light Purple
    )

    // Radial Gradient that breathes
    val brush = Brush.radialGradient(
        colors = bgColors,
        center = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f), // Top right corner-ish
        radius = breatheRadius
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
            .imePadding()
    ) {
        // Active Moving Orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = 350f,
                center = androidx.compose.ui.geometry.Offset(
                    size.width * 0.8f + orb1OffsetX, 
                    size.height * 0.2f + orb1OffsetY
                )
            )
            drawCircle(
                color = KlikPrimary.copy(alpha = 0.08f),
                radius = 500f,
                center = androidx.compose.ui.geometry.Offset(
                    size.width * 0.1f - orb1OffsetX, 
                    size.height * 0.8f - orb1OffsetY
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .windowInsetsPadding(WindowInsets.ime),
            horizontalAlignment = Alignment.Start, // Left align content
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(140.dp))
            
            // Removed App Logo as per user request

            // New Slogans - Clean & Free
            Text(
                text = stringResource(Res.string.auth_slogan_line1),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = montserratFontFamily()
                ),
                fontWeight = FontWeight.Normal, // No Bold
                color = KlikBlack,
                textAlign = TextAlign.Start
            )

            Text(
                text = stringResource(Res.string.auth_slogan_line2),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Serif
                ),
                fontWeight = FontWeight.Normal, // No Bold
                color = KlikBlack.copy(alpha = 0.8f),
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            if (uiState.isForgotPasswordMode) {
                // Forgot Password Form
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(Res.string.auth_reset_password),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = montserratFontFamily()
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = KlikBlack
                    )

                    if (uiState.passwordResetSent) {
                        // Success state
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFE8F5E9).copy(alpha = 0.8f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.auth_reset_password_sent, uiState.forgotPasswordEmail),
                                color = Color(0xFF2E7D32),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = montserratFontFamily()
                                ),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(Res.string.auth_reset_password_instructions),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = montserratFontFamily()
                            ),
                            color = KlikBlack.copy(alpha = 0.6f)
                        )

                        GlassyTextField(
                            value = uiState.forgotPasswordEmail,
                            onValueChange = { viewModel.updateForgotPasswordEmail(it) },
                            label = stringResource(Res.string.auth_email_address),
                            leadingIcon = Icons.Filled.Email,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.requestPasswordReset()
                                }
                            )
                        )
                    }

                    // Error message
                    AnimatedVisibility(
                        visible = uiState.error != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFFFEBEE).copy(alpha = 0.8f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.error ?: "",
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = montserratFontFamily()
                                ),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (!uiState.passwordResetSent) {
                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = KlikPrimary)
                                .background(
                                    if (uiState.isLoading) KlikPrimary.copy(alpha = 0.5f) else KlikPrimary, 
                                    RoundedCornerShape(16.dp)
                                )
                                .liquid(liquidState) {
                                    edge = 0.1f
                                    shape = RoundedCornerShape(16.dp)
                                    frost = 2.dp
                                    curve = 0.3f
                                    refraction = 0.4f
                                    tint = Color.White.copy(alpha = 0.1f)
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !uiState.isLoading) {
                                    focusManager.clearFocus()
                                    viewModel.requestPasswordReset()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = stringResource(Res.string.auth_send_reset_link),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = montserratFontFamily()
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Back to login
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { viewModel.exitForgotPasswordMode() }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.auth_back_to),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = montserratFontFamily()
                            ),
                            color = KlikBlack.copy(alpha = 0.7f)
                        )
                        Text(
                            text = stringResource(Res.string.auth_sign_in),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = montserratFontFamily()
                            ),
                            fontWeight = FontWeight.Bold,
                            color = KlikPrimary
                        )
                    }
                }
            } else {
                // Normal Login/Signup Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Name field (only for signup)
                    AnimatedVisibility(
                        visible = !uiState.isLoginMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        GlassyTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = stringResource(Res.string.auth_full_name),
                            leadingIcon = Icons.Filled.Person,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                    }

                    // Login: Email or Username field
                    // Signup: Email field only
                    if (uiState.isLoginMode) {
                        GlassyTextField(
                            value = uiState.identifier,
                            onValueChange = { viewModel.updateIdentifier(it) },
                            label = stringResource(Res.string.auth_email_or_username),
                            leadingIcon = Icons.Filled.Person,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                    } else {
                        GlassyTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = stringResource(Res.string.auth_email_address),
                            leadingIcon = Icons.Filled.Email,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                    }

                    // Password field
                    GlassyTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = stringResource(Res.string.auth_password),
                        leadingIcon = Icons.Filled.Lock,
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        CustomIcons.VisibilityOff
                                    else
                                        CustomIcons.Visibility,
                                    contentDescription = if (passwordVisible) stringResource(Res.string.auth_hide_password) else stringResource(Res.string.auth_show_password),
                                    tint = KlikBlack.copy(alpha = 0.5f)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (uiState.isLoginMode) viewModel.login() else viewModel.signup()
                            }
                        )
                    )

                    // Forgot Password link (login mode only)
                    AnimatedVisibility(
                        visible = uiState.isLoginMode,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = stringResource(Res.string.auth_forgot_password),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = montserratFontFamily()
                            ),
                            fontWeight = FontWeight.SemiBold,
                            color = KlikPrimary,
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { viewModel.enterForgotPasswordMode() }
                                .padding(vertical = 4.dp)
                        )
                    }

                    // Age confirmation checkbox (signup only)
                    AnimatedVisibility(
                        visible = !uiState.isLoginMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { ageConfirmed = !ageConfirmed }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = ageConfirmed,
                                onCheckedChange = { ageConfirmed = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = KlikPrimary,
                                    uncheckedColor = KlikBlack.copy(alpha = 0.4f)
                                )
                            )
                            Text(
                                text = stringResource(Res.string.auth_age_confirmation),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = montserratFontFamily()
                                ),
                                color = KlikBlack.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Error message
                    AnimatedVisibility(
                        visible = uiState.error != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFFFEBEE).copy(alpha = 0.8f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.error ?: "",
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = montserratFontFamily()
                                ),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Submit button with LiquidGlass
                    val canSubmit = uiState.isLoginMode || ageConfirmed
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = KlikPrimary)
                            .background(
                                if (uiState.isLoading || !canSubmit) KlikPrimary.copy(alpha = 0.5f) else KlikPrimary,
                                RoundedCornerShape(16.dp)
                            )
                            .liquid(liquidState) {
                                edge = 0.1f
                                shape = RoundedCornerShape(16.dp)
                                frost = 2.dp
                                curve = 0.3f
                                refraction = 0.4f
                                tint = Color.White.copy(alpha = 0.1f)
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = !uiState.isLoading && canSubmit) {
                                focusManager.clearFocus()
                                if (uiState.isLoginMode) viewModel.login() else viewModel.signup()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (uiState.isLoginMode) stringResource(Res.string.auth_sign_in) else stringResource(Res.string.auth_create_account),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = montserratFontFamily()
                                ),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Sign In with Apple (only shown on iOS)
                    if (viewModel.isAppleSignInSupported()) {
                        Spacer(Modifier.height(24.dp))

                        // Divider with "or" text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(KlikBlack.copy(alpha = 0.2f))
                            )
                            Text(
                                text = stringResource(Res.string.auth_or_continue_with),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = montserratFontFamily()
                                ),
                                color = KlikBlack.copy(alpha = 0.5f)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(KlikBlack.copy(alpha = 0.2f))
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Sign In with Apple button (following Apple HIG)
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.loginWithApple()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White,
                                disabledContainerColor = Color.Black.copy(alpha = 0.5f)
                            ),
                            enabled = !uiState.isLoading
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Apple logo (using text as icon, can be replaced with actual asset)
                                Text(
                                    text = "\uF8FF", // Apple logo Unicode (may need fallback)
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = if (uiState.isLoginMode) stringResource(Res.string.auth_sign_in_with_apple) else stringResource(Res.string.auth_sign_up_with_apple),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = montserratFontFamily()
                                    ),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Toggle login/signup (hidden in forgot password mode)
            if (!uiState.isForgotPasswordMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { viewModel.toggleMode() }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Start, // Left align
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isLoginMode)
                            stringResource(Res.string.auth_new_to_klik)
                        else
                            stringResource(Res.string.auth_already_have_account),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = montserratFontFamily()
                        ),
                        color = KlikBlack.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (uiState.isLoginMode) stringResource(Res.string.auth_sign_up) else stringResource(Res.string.auth_sign_in),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = montserratFontFamily()
                        ),
                        fontWeight = FontWeight.Bold,
                        color = KlikPrimary
                    )
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun GlassyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = montserratFontFamily()
                )
            )
        },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = if (isFocused) KlikPrimary else KlikBlack.copy(alpha = 0.4f)
            )
        },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = KlikPrimary,
            unfocusedBorderColor = Color.Black.copy(alpha = 0.1f),
            focusedContainerColor = Color.White.copy(alpha = 0.5f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.3f),
            focusedLabelColor = KlikPrimary,
            unfocusedLabelColor = KlikBlack.copy(alpha = 0.5f),
            focusedTextColor = Color.Black,
            unfocusedTextColor = KlikBlack,
            cursorColor = KlikPrimary
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = montserratFontFamily()
        )
    )
}
