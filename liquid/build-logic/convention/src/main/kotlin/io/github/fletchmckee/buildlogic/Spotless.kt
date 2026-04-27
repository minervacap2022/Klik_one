// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.buildlogic

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal fun Project.configureSpotless() {
  with(pluginManager) { apply("com.diffplug.spotless") }

  spotless {
    val ktlintDep = libs.findLibrary("ktlint-core").get()
    val composeRulesDep = libs.findLibrary("ktlint-compose-rules").get()
    val ktlintVersion = ktlintDep.get().version
    val composeRulesCoordinates = "${composeRulesDep.get().module}:${composeRulesDep.get().version}"

    kotlin {
      target("**/*.kt")
      targetExclude("build/**/*.kt")
      ktlint(ktlintVersion)
        .editorConfigOverride(
          mapOf(
            "ktlint_standard_filename" to "disabled",
            "ktlint_standard_property-naming" to "disabled",
            "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
            // Stylistic Compose / standard-rule opinions the K1 codebase
            // has not adopted as conventions. Real bugs (wildcard imports,
            // syntax errors) are still enforced.
            "ktlint_compose_modifier-missing-check" to "disabled",
            "ktlint_compose_param-order-check" to "disabled",
            "ktlint_compose_parameter-naming" to "disabled",
            "ktlint_compose_mutable-state-autoboxing" to "disabled",
            "ktlint_compose_lambda-param-in-effect" to "disabled",
            "ktlint_compose_modifier-clickable-order" to "disabled",
            "ktlint_compose_lambda-param-event-trailing" to "disabled",
            "ktlint_compose_multiple-emitters-check" to "disabled",
            "ktlint_standard_value-parameter-comment" to "disabled",
            "ktlint_standard_backing-property-naming" to "disabled",
          ),
        ).customRuleSets(listOf(composeRulesCoordinates))
      licenseHeaderFile(rootProject.file("spotless/copyright.txt"))
    }

    kotlinGradle {
      target("**/*.kts")
      targetExclude("build/**/*.kts")
      ktlint(ktlintVersion)
      licenseHeaderFile(rootProject.file("spotless/copyright.txt"), "(^(?![\\/ ]\\**).*$)")
    }
  }
}

private fun Project.spotless(action: SpotlessExtension.() -> Unit) = extensions.configure<SpotlessExtension>(action)
