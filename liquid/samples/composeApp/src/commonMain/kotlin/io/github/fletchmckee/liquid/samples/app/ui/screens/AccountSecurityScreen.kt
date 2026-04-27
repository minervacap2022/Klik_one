// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1ButtonPrimary
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Eyebrow
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable

@Composable
fun AccountSecurityScreen(
  onChangePassword: (currentPassword: String, newPassword: String) -> Unit,
  onBack: () -> Unit,
) {
  var currentPassword by remember { mutableStateOf("") }
  var newPassword by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordError by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    // Top rail — K1 text-only chrome
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        "Back",
        style = K1Type.metaSm.copy(color = KlikInkSecondary),
        modifier = Modifier.k1Clickable(onClick = onBack).padding(end = K1Sp.m),
      )
      Box(Modifier.weight(1f))
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp),
    ) {
      K1Eyebrow("Klik")
      Spacer(Modifier.height(K1Sp.m))
      Text("Account & security.", style = K1Type.display)
      Spacer(Modifier.height(K1Sp.m))
      Text(
        "Update the password you use for email sign-in.",
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )

      Spacer(Modifier.height(K1Sp.xxl))

      K1Eyebrow("Change password")
      Spacer(Modifier.height(K1Sp.m))

      K1PasswordField(
        value = currentPassword,
        onValueChange = {
          currentPassword = it
          passwordError = null
        },
        placeholder = "Current password",
      )
      Spacer(Modifier.height(K1Sp.m))
      K1PasswordField(
        value = newPassword,
        onValueChange = {
          newPassword = it
          passwordError = null
        },
        placeholder = "New password",
      )
      Spacer(Modifier.height(K1Sp.m))
      K1PasswordField(
        value = confirmPassword,
        onValueChange = {
          confirmPassword = it
          passwordError = null
        },
        placeholder = "Confirm new password",
      )

      if (passwordError != null) {
        Spacer(Modifier.height(K1Sp.m))
        Text(
          passwordError!!,
          style = K1Type.bodySm.copy(color = KlikInkPrimary),
        )
      }

      Spacer(Modifier.height(K1Sp.xl))

      K1ButtonPrimary(
        label = if (isSubmitting) "Updating…" else "Update password",
        enabled = !isSubmitting && currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank(),
        onClick = {
          when {
            currentPassword.isBlank() -> passwordError = "Current password is required"

            newPassword.length < 8 -> passwordError = "New password must be at least 8 characters"

            newPassword != confirmPassword -> passwordError = "Passwords do not match"

            else -> {
              isSubmitting = true
              onChangePassword(currentPassword, newPassword)
            }
          }
        },
        modifier = Modifier.fillMaxWidth(),
      )

      Spacer(Modifier.height(K1Sp.xxl))
    }
  }
}

@Composable
private fun K1PasswordField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
) {
  Box(
    Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(KlikPaperCard)
      .border(0.75.dp, KlikLineHairline, RoundedCornerShape(12.dp))
      .padding(horizontal = 16.dp, vertical = 14.dp),
  ) {
    if (value.isEmpty()) {
      Text(placeholder, style = K1Type.bodyMd.copy(color = KlikInkTertiary))
    }
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      singleLine = true,
      visualTransformation = PasswordVisualTransformation(),
      textStyle = K1Type.bodyMd.copy(color = KlikInkPrimary),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
