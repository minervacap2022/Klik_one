# Klik One

A new Compose Multiplatform app implementing the **Klik One** visual design —
minimal monochrome, near-black ink on warm paper, editorial typography.
Reference mockups: `../klik_one_5_screens.html` and
`../klik_one_session_detail.html` in the parent `Klik/` folder.

## Layout

```
liquid/
  samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/ui/klikone/
    KlikOneKit.kt         — design system (K1Type, K1Sp, K1R, atoms: Chip,
                              SectionHeader, Avatar, Waveform, AskFab,
                              SignalCard, BottomNav, SettingsRow, StatusPill,
                              TopBar, Screen scaffold, 4 line-icons)
    TodayScreen.kt        — Today tab
    MovesScreen.kt        — Moves tab (replaces EventsScreen)
    NetworkScreen.kt      — Network tab (replaces WorkLifeScreen)
    YouScreen.kt          — You tab (replaces ProfileScreen)
    SessionDetailScreen.kt — past-meeting detail (tabs: Summary / To-dos /
                              Transcript / Highlights)
    OnboardingScreen.kt   — 3-step first-run: Welcome / Pair / Pick role
    AskKlikSheet.kt       — bottom-sheet Ask Klik chat
    LiveRecordingScreen.kt — full-screen capture surface
```

## Status

All files compile cleanly against the iOS Simulator Arm64 target
(`./gradlew :samples:composeApp:compileKotlinIosSimulatorArm64`).

For the backend fields/endpoints still required to light up the full
experience (3-line summaries, structured decisions, live-transcript
WebSocket, weekly/attention signals, action resolver, etc.), see
[BACKEND-REQUIREMENTS.md](./BACKEND-REQUIREMENTS.md).

## Build

```bash
cd liquid
./gradlew :samples:composeApp:linkDebugFrameworkIosSimulatorArm64
open samples/iosApp/iosApp.xcodeproj
```
