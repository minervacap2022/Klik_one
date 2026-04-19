package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.domain.entity.Subscription
import io.github.fletchmckee.liquid.samples.app.domain.entity.SubscriptionPlan
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import liquid_root.samples.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

// Theme colors for pricing
private val KlikPrimary = Color(0xFF007AFF)
private val StarterTierColor = Color(0xFF8E8E93)
private val BasicTierColor = Color(0xFF4A90D9)
private val ProTierColor = Color(0xFFFFCC00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreen(
    plans: List<SubscriptionPlan>,
    currentSubscription: Subscription?,
    onUpgrade: (planCode: String, billingCycle: String) -> Unit,
    onDowngrade: (planCode: String, billingCycle: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isYearly by remember { mutableStateOf(false) }
    val billingCycle = if (isYearly) "yearly" else "monthly"

    // Sort plans by tier
    val sortedPlans = plans.sortedBy { tierOrder(it.planCode) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.nav_back),
                    tint = KlikBlack
                )
            }
            Text(
                "Plans",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = KlikBlack
            )
        }

        // Monthly/Yearly toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            BillingToggleButton(
                text = stringResource(Res.string.pricing_monthly),
                selected = !isYearly,
                onClick = { isYearly = false }
            )
            Spacer(Modifier.width(12.dp))
            BillingToggleButton(
                text = stringResource(Res.string.pricing_yearly),
                selected = isYearly,
                onClick = { isYearly = true }
            )
        }

        // Plans list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = sortedPlans,
                key = { it.planCode }
            ) { plan ->
                PlanCard(
                    plan = plan,
                    currentSubscription = currentSubscription,
                    billingCycle = billingCycle,
                    isYearly = isYearly,
                    onUpgrade = onUpgrade,
                    onDowngrade = onDowngrade
                )
            }
        }
    }
}

@Composable
private fun BillingToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) KlikPrimary else Color.Transparent
    val textColor = if (selected) Color.White else KlikBlack
    val borderColor = if (selected) KlikPrimary else KlikBlack.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    currentSubscription: Subscription?,
    billingCycle: String,
    isYearly: Boolean,
    onUpgrade: (String, String) -> Unit,
    onDowngrade: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val liquidState = rememberLiquidState()
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(20.dp)

    val tierColor = when (plan.planCode.lowercase()) {
        "starter" -> StarterTierColor
        "basic" -> BasicTierColor
        "pro" -> ProTierColor
        else -> StarterTierColor
    }

    val isCurrent = plan.planCode == currentSubscription?.planCode
    val currentTier = tierOrder(currentSubscription?.planCode ?: "")
    val planTier = tierOrder(plan.planCode)
    val isUpgrade = planTier > currentTier
    val isDowngrade = planTier < currentTier

    // Calculate price
    val priceInCents = if (isYearly) plan.priceYearlyInCents else plan.priceMonthlyInCents
    val priceDisplay = formatPrice(priceInCents, isYearly)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(tierColor.copy(alpha = 0.05f), cardShape)
            .border(
                BorderStroke(
                    if (isCurrent) 2.dp else 1.dp,
                    if (isCurrent) tierColor else tierColor.copy(alpha = 0.3f)
                ),
                cardShape
            )
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.02f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = tierColor.copy(alpha = 0.05f)
            }
            .clip(cardShape)
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Plan name with tier badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tierColor
                )
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .background(tierColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.pricing_current),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tierColor
                        )
                    }
                }
            }

            // Price
            Text(
                text = priceDisplay,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = KlikBlack
            )

            Spacer(Modifier.height(4.dp))

            // ASR Minutes
            FeatureRow(
                text = "ASR Minutes: ${if (plan.asrMonthlyMinutes == -1) "Unlimited" else "${plan.asrMonthlyMinutes} min/mo"}",
                enabled = true
            )

            // Storage
            FeatureRow(
                text = "Storage: ${if (plan.storageMb == -1) "Unlimited" else "${plan.storageMb} MB"}",
                enabled = true
            )

            // Cloud Backup
            FeatureRow(
                text = "Cloud Backup: ${if (plan.cloudBackupEnabled) "Included" else "Not included"}",
                enabled = plan.cloudBackupEnabled
            )

            // Features map
            plan.features.forEach { (featureName, enabled) ->
                FeatureRow(
                    text = featureName.replace("_", " ").replaceFirstChar { it.uppercase() },
                    enabled = enabled
                )
            }

            Spacer(Modifier.height(8.dp))

            // Action button
            Button(
                onClick = {
                    when {
                        isCurrent -> { /* Do nothing */ }
                        isUpgrade -> onUpgrade(plan.planCode, billingCycle)
                        isDowngrade -> onDowngrade(plan.planCode, billingCycle)
                    }
                },
                enabled = !isCurrent,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isCurrent -> Color.Gray.copy(alpha = 0.3f)
                        isUpgrade -> tierColor
                        else -> Color.Gray.copy(alpha = 0.5f)
                    },
                    contentColor = if (isCurrent) Color.Gray else Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        isCurrent -> stringResource(Res.string.pricing_current_plan)
                        isUpgrade -> stringResource(Res.string.pricing_upgrade)
                        isDowngrade -> stringResource(Res.string.pricing_downgrade)
                        else -> stringResource(Res.string.pricing_select)
                    },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (enabled) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = if (enabled) Color(0xFF34C759) else Color(0xFFFF3B30),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) KlikBlack else KlikBlack.copy(alpha = 0.5f)
        )
    }
}

private fun tierOrder(planCode: String): Int = when (planCode.lowercase()) {
    "starter" -> 0
    "basic" -> 1
    "pro" -> 2
    else -> -1
}

private fun formatPrice(priceInCents: Int, isYearly: Boolean): String {
    val dollars = priceInCents / 100
    val cents = priceInCents % 100
    val formatted = if (cents == 0) {
        "$${dollars}"
    } else {
        "$${dollars}.${cents.toString().padStart(2, '0')}"
    }
    return if (isYearly) "$formatted/yr" else "$formatted/mo"
}
