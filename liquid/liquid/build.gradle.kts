import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.kotlin.compose) // Required for Kotlin 2.0+
  // Removed publishing and validator plugins for local integration simplification
}

kotlin {
  explicitApi()
  // addDefaultLiquidTargets() - REMOVED

  // Android target removed to match root project configuration
  iosX64()
  iosArm64()
  iosSimulatorArm64()
  jvm()

  js {
    browser()
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }

  compilerOptions {
    allWarningsAsErrors = false // Relaxed for local build
  }

  // androidLibrary block removed

  sourceSets {
    commonMain.dependencies {
      api(compose.ui)
      implementation(compose.foundation)
    }

    val skikoMain by creating {
      dependsOn(commonMain.get())
    }

    iosMain {
      dependsOn(skikoMain)
    }

    jvmMain {
      dependsOn(skikoMain)
    }

    jsMain {
      dependsOn(skikoMain)
    }

    wasmJsMain {
      dependsOn(skikoMain)
    }
    // Test dependencies omitted/simplified for local build integration
    commonTest.dependencies {
      implementation(kotlin("test"))
    }
  }

  // Force link iOS targets to iosMain (in case default hierarchy failed)
  configure(listOf(iosX64(), iosArm64(), iosSimulatorArm64())) {
      compilations.getByName("main").defaultSourceSet.dependsOn(sourceSets.getByName("iosMain"))
  }
}

// dependencies block removed as constraints are likely unnecessary for local integration

// tasks.withType block removed

// apiValidation and mavenPublishing blocks removed
