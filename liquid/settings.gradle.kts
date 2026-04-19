// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
pluginManagement {
  includeBuild("build-logic")
  repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/central") }
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    google()
    maven { url = uri("https://maven.aliyun.com/repository/central") }
    mavenCentral()
  }
}

rootProject.name = "liquid-root"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

include(":liquid")
include(":benchmark")
include(":samples:composeApp")
include(":core:testing")
