// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("unused") // Invoked reflectively
class AndroidKmpLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    apply(plugin = "com.android.library")
    apply(plugin = "org.jetbrains.kotlin.multiplatform")

    extensions.configure<LibraryExtension> {
      compileSdk = 35
      defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      }
    }

    extensions.configure<KotlinMultiplatformExtension> {
      androidTarget {
        compilerOptions {
          jvmTarget.set(JvmTarget.JVM_11)
        }
      }
    }
  }
}
