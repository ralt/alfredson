# Alfredson

A small Android app that walks you through the 12-week Alfredson protocol for
Achilles tendinopathy — twice-daily eccentric calf raises with a progressive
support/load/speed schedule.

Each day the app shows what to do today (support type, speed, additional load
as a percentage of body weight and the corresponding dumbbell weight in kg),
lets you check off morning and evening sessions, and reminds you via
notifications.

## Features

- 12-week schedule, twice-daily sessions, driven by an editable JSON asset
- Morning + evening session check-offs with a streak counter
- Calendar view: 12×7 tile grid colored by completion; tap any past day to
  toggle retroactively
- Body weight input → automatic dumbbell weight calculation for the weeks
  that prescribe additional load (10% / 20% body weight)
- Configurable heel-drop variant: both straight-knee + bent-knee (canonical
  Alfredson) or straight-knee only
- French and English UI, toggleable in-app
- Daily reminders at configurable morning/evening times

## Protocol

Loaded from [`app/src/main/assets/schedule.json`](app/src/main/assets/schedule.json) —
edit this file and rebuild to adjust the protocol.

| Week | Support | Speed pattern (days 1-3 / 4-5 / 6-7) | Extra load | Plyometric |
|------|---------|--------------------------------------|------------|------------|
| 1 | Both feet | slow / medium / fast | — | — |
| 2 | Both feet, weighted on healthy side | slow / medium / fast | — | — |
| 3 | Single leg (injured) | slow / medium / fast | — | — |
| 4 | Single leg | slow / medium / fast | +10% BW | — |
| 5 | Single leg | slow / medium / fast | +20% BW | — |
| 6 | Single leg | slow / medium / fast | +20% BW | — |
| 7–9 | Single leg | slow / medium / fast | +20% BW | — |
| 10 | Single leg | slow / medium / fast | +20% BW | Pogo jumps (bilateral) on days 2/4/6 |
| 11–12 | Single leg | slow / medium / fast | +20% BW | Single-leg hops on days 2/4/6 |

Weeks 1–6 follow the source [`protocol.md`](protocol.md). Weeks 7–9 are a
consolidation phase and weeks 10–12 introduce plyometrics (Silbernagel-style
return to load). Adjust to taste in the JSON.

## Stack

- Kotlin 2.0.21, Jetpack Compose, Material3
- Navigation Compose, ViewModel + StateFlow
- DataStore Preferences for user settings
- JSON file in `filesDir` for the session log (via `kotlinx.serialization`)
- WorkManager for daily morning/evening reminders (self-rescheduling chain)
- `AppCompatDelegate.setApplicationLocales` for in-app FR/EN toggle
- AGP 8.7.3 on Gradle 8.9; minSdk 26 / targetSdk 34

## Build

```sh
./gradlew :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

`local.properties` needs `sdk.dir=/path/to/Android/Sdk` (gitignored).

## Install

With a device connected and authorized for USB debugging:

```sh
./gradlew :app:installDebug
# or
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
