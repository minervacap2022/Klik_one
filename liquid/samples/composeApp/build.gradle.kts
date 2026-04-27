// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
import com.android.build.api.variant.HasUnitTestBuilder
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.liquid.android.application)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  // alias(libs.plugins.compose.hotReload) // Disabled - incompatible with Gradle 9
  alias(libs.plugins.roborazzi)
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll(
      "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
      "-opt-in=kotlin.time.ExperimentalTime",
    )
  }

  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  listOf(
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach { iosTarget ->
    // Configure DEBUG framework for development
    iosTarget.binaries.framework(buildTypes = setOf(NativeBuildType.DEBUG)) {
      baseName = "ComposeApp"
      isStatic = false
    }

    // Configure RELEASE framework for App Store submissions
    // Release builds automatically generate dSYM files
    iosTarget.binaries.framework(buildTypes = setOf(NativeBuildType.RELEASE)) {
      baseName = "ComposeApp"
      isStatic = false
    }

    // Disabled - incompatible with Xcode 26 beta SDK
    // Enables calling Swift code from Kotlin for the WebVIew.
    // iosTarget.compilations.named("main") {
    //   cinterops.register("SwiftGlassWebViewProvider") {
    //     definitionFile.set(
    //       project.layout.projectDirectory.file("../iosApp/iosApp/Interops/SwiftGlassWebViewProvider.def"),
    //     )
    //     includeDirs(
    //       project.layout.projectDirectory
    //         .dir("../iosApp/iosApp/Interops/")
    //         .asFile,
    //     )
    //   }
    // }

    // EventKit bridge for Apple Calendar/Reminders integration
    // Exposes EKEntityType and EKSpan enum constants to Kotlin/Native
    iosTarget.compilations.named("main") {
      cinterops.register("EventKitBridge") {
        definitionFile.set(
          project.layout.projectDirectory.file("../iosApp/iosApp/Interops/EventKit.def"),
        )
        includeDirs(
          project.layout.projectDirectory
            .dir("../iosApp/iosApp/Interops/")
            .asFile,
        )
      }
    }
  }

  jvm()

  js {
    browser()
    binaries.executable()
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.liquid)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.material3)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.components.resources)
      implementation(libs.compose.material.icons)
      implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
      implementation(libs.kotlinx.serialization)
      // Disabled due to network/TLS issues downloading dependencies
      // implementation(libs.jetbrains.navigation.compose)
      implementation(libs.coil.compose)
      implementation(libs.coil.ktor)
      implementation(libs.markdown.renderer)
      implementation(libs.markdown.renderer.m3)
      implementation(libs.ktor.core)
      // implementation(libs.jetbrains.lifecycle.runtimeCompose)
      // implementation(libs.jetbrains.material3.adaptive)
    }

    androidMain.dependencies {
      // implementation(libs.ktor.cio)
      implementation(libs.activity.compose)
      implementation(libs.androidx.lifecycle.process)
      implementation(libs.androidx.media3.exoplayer)
      implementation(libs.androidx.media3.ui.compose)
      // EncryptedSharedPreferences for secure token storage
      implementation(libs.androidx.security.crypto)
    }

    iosMain.dependencies {
      implementation(libs.ktor.darwin)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(projects.core.testing)
    }

    jvmMain.dependencies {
      implementation(libs.ktor.cio)
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutines.swing)
    }
  }
}

android {
  namespace = "io.github.fletchmckee.liquid.samples.app"

  defaultConfig {
    applicationId = "io.github.fletchmckee.liquid.samples.app"
    versionCode = 1
    versionName = "1.0"
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }

    create("benchmark") {
      initWith(buildTypes.getByName("release"))
      signingConfig = signingConfigs.getByName("debug")
      matchingFallbacks += listOf("release")
      isDebuggable = false
    }
  }

  testOptions {
    animationsDisabled = true
  }
}

androidComponents {
  beforeVariants(selector().withBuildType("release")) { variantBuilder ->
    variantBuilder.enable = false
  }

  beforeVariants(selector().withBuildType("benchmark")) { variantBuilder ->
    (variantBuilder as? HasUnitTestBuilder)?.apply {
      enableUnitTest = false
    }
  }
}

compose.desktop {
  application {
    mainClass = "io.github.fletchmckee.liquid.samples.app.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "io.github.fletchmckee.liquid.samples.app"
      packageVersion = "1.0.0"
    }
  }
}

tasks.withType<KotlinJsTest> {
  enabled = false
}

tasks.withType<Test>().configureEach {
  filter {
    isFailOnNoMatchingTests = false
  }
}

roborazzi {
  outputDir.set(project.layout.projectDirectory.dir("screenshots"))
}

// Configure Compose Resources for iOS bundling
compose.resources {
  publicResClass = true
  packageOfResClass = "liquid_root.samples.composeapp.generated.resources"
  generateResClass = auto
}
