// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.core.rememberViewModel
import io.github.fletchmckee.liquid.samples.app.presentation.auth.AuthEvent
import io.github.fletchmckee.liquid.samples.app.presentation.auth.AuthViewModel
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard

@Composable
fun K1AuthScreen(
  viewModel: AuthViewModel = rememberViewModel { AuthViewModel() },
  onAuthSuccess: () -> Unit,
) {
  val ui by viewModel.state.collectAsState()
  val events by viewModel.events.collectAsState()

  LaunchedEffect(events) {
    when (events) {
      is AuthEvent.LoginSuccess, is AuthEvent.SignupSuccess -> {
        onAuthSuccess()
        viewModel.consumeEvent()
      }

      is AuthEvent.ShowError -> viewModel.consumeEvent()

      else -> { /* no-op */ }
    }
  }

  Column(
    Modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .verticalScroll(rememberScrollState())
      .statusBarsPadding()
      .imePadding()
      .padding(top = 56.dp, bottom = 32.dp),
  ) {
    // Brand mark + headline
    Column(Modifier.padding(horizontal = 32.dp)) {
      K1WaveformLive(Modifier.size(width = 80.dp, height = 44.dp))
      Spacer(Modifier.height(K1Sp.xxxl))
      K1Eyebrow("Klik", large = true)
      Spacer(Modifier.height(K1Sp.m))
      Text(
        when {
          ui.isForgotPasswordMode -> "Reset your password."
          ui.isLoginMode -> "Welcome back."
          else -> "Create your Klik account."
        },
        style = K1Type.display,
      )
      Spacer(Modifier.height(K1Sp.m))
      Text(
        when {
          ui.isForgotPasswordMode -> "We'll email you a reset link."
          ui.isLoginMode -> "Sign in to pick up where you left off."
          else -> "Klik listens, understands, and quietly handles your work."
        },
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )
    }

    Spacer(Modifier.height(K1Sp.huge))

    Column(Modifier.padding(horizontal = 32.dp)) {
      if (ui.isForgotPasswordMode) {
        ForgotPasswordForm(
          email = ui.forgotPasswordEmail,
          onEmailChange = viewModel::updateForgotPasswordEmail,
          passwordResetSent = ui.passwordResetSent,
          onSubmit = viewModel::requestPasswordReset,
          onCancel = viewModel::exitForgotPasswordMode,
          isLoading = ui.isLoading,
        )
      } else {
        AuthForm(
          isLoginMode = ui.isLoginMode,
          identifier = ui.identifier,
          email = ui.email,
          password = ui.password,
          name = ui.name,
          isLoading = ui.isLoading,
          onIdentifier = viewModel::updateIdentifier,
          onEmail = viewModel::updateEmail,
          onPassword = viewModel::updatePassword,
          onName = viewModel::updateName,
          onSubmit = { if (ui.isLoginMode) viewModel.login() else viewModel.signup() },
          onToggle = viewModel::toggleMode,
          onForgot = viewModel::enterForgotPasswordMode,
        )

        if (viewModel.isAppleSignInSupported()) {
          Spacer(Modifier.height(K1Sp.m))
          Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(Modifier.weight(1f).height(0.5.dp).background(KlikLineHairline))
            Spacer(Modifier.width(K1Sp.s))
            Text("or", style = K1Type.metaSm)
            Spacer(Modifier.width(K1Sp.s))
            Box(Modifier.weight(1f).height(0.5.dp).background(KlikLineHairline))
          }
          Spacer(Modifier.height(K1Sp.m))
          AppleButton(onClick = viewModel::loginWithApple)
        }
      }

      if (ui.error != null) {
        Spacer(Modifier.height(K1Sp.lg))
        K1SignalCard(
          signal = K1Signal.Risk,
          eyebrow = "Error",
          body = ui.error!!,
        )
      }
    }

    Spacer(Modifier.height(K1Sp.huge))

    Text(
      "By continuing, you agree to Klik's Terms and Privacy Policy.",
      style = K1Type.metaSm.copy(textAlign = TextAlign.Center, color = KlikInkMuted),
      modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
    )
  }
}

@Composable
private fun AuthForm(
  isLoginMode: Boolean,
  identifier: String,
  email: String,
  password: String,
  name: String,
  isLoading: Boolean,
  onIdentifier: (String) -> Unit,
  onEmail: (String) -> Unit,
  onPassword: (String) -> Unit,
  onName: (String) -> Unit,
  onSubmit: () -> Unit,
  onToggle: () -> Unit,
  onForgot: () -> Unit,
) {
  Column {
    if (!isLoginMode) {
      KField(
        placeholder = "Full name",
        value = name,
        onChange = onName,
        capitalize = true,
      )
      Spacer(Modifier.height(K1Sp.m))
      KField(
        placeholder = "Email",
        value = email,
        onChange = onEmail,
        keyboard = KeyboardType.Email,
      )
    } else {
      KField(
        placeholder = "Email or username",
        value = identifier,
        onChange = onIdentifier,
        keyboard = KeyboardType.Email,
      )
    }
    Spacer(Modifier.height(K1Sp.m))
    KField(
      placeholder = "Password",
      value = password,
      onChange = onPassword,
      password = true,
    )

    Spacer(Modifier.height(K1Sp.lg))

    K1ButtonPrimary(
      label = when {
        isLoading && isLoginMode -> "Signing in…"
        isLoading -> "Creating account…"
        isLoginMode -> "Sign in"
        else -> "Create account"
      },
      onClick = { if (!isLoading) onSubmit() },
      enabled = !isLoading,
      pill = true,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(K1Sp.m))

    Row(
      Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        if (isLoginMode) "New here?" else "Already have an account?",
        style = K1Type.meta,
      )
      Spacer(Modifier.width(6.dp))
      Box(
        Modifier
          .k1Clickable(onClick = onToggle)
          .padding(horizontal = 4.dp, vertical = 6.dp),
      ) {
        Text(
          if (isLoginMode) "Create an account" else "Sign in",
          style = K1Type.bodyMd.copy(color = KlikInkPrimary),
        )
      }
    }

    if (isLoginMode) {
      Spacer(Modifier.height(K1Sp.s))
      Box(
        Modifier
          .fillMaxWidth()
          .k1Clickable(onClick = onForgot)
          .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          "Forgot your password?",
          style = K1Type.caption.copy(color = KlikInkTertiary),
        )
      }
    }
  }
}

@Composable
private fun ForgotPasswordForm(
  email: String,
  onEmailChange: (String) -> Unit,
  passwordResetSent: Boolean,
  onSubmit: () -> Unit,
  onCancel: () -> Unit,
  isLoading: Boolean,
) {
  Column {
    if (passwordResetSent) {
      K1SignalCard(
        signal = K1Signal.Commitment,
        eyebrow = "Check your email",
        body = "If that address is on file, a reset link is on its way.",
      )
      Spacer(Modifier.height(K1Sp.lg))
      K1ButtonPrimary(
        label = "Back to sign in",
        onClick = onCancel,
        pill = true,
        modifier = Modifier.fillMaxWidth(),
      )
    } else {
      KField(
        placeholder = "Email",
        value = email,
        onChange = onEmailChange,
        keyboard = KeyboardType.Email,
      )
      Spacer(Modifier.height(K1Sp.lg))
      K1ButtonPrimary(
        label = if (isLoading) "Sending…" else "Send reset link",
        onClick = { if (!isLoading) onSubmit() },
        enabled = !isLoading,
        pill = true,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(K1Sp.m))
      Box(
        Modifier
          .fillMaxWidth()
          .k1Clickable(onClick = onCancel)
          .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          "Cancel",
          style = K1Type.bodyMd.copy(color = KlikInkTertiary),
        )
      }
    }
  }
}

@Composable
private fun KField(
  placeholder: String,
  value: String,
  onChange: (String) -> Unit,
  keyboard: KeyboardType = KeyboardType.Text,
  password: Boolean = false,
  capitalize: Boolean = false,
) {
  Column(
    Modifier
      .fillMaxWidth()
      .clip(K1R.card)
      .background(KlikPaperCard)
      .border(0.5.dp, KlikLineHairline, K1R.card)
      .padding(horizontal = 14.dp, vertical = 12.dp),
  ) {
    Text(placeholder, style = K1Type.metaSm)
    Spacer(Modifier.height(2.dp))
    BasicTextField(
      value = value,
      onValueChange = onChange,
      singleLine = true,
      textStyle = K1Type.bodyMd,
      visualTransformation = if (password) {
        PasswordVisualTransformation()
      } else {
        VisualTransformation.None
      },
      keyboardOptions = KeyboardOptions(
        keyboardType = keyboard,
        capitalization = if (capitalize) {
          KeyboardCapitalization.Words
        } else {
          KeyboardCapitalization.None
        },
      ),
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun AppleButton(onClick: () -> Unit) {
  Row(
    Modifier
      .fillMaxWidth()
      .clip(K1R.pill)
      .background(Color.Black)
      .k1Clickable(onClick = onClick)
      .padding(vertical = 14.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      "",
      style = K1Type.bodyMd.copy(color = Color.White, fontWeight = FontWeight.Medium),
    )
    Spacer(Modifier.width(8.dp))
    Text(
      "Continue with Apple",
      style = K1Type.bodyMd.copy(color = Color.White, fontWeight = FontWeight.Medium),
    )
  }
}
