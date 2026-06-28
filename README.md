# Chipstrap

> **Native, fast, optimized Roblox launcher for Android — forked from the abandoned Chevstrap project.**

Chipstrap is a Kotlin + Jetpack Compose rewrite of [Chevstrap](https://github.com/FrosSky/Chevstrap),
which was discontinued after Roblox disabled external FFlag reads on the Android
client. Chipstrap brings it back to life with a **multi-strategy injection engine**
that works around Roblox's clamp, plus a battery of pre-tuned optimization
presets and BloxStrap-inspired server info / activity tracker features.

## Why does this exist?

The original Chevstrap README read:

> FFLAGS DISABLED BY ROBLOX ITSELF.
> DISCONTINUED

Roblox 2.650+ stopped honoring `ClientAppSettings.json` placed outside its own
private data directory, and verifies a checksum on launch. That broke every
"just drop a file" launcher on Android.

Chipstrap ships **four injection strategies** and picks the best available one
at runtime:

| Strategy | Requires | Reliable | Survives Roblox updates |
|---|---|---|---|
| **Shizuku** | Shizuku running + binder granted | ✅ | ✅ |
| **Root** | Rooted device | ✅ | ✅ |
| **Virtual space** | Parallel Space / DualSpace | ⚠️ | ⚠️ |
| **Local profile** | Nothing | ❌ (export only) | ✅ |

If no strategy is available, Chipstrap still keeps your FFlag profile locally
and exposes it via export/import so you can apply it with whatever external
tool you trust.

## Features

### FFlag management
- JSON-backed FFlag store (`Modifications/ClientSettings/ClientAppSettings.json`)
- One-tap presets: **Ultra FPS**, **Balanced**, **Battery Saver**, **High Quality**,
  **Low-End Device**, **Competitive** (uncapped FPS)
- Per-flag add / edit / delete
- Import / Export JSON (BloxStrap-compatible)
- Backups / Restore
- Preset-aware UI

### Optimization engine (pre-optimized features)
- ✅ Clear Roblox cache before launch
- ✅ Kill background apps
- ✅ Force CPU performance governor (root)
- ✅ Anti-Doze wakelock during gameplay
- ✅ GPU tuning (disable animation scale)
- ✅ BT audio buffer boost
- ✅ Aggressive memory trim
- ✅ Low-latency private DNS (1.1.1.1)
- ✅ Frame-rate cap & rendering backend toggles via FFlags

### BloxStrap-inspired
- 🌍 **Server info**: live JobID, place, universe, location (via ipinfo.io), ping
- 🕒 **Activity tracker**: session duration, recent experiences
- 🎮 **Multi-version**: Global, VNG (Vietnam), or custom Roblox package

### Native Android
- 100% **Kotlin** + **Jetpack Compose** + **Material 3**
- Built on **Coroutines** + **DataStore** + **Navigation Compose** + **OkHttp**
- Single foreground service owns the launch pipeline (survives backgrounding)
- No WebView, no Electron, no Java — pure native, fast startup, low memory

## Build

Requirements: JDK 17, Android SDK 34, Kotlin 2.1.

```bash
git clone https://github.com/TheStrongestOfTomorrow/Chipstrap.git
cd Chipstrap
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

For a release build:
```bash
./gradlew assembleRelease
```

## Install

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

On first launch, grant the notification permission (used by the launcher
foreground service) and pick an injection strategy in **Integrations**.

## Setup Shizuku (recommended, no root)

1. Install [Shizuku](https://shizuku.rikka.app/) from Google Play.
2. Start Shizuku via wireless debugging (Android 11+) or ADB.
3. In Chipstrap → Integrations → Application strategy → **Shizuku**.
4. Done — Chipstrap will use Shizuku to write `ClientAppSettings.json` into
   Roblox's private data directory on every launch.

## Setup root

1. Make sure your `su` binary works (`su -c id` returns `uid=0`).
2. In Chipstrap → Integrations → Application strategy → **Root**.

## Use a preset

1. Open the **FFlags** screen from the drawer.
2. Tap **Apply preset** under any preset you want.
3. Go back to **Home** → **Apply & Launch**.

## Project layout

```
app/src/main/kotlin/com/chipstrap/rbx/
├── ChipstrapApp.kt             # Application entrypoint
├── MainActivity.kt             # Compose host + navigation
├── core/Logger.kt              # File logger
├── data/
│   ├── AppPaths.kt             # All on-device paths
│   └── SettingsStore.kt        # DataStore-backed preferences
├── fflags/
│   ├── presets/FFlagPresets.kt # Pre-tuned FFlag bundles
│   ├── repository/             # JSON-backed flag store
│   └── strategies/             # Shizuku / Root / Virtual / Local injectors
├── optimization/               # CPU/cache/wakelock/DNS optimizations
├── roblox/                     # Package helpers + launcher pipeline
├── server/                     # Server info provider (BloxStrap-style)
├── activity/                   # Activity tracker
├── service/                    # Launcher foreground service
└── ui/                         # Compose UI (Material 3)
```

## Disclaimer

Chipstrap is **not affiliated with Roblox Corporation**. Modifying the Roblox
client may violate Roblox's Terms of Service. Use at your own risk.

## Credits

- [Chevstrap](https://github.com/FrosSky/Chevstrap) — original Android port this project was forked from.
- [BloxStrap](https://github.com/bloxstraplabs/bloxstrap) — Windows Roblox bootstrapper that inspired the server-info and activity-tracking features.
- [Roblox Client Tracker](https://github.com/MaximumADHD/Roblox-Client-Tracker) — flag reference.

## License

MIT. See [LICENSE.txt](LICENSE.txt).
