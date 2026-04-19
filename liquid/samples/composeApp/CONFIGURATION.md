# Configuration Guide

## Environment Configuration

The app now supports multiple deployment environments through the `Environment` class.

### Setting the Environment

Configure the environment during app initialization (e.g., in `MainApp.kt` or `MainActivity`):

```kotlin
import io.github.fletchmckee.liquid.samples.app.data.network.Environment

// App defaults to PRODUCTION - no configuration needed for production builds

// For staging environment
Environment.configure(Environment.Type.STAGING)

// For development, use custom config with your local server URLs
val devConfig = Environment.Config(
    type = Environment.Type.DEVELOPMENT,
    baseUrl = "http://your-local-server/api/v1",
    authBaseUrl = "http://your-local-server/api/auth",
    // ... other URLs
    useHttps = false
)
Environment.configure(devConfig)
```

### Environment Configurations

#### Production (Default)
- All services: `https://hiklik.ai/api/*`

#### Staging
- All services: `https://staging.hiklik.ai/api/*`

#### Development
- Requires explicit custom configuration with your local server URLs
- No hardcoded IPs - use Environment.configure(Config(...)) with your local dev server

### Custom Environment

For custom deployments or testing:

```kotlin
val customConfig = Environment.Config(
    type = Environment.Type.DEVELOPMENT,
    baseUrl = "http://custom.server.com/api/v1",
    authBaseUrl = "http://custom.server.com/api/auth",
    // ... other URLs
    useHttps = true
)
Environment.configure(customConfig)
```

## Data Limits Configuration

Configure data limits based on device capabilities and performance requirements.

### Default Limits

```kotlin
import io.github.fletchmckee.liquid.samples.app.data.network.DataLimits

// Use defaults (already set)
DataLimits.resetToDefaults()
```

### Low-Memory Devices

```kotlin
// Reduce limits for low-memory devices
DataLimits.configureLowMemory()
```

### High-Performance Devices

```kotlin
// Increase limits for high-performance devices
DataLimits.configureHighPerformance()
```

### Custom Limits

```kotlin
// Customize specific limits
DataLimits.API.defaultQueryLimit = 150
DataLimits.UI.maxListItems = 75
DataLimits.Processing.maxSubtitleLength = 200
```

## Device Configuration

### Device ID Management

The app generates a unique device ID for authentication. To use a persistent device ID:

```kotlin
import io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo

// Set a custom or restored device ID
DeviceInfo.setDeviceId("klik_mobile_ios_123456")

// Get the current device ID
val deviceId = DeviceInfo.getDeviceId()

// Reset device ID (for testing or logout)
DeviceInfo.reset()
```

## Authentication

### Token Refresh

Token refresh is now properly implemented. Tokens will automatically refresh when they expire:

```kotlin
// Manual token refresh (if needed)
val result = authRepository.refreshToken()
when (result) {
    is Result.Success -> println("Token refreshed")
    is Result.Error -> println("Refresh failed: ${result.exception.message}")
}
```

### Token Validation

```kotlin
// Check if current token is valid
val isValid = authRepository.isTokenValid()
```

## Build Variants

Configure different environments for different build variants in your build configuration:

### Android (build.gradle.kts)

```kotlin
android {
    buildTypes {
        debug {
            buildConfigField("String", "ENV_TYPE", "\"DEVELOPMENT\"")
        }
        release {
            buildConfigField("String", "ENV_TYPE", "\"PRODUCTION\"")
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("staging") {
            dimension = "environment"
            buildConfigField("String", "ENV_TYPE", "\"STAGING\"")
        }
    }
}
```

### iOS (xcconfig files)

Create `Development.xcconfig`, `Staging.xcconfig`, and `Production.xcconfig` files:

```
// Development.xcconfig
ENV_TYPE = DEVELOPMENT

// Staging.xcconfig
ENV_TYPE = STAGING

// Production.xcconfig
ENV_TYPE = PRODUCTION
```

## Migration Notes

### Removed Features

1. **Demo Token Generation**: Removed `simulateLogin()` and `simulateSignup()` functions. The app now uses only real backend authentication.

2. **Hardcoded Device IDs**: Replaced with configurable `DeviceInfo` system.

3. **Hardcoded Endpoints**: Replaced with `Environment` configuration system.

### Data Source Architecture

The "Mock" data sources are actually in-memory caches for real API data:
- `MockTaskDataSource` → In-memory cache for tasks from API
- `MockPersonDataSource` → In-memory cache for people from API
- etc.

They are populated by `RemoteDataFetcher` during app initialization. Consider renaming to `CachedDataSource` or `InMemoryDataSource` for clarity.

## Best Practices

1. **Always configure environment at app startup** before making any API calls
2. **Persist device ID** using SecureStorage for consistent device identification
3. **Adjust data limits** based on device capabilities (detect available memory)
4. **Use appropriate environment** for each build variant
5. **Never commit production credentials** to version control

## Troubleshooting

### "Missing User-Id" Error

Configure the environment before calling `AppModule.initialize()`:

```kotlin
Environment.configure(Environment.Type.DEVELOPMENT)
AppModule.initialize()
```

### Network Timeouts

Adjust timeout values in `ApiConfig`:

```kotlin
// Currently set to 30 seconds
ApiConfig.TIMEOUT_MS = 60_000L  // Increase to 60 seconds
```

### Memory Issues on Low-End Devices

```kotlin
DataLimits.configureLowMemory()
```
