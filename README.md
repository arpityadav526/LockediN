# LockediN

A personal study-lockdown Android app. Once activated, the phone is locked into this single app — no navigation to home, no back button, no social media, no way out. All study tools are built inside. The device owner (a trusted sibling/parent) holds a secret PIN and is the only one who can unlock the device.

## Features

- **Kiosk Lock Mode** — Device is locked into the app with no way out
- **Boot Persistence** — Re-locks after power cycles
- **Accessibility Service Fallback** — Extra protection layer
- **File Hub** — Receive and view homework files (PDF, images, documents)
- **AI Chat** — GPT-4o-mini powered study assistant
- **Scientific Calculator** — Full expression parser with trig, log, sqrt
- **Dictionary** — English word definitions via Free Dictionary API
- **Pomodoro Timer** — 25/5/15 min cycles with notifications
- **Quick Notes** — Auto-saving scratchpad
- **Unit Converter** — Length, mass, temperature, area, volume
- **Formula Sheet** — Algebra, geometry, trig, calculus, physics, chemistry

## Setup

### Prerequisites
- Android Studio (latest stable)
- JDK 17
- Android SDK 34

### Build
1. Clone the repository
2. Open in Android Studio
3. Add your OpenAI API key to `local.properties`:
   ```
   OPENAI_API_KEY=your_openai_key_here
   ```
4. Build and run on a device (API 26+)

### Owner Setup (One-time, on the target device)

#### Step 1 — Set Device Owner
Connect the phone to a laptop with USB debugging enabled:
```bash
adb shell dpm set-device-owner com.lockedin/.feature.lock.DeviceAdminReceiver
```

> **Note:** If the device has Google accounts set up, remove them before running this command, or it will fail. Re-add them after.

#### Step 2 — Enable Accessibility Service
On the phone:
Settings → Accessibility → Downloaded Apps → LockediN Guard → Enable

#### Step 3 — Launch LockediN
Set up your PIN on first launch. The device enters lockdown.

#### Step 4 — How to Unlock Anytime
Tap the LockediN logo in the top-right corner **5 times in quick succession** → enter your PIN.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Repository |
| DI | Hilt (Dagger-Hilt) |
| Database | Room |
| Preferences | DataStore |
| Networking | Retrofit + OkHttp |
| PDF Viewer | barteksc/AndroidPdfViewer |
| Image Loading | Coil |

## License
Private — not for redistribution.
