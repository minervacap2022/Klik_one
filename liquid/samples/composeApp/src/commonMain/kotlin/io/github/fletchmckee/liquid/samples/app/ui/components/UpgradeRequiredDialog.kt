package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.runtime.Composable

@Composable
fun UpgradeRequiredDialog(
    featureName: String,
    currentTier: String,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    LiquidGlassDialog(
        onDismissRequest = onDismiss,
        title = "Upgrade Required",
        message = "$featureName requires the Pro plan. Upgrade to unlock this feature.",
        confirmText = "View Plans",
        onConfirm = onUpgrade,
        dismissText = "Cancel"
    )
}
