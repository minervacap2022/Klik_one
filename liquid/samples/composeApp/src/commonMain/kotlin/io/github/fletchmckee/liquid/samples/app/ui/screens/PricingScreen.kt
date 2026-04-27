// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.domain.entity.Subscription
import io.github.fletchmckee.liquid.samples.app.domain.entity.SubscriptionPlan
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1ButtonPrimary
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Chip
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Eyebrow
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable

@Composable
fun PricingScreen(
  plans: List<SubscriptionPlan>,
  currentSubscription: Subscription?,
  onUpgrade: (planCode: String, billingCycle: String) -> Unit,
  onDowngrade: (planCode: String, billingCycle: String) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var isYearly by remember { mutableStateOf(false) }
  val billingCycle = if (isYearly) "yearly" else "monthly"
  val sortedPlans = plans.sortedBy { tierOrder(it.planCode) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
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

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
    ) {
      item {
        K1Eyebrow("Klik · Plans")
        Spacer(Modifier.height(K1Sp.m))
        Text("Pick what fits your week.", style = K1Type.display)
        Spacer(Modifier.height(K1Sp.m))
        Text(
          "Every plan keeps Klik listening quietly. Upgrade when you want deeper memory, risk signals, or cross-project reasoning.",
          style = K1Type.bodySm.copy(color = KlikInkSecondary),
        )
        Spacer(Modifier.height(K1Sp.xl))

        // Billing toggle — paper track with two K1 chips.
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          K1Chip(
            label = "Monthly",
            selected = !isYearly,
            onClick = { isYearly = false },
          )
          K1Chip(
            label = "Yearly · save ≈ 2 mo",
            selected = isYearly,
            onClick = { isYearly = true },
          )
        }
        Spacer(Modifier.height(K1Sp.xl))
      }

      items(items = sortedPlans, key = { it.planCode }) { plan ->
        K1PlanCard(
          plan = plan,
          isYearly = isYearly,
          currentPlanCode = currentSubscription?.planCode,
          onUpgrade = { onUpgrade(plan.planCode, billingCycle) },
          onDowngrade = { onDowngrade(plan.planCode, billingCycle) },
        )
        Spacer(Modifier.height(12.dp))
      }

      item {
        Spacer(Modifier.height(K1Sp.xxl))
        Text(
          "Billed through the App Store. Cancel or switch at any time.",
          style = K1Type.metaSm.copy(color = KlikInkTertiary),
        )
        Spacer(Modifier.height(K1Sp.xxl))
      }
    }
  }
}

@Composable
private fun K1PlanCard(
  plan: SubscriptionPlan,
  isYearly: Boolean,
  currentPlanCode: String?,
  onUpgrade: () -> Unit,
  onDowngrade: () -> Unit,
) {
  val isCurrent = plan.planCode.equals(currentPlanCode, ignoreCase = true)
  val isProTier = plan.planCode.equals("pro", ignoreCase = true)
  val currentTier = currentPlanCode?.let { tierOrder(it) } ?: 0
  val thisTier = tierOrder(plan.planCode)
  val isUpgrade = thisTier > currentTier
  val isDowngrade = thisTier < currentTier

  val shape = RoundedCornerShape(16.dp)
  val bg = if (isProTier) KlikInkPrimary else KlikPaperCard
  val fg = if (isProTier) KlikPaperCard else KlikInkPrimary
  val fgSecondary = if (isProTier) KlikPaperCard.copy(alpha = 0.75f) else KlikInkSecondary
  val fgTertiary = if (isProTier) KlikPaperCard.copy(alpha = 0.5f) else KlikInkTertiary

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(bg)
      .border(0.75.dp, KlikLineHairline, shape)
      .padding(24.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top,
    ) {
      Column(Modifier.weight(1f)) {
        K1Eyebrow(
          text = plan.planCode.uppercase(),
          modifier = Modifier,
        )
        Spacer(Modifier.height(4.dp))
        Text(
          plan.displayName,
          style = K1Type.h2.copy(color = fg),
        )
      }
      if (isCurrent) {
        Box(
          Modifier
            .clip(CircleShape)
            .background(if (isProTier) KlikPaperCard.copy(alpha = 0.15f) else KlikPaperSoft)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
          Text(
            "Current",
            style = K1Type.metaSm.copy(color = fg, fontWeight = FontWeight.Medium),
          )
        }
      }
    }

    Spacer(Modifier.height(K1Sp.m))
    val priceCents = if (isYearly) plan.priceYearlyInCents else plan.priceMonthlyInCents
    Text(
      formatPrice(priceCents, isYearly),
      style = K1Type.h1.copy(color = fg),
    )
    Spacer(Modifier.height(K1Sp.lg))

    // Capabilities — extracted from features map + numeric fields.
    val bullets = buildPlanBullets(plan)
    bullets.forEach { line ->
      Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 4.dp),
      ) {
        Text(
          "·",
          style = K1Type.bodyMd.copy(color = fgTertiary),
          modifier = Modifier.width(14.dp),
        )
        Text(line, style = K1Type.bodySm.copy(color = fgSecondary))
      }
    }

    Spacer(Modifier.height(K1Sp.lg))

    when {
      isCurrent -> K1DisabledBanner("You're on this plan.", fg = fg, onInk = isProTier)

      isUpgrade -> K1ButtonPrimary(
        label = "Upgrade",
        onClick = onUpgrade,
        modifier = Modifier.fillMaxWidth(),
      )

      isDowngrade -> {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isProTier) KlikPaperCard.copy(alpha = 0.15f) else KlikPaperSoft)
            .k1Clickable(onClick = onDowngrade)
            .padding(vertical = 14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text("Downgrade", style = K1Type.bodyMd.copy(color = fg))
        }
      }

      else -> K1ButtonPrimary(
        label = "Choose plan",
        onClick = onUpgrade,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun K1DisabledBanner(label: String, fg: androidx.compose.ui.graphics.Color, onInk: Boolean) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(if (onInk) KlikPaperCard.copy(alpha = 0.12f) else KlikPaperSoft)
      .padding(vertical = 14.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(label, style = K1Type.bodyMd.copy(color = fg))
  }
}

private fun buildPlanBullets(plan: SubscriptionPlan): List<String> {
  val out = mutableListOf<String>()
  out += "${plan.asrMonthlyMinutes} min of speech a month"
  out += "${plan.storageMb} MB of session storage"
  if (plan.cloudBackupEnabled) out += "Cloud backup"
  val f = plan.features
  if (f["insights_enabled"] == true) out += "Daily insights"
  if (f["memory_enabled"] == true) out += "Persistent memory"
  if (f["goals_enabled"] == true) out += "Goals & growth tree"
  if (f["risk_analysis"] == true) out += "Risk analysis"
  if (f["knowledge_graph"] == true) out += "Full knowledge graph"
  return out
}

private fun tierOrder(planCode: String): Int = when (planCode.lowercase()) {
  "starter" -> 0
  "basic" -> 1
  "pro" -> 2
  else -> -1
}

private fun formatPrice(priceInCents: Int, isYearly: Boolean): String {
  if (priceInCents == 0) return "Free"
  val dollars = priceInCents / 100
  val cents = priceInCents % 100
  val base = if (cents == 0) "$$dollars" else "$$dollars.${cents.toString().padStart(2, '0')}"
  return if (isYearly) "$base /yr" else "$base /mo"
}
