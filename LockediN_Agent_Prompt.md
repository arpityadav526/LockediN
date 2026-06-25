# LockediN — Android App: Complete Build Specification

## Agent Instructions
You are building a **complete, production-ready Android application** from scratch called **LockediN**.
Read every section carefully before writing a single line of code.
Do not skip sections. Do not use placeholder comments like `// TODO: implement`.
Every feature described must be fully implemented.

---

## 1. Project Overview

**App name**: LockediN  
**Package name**: `com.lockedin`  
**Purpose**: A personal study-lockdown app for a student. Once activated, the phone is locked into this single app — no navigation to home, no back button, no social media, no way out. All study tools are built inside. The device owner (a trusted sibling/parent) holds a secret PIN and is the only one who can unlock the device.

---

## 2. Core Feature Requirements

### 2.1 Kiosk Lock Mode
- On every launch, the app immediately enters Android's **Lock Task Mode** via `Activity.startLockTask()`
- This disables: home button, back button, recent apps button, notification shade, status bar interactions
- Lock state is persisted in DataStore. Even after full power-off and reboot, the device re-enters lock mode automatically on boot
- Use `android:lockTaskMode="always"` in the manifest for the MainActivity

### 2.2 Owner Exit Mechanism (Secret Unlock)
- The student cannot exit under any normal circumstances
- The owner (sibling/parent) exits by tapping **the LockediN logo 5 times in under 3 seconds** anywhere on the home screen
- After the 5th tap, a PIN dialog silently appears (no animation, no sound)
- The PIN is a 6-digit numeric code set during first launch
- On correct PIN entry: call `stopLockTask()`, set `isLocked = false` in DataStore, navigate to Settings screen
- On wrong PIN: dialog dismisses silently. No error message or shake (prevents brute-force detection)
- After unlocking, owner can: re-lock manually, change PIN, delete files

### 2.3 Boot Persistence
- Register a `BroadcastReceiver` for `android.intent.action.BOOT_COMPLETED`
- On boot, read `isLocked` from DataStore (blocking read using `runBlocking`)
- If `isLocked == true`, launch `MainActivity` with `FLAG_ACTIVITY_NEW_TASK`
- MainActivity will call `startLockTask()` again on resume

### 2.4 Accessibility Service (Fallback Layer)
- Register an `AccessibilityService` named `LockGuardService`
- Monitor `TYPE_WINDOW_STATE_CHANGED` events
- If `isLocked == true` and the foreground package is NOT `com.lockedin`, immediately launch MainActivity
- This is the secondary defense if Lock Task Mode alone is insufficient on a specific device

### 2.5 File Import from WhatsApp
- Register `ShareReceiverActivity` as a share target in the manifest for:  
  `application/pdf`, `image/*`, `application/msword`,  
  `application/vnd.openxmlformats-officedocument.wordprocessingml.document`,  
  `text/plain`, `*/*`
- When a file is shared from WhatsApp (or any app), copy it to `context.filesDir/homework/`
- Save metadata to Room database (name, path, type, size, date)
- Show a brief toast: "Homework saved to LockediN ✓"

### 2.6 Built-in Study Tools
All tools accessible from a bottom navigation bar or a home grid. No external browser or app is accessible.

| Tool | Description |
|------|-------------|
| File Hub | List and open received homework files |
| AI Chat | ChatGPT-powered assistant (OpenAI `gpt-4o-mini`) |
| Calculator | Scientific calculator with history |
| Dictionary | English word definitions via Free Dictionary API |
| Pomodoro Timer | 25-min study / 5-min break cycles with notification |
| Quick Notes | Persistent scratchpad saved in Room |
| Unit Converter | Length, mass, temperature, area, volume |
| Formula Sheet | Static cheat sheet: algebra, geometry, physics, chemistry |

---

## 3. Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Kotlin | 1.9+ |
| UI | Jetpack Compose | BOM 2024.06.00 |
| Design system | Material Design 3 | (included in Compose BOM) |
| Architecture | MVVM + Repository | — |
| DI | Hilt (Dagger-Hilt) | 2.51.1 |
| Navigation | Compose Navigation | 2.7.7 |
| Database | Room | 2.6.1 |
| Preferences | DataStore (Preferences) | 1.1.1 |
| Networking | Retrofit | 2.11.0 |
| HTTP client | OkHttp | 4.12.0 |
| JSON | Gson | 2.10.1 |
| PDF viewer | barteksc/AndroidPdfViewer | 3.2.0-beta.1 |
| Image loading | Coil | 2.6.0 |
| Coroutines | Kotlinx Coroutines | 1.8.1 |
| Min SDK | — | 26 (Android 8.0) |
| Target SDK | — | 34 (Android 14) |
| Build system | Gradle with Kotlin DSL | — |

---

## 4. Gradle Setup

### `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "LockediN"
include(":app")
```

### `app/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.lockedin"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.lockedin"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures { compose = true }
    kotlinOptions { jvmTarget = "17" }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Retrofit + OkHttp + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    // Coil (image loading)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // PDF Viewer
    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")

    // Accessibility
    implementation("androidx.core:core-ktx:1.13.1")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

---

## 5. AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.lockedin">

    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE"
        tools:ignore="ProtectedPermissions" />

    <application
        android:name=".LockediNApp"
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="LockediN"
        android:theme="@style/Theme.LockediN"
        android:networkSecurityConfig="@xml/network_security_config">

        <!-- Main Activity — Kiosk mode -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:lockTaskMode="always"
            android:launchMode="singleTask"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.HOME" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Share Receiver — receives files from WhatsApp etc. -->
        <activity
            android:name=".feature.share.ShareReceiverActivity"
            android:exported="true"
            android:launchMode="singleTask">
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="application/pdf" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="image/*" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="application/msword" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="application/vnd.openxmlformats-officedocument.wordprocessingml.document" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="text/plain" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="*/*" />
            </intent-filter>
        </activity>

        <!-- Boot Receiver -->
        <receiver
            android:name=".feature.lock.BootReceiver"
            android:exported="true"
            android:enabled="true">
            <intent-filter android:priority="999">
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
            </intent-filter>
        </receiver>

        <!-- Device Admin Receiver (for true kiosk mode via ADB) -->
        <receiver
            android:name=".feature.lock.DeviceAdminReceiver"
            android:exported="true"
            android:permission="android.permission.BIND_DEVICE_ADMIN">
            <meta-data
                android:name="android.app.device_admin"
                android:resource="@xml/device_admin" />
            <intent-filter>
                <action android:name="android.app.action.DEVICE_ADMIN_ENABLED" />
            </intent-filter>
        </receiver>

        <!-- Accessibility Service -->
        <service
            android:name=".feature.lock.LockGuardService"
            android:exported="true"
            android:label="LockediN Guard"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>

        <!-- FileProvider for sharing files into the app -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="com.lockedin.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

    </application>
</manifest>
```

---

## 6. Resource XML Files

### `res/xml/device_admin.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<device-admin xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-policies>
        <force-lock />
        <wipe-data />
    </uses-policies>
</device-admin>
```

### `res/xml/accessibility_service_config.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged|typeWindowContentChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="false"
    android:description="@string/accessibility_description"
    android:notificationTimeout="100"
    android:packageNames="" />
```

### `res/xml/file_paths.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <files-path name="homework" path="homework/" />
    <cache-path name="cache" path="." />
</paths>
```

### `res/xml/network_security_config.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

---

## 7. Project Package Structure

```
com.lockedin/
├── LockediNApp.kt
├── MainActivity.kt
├── di/
│   ├── AppModule.kt
│   └── DatabaseModule.kt
├── core/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── components/
│   │   ├── PinDialog.kt
│   │   ├── ToolCard.kt
│   │   └── FileCard.kt
│   └── utils/
│       ├── SecurityUtils.kt
│       └── FileUtils.kt
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   ├── FileDao.kt
│   │   │   ├── NoteDao.kt
│   │   │   └── ChatMessageDao.kt
│   │   └── entity/
│   │       ├── FileEntity.kt
│   │       ├── NoteEntity.kt
│   │       └── ChatMessageEntity.kt
│   ├── repository/
│   │   ├── FileRepository.kt
│   │   ├── NoteRepository.kt
│   │   └── ChatRepository.kt
│   └── preferences/
│       └── AppPreferences.kt
├── domain/
│   └── model/
│       ├── StudyFile.kt
│       ├── Note.kt
│       └── ChatMessage.kt
├── feature/
│   ├── lock/
│   │   ├── LockManager.kt
│   │   ├── DeviceAdminReceiver.kt
│   │   ├── BootReceiver.kt
│   │   └── LockGuardService.kt
│   ├── share/
│   │   └── ShareReceiverActivity.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── files/
│   │   ├── FilesScreen.kt
│   │   ├── FilesViewModel.kt
│   │   └── viewer/
│   │       ├── PdfViewerScreen.kt
│   │       └── ImageViewerScreen.kt
│   ├── aichat/
│   │   ├── AiChatScreen.kt
│   │   ├── AiChatViewModel.kt
│   │   └── OpenAiService.kt
│   ├── tools/
│   │   ├── calculator/
│   │   │   ├── CalculatorScreen.kt
│   │   │   └── CalculatorViewModel.kt
│   │   ├── dictionary/
│   │   │   ├── DictionaryScreen.kt
│   │   │   ├── DictionaryViewModel.kt
│   │   │   └── DictionaryApiService.kt
│   │   ├── timer/
│   │   │   ├── TimerScreen.kt
│   │   │   └── TimerViewModel.kt
│   │   ├── notes/
│   │   │   ├── NotesScreen.kt
│   │   │   └── NotesViewModel.kt
│   │   ├── converter/
│   │   │   ├── ConverterScreen.kt
│   │   │   └── ConverterViewModel.kt
│   │   └── formulas/
│   │       └── FormulaSheetScreen.kt
│   └── settings/
│       ├── SettingsScreen.kt
│       └── SettingsViewModel.kt
└── navigation/
    ├── AppNavigation.kt
    └── NavRoutes.kt
```

---

## 8. Database Schema (Room)

### `FileEntity.kt`
```kotlin
@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val path: String,       // absolute path inside filesDir
    val mimeType: String,
    val sizeBytes: Long,
    val receivedAt: Long    // System.currentTimeMillis()
)
```

### `NoteEntity.kt`
```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val updatedAt: Long
)
```

### `ChatMessageEntity.kt`
```kotlin
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,       // "user" or "assistant"
    val content: String,
    val timestamp: Long
)
```

### `AppDatabase.kt`
```kotlin
@Database(
    entities = [FileEntity::class, NoteEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun noteDao(): NoteDao
    abstract fun chatMessageDao(): ChatMessageDao
}
```

---

## 9. DataStore Preferences (`AppPreferences.kt`)

Keys to store:
- `IS_LOCKED: Boolean` — whether kiosk mode is currently active
- `PIN_HASH: String` — SHA-256 hash of the 6-digit PIN
- `PIN_SET: Boolean` — whether a PIN has been configured yet
- `TOTAL_STUDY_MINUTES: Int` — cumulative study time (shown on home)
- `POMODORO_CYCLES: Int` — completed pomodoro cycles

Implement using `PreferencesDataStore`. Expose each key as a `Flow<T?>`.
Provide a `suspend` setter for each.

---

## 10. Lock System Implementation

### `LockManager.kt`
```kotlin
@Singleton
class LockManager @Inject constructor(
    private val appPreferences: AppPreferences
) {
    fun enterLockMode(activity: Activity) {
        try {
            activity.startLockTask()
        } catch (e: Exception) {
            // startLockTask() fails silently on some devices without Device Owner
            // The Accessibility Service acts as fallback
        }
        // Also ensure window flags block recent apps on older Android
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun exitLockMode(activity: Activity) {
        try {
            activity.stopLockTask()
        } catch (e: Exception) { /* no-op */ }
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
```

### `DeviceAdminReceiver.kt`
```kotlin
@Singleton
class DeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        // Device admin enabled — now startLockTask() works without user confirmation
    }
}
```

### `BootReceiver.kt`
```kotlin
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return

        // Read lock state synchronously
        val prefs = PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("app_prefs") }
        )
        val isLocked = runBlocking {
            prefs.data.map { it[booleanPreferencesKey("IS_LOCKED")] ?: true }.first()
        }
        if (isLocked) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("FROM_BOOT", true)
            }
            context.startActivity(launchIntent)
        }
    }
}
```

### `LockGuardService.kt` (Accessibility Service)
```kotlin
class LockGuardService : AccessibilityService() {

    private lateinit var appPreferences: AppPreferences

    override fun onServiceConnected() {
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName == "com.lockedin") return

        val isLocked = runBlocking {
            appPreferences.isLocked.first() ?: true
        }
        if (isLocked) {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {}
}
```

---

## 11. MainActivity.kt

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var lockManager: LockManager
    @Inject lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent screenshots / screen recordings
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Enter kiosk mode
        lockManager.enterLockMode(this)

        setContent {
            LockediNTheme {
                AppNavigation(
                    lockManager = lockManager,
                    onUnlock = { lockManager.exitLockMode(this) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-enter lock mode if we somehow got here while still locked
        lifecycleScope.launch {
            if (appPreferences.isLocked.first() == true) {
                lockManager.enterLockMode(this@MainActivity)
            }
        }
    }

    // Block hardware back button while locked
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // intentionally do nothing
    }
}
```

---

## 12. Navigation (`AppNavigation.kt`)

Use Jetpack Compose Navigation with a bottom navigation bar.

### Routes (`NavRoutes.kt`)
```kotlin
object Routes {
    const val HOME = "home"
    const val FILES = "files"
    const val PDF_VIEWER = "pdf_viewer/{fileId}"
    const val IMAGE_VIEWER = "image_viewer/{fileId}"
    const val AI_CHAT = "ai_chat"
    const val CALCULATOR = "calculator"
    const val DICTIONARY = "dictionary"
    const val TIMER = "timer"
    const val NOTES = "notes"
    const val CONVERTER = "converter"
    const val FORMULAS = "formulas"
    const val SETTINGS = "settings"
}
```

Bottom nav tabs: Home, Files, AI Chat, Tools (sub-menu), Notes

---

## 13. UI Theme

### Color Palette (calm, studious, deep blue)

```kotlin
// Color.kt
val PrimaryBlue = Color(0xFF1A3A5C)
val SecondaryBlue = Color(0xFF2D6A9F)
val AccentTeal = Color(0xFF0D9488)
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val OutlineLight = Color(0xFFCBD5E1)
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF475569)

// Dark theme
val BackgroundDark = Color(0xFF0F1729)
val SurfaceDark = Color(0xFF1E2D45)
val TextPrimaryDark = Color(0xFFE2E8F0)
```

### Typography
Use `Roboto` (default on Android). Font sizes: 22 (headline), 18 (title), 16 (body), 14 (label), 12 (caption).

### Shapes
Rounded corners throughout: small = 8dp, medium = 12dp, large = 16dp.

---

## 14. First-Launch PIN Setup

On first launch, check `PIN_SET` in DataStore.
If `false`, show a full-screen **SetupScreen** (not accessible again after setup):

```
"Welcome to LockediN"
"Set your owner PIN — you will need this to unlock the device."

[6-digit PIN input]
[Confirm PIN input]
[Confirm button]
```

Hash the PIN with SHA-256 and store the hash in DataStore. Set `PIN_SET = true` and `IS_LOCKED = true`. Never store the raw PIN.

```kotlin
// SecurityUtils.kt
fun hashPin(pin: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
    return hash.joinToString("") { "%02x".format(it) }
}

fun verifyPin(inputPin: String, storedHash: String): Boolean {
    return hashPin(inputPin) == storedHash
}
```

---

## 15. Owner Exit & Secret Gesture

In `HomeScreen.kt`, attach a tap counter to the LockediN logo:

```kotlin
var tapCount by remember { mutableIntStateOf(0) }
var lastTapTime by remember { mutableLongStateOf(0L) }
var showPinDialog by remember { mutableStateOf(false) }

Image(
    painter = painterResource(R.drawable.ic_logo),
    contentDescription = "LockediN",
    modifier = Modifier
        .size(40.dp)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null  // No ripple — secret gesture
        ) {
            val now = System.currentTimeMillis()
            if (now - lastTapTime > 3000L) tapCount = 0
            tapCount++
            lastTapTime = now
            if (tapCount >= 5) {
                tapCount = 0
                showPinDialog = true
            }
        }
)

if (showPinDialog) {
    PinDialog(
        onConfirm = { enteredPin ->
            val isCorrect = securityUtils.verifyPin(enteredPin, storedPinHash)
            if (isCorrect) {
                onUnlock()
                navController.navigate(Routes.SETTINGS)
            }
            showPinDialog = false
        },
        onDismiss = { showPinDialog = false }
    )
}
```

---

## 16. Home Screen (`HomeScreen.kt`)

Layout:
- Top bar: "LockediN" title (left) + logo (right, the secret tap target)
- Study stats row: "Today: X min | Pomodoros: Y"
- Quick access grid (2×4): AI Chat, Calculator, Dictionary, Timer, Notes, Converter, Formulas, Files
- Bottom nav bar

Each grid card uses `ToolCard` component with icon + label.

---

## 17. File Hub (`FilesScreen.kt`)

- Displays all received homework files from Room database
- Each file shows: file type icon, file name, date received, file size
- Tap to open: PDF → PdfViewerScreen, image → ImageViewerScreen, text → in-app text viewer
- Swipe to delete (with confirmation dialog)
- Empty state: "No homework yet — share files from WhatsApp"
- Sort order: newest first

---

## 18. PDF Viewer (`PdfViewerScreen.kt`)

Use `barteksc/AndroidPdfViewer` via `AndroidView` in Compose:

```kotlin
AndroidView(
    factory = { ctx ->
        PDFView(ctx, null).apply {
            fromFile(File(filePath))
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .load()
        }
    },
    modifier = Modifier.fillMaxSize()
)
```

Show page number indicator. No share/export buttons.

---

## 19. Image Viewer (`ImageViewerScreen.kt`)

Use Coil's `AsyncImage` with pinch-to-zoom via `Modifier.graphicsLayer` + `transformable`.
No share/export/download buttons.

---

## 20. AI Chat (`AiChatScreen.kt` + `OpenAiService.kt`)

### System Prompt
```
You are a focused study assistant. Help the student understand concepts, 
solve homework problems, explain topics clearly. Do not engage in casual 
conversation unrelated to studying. Keep answers concise and educational.
```

### API Call
Use Retrofit to POST to `https://api.openai.com/v1/chat/completions`.  
Model: `gpt-4o-mini`. Max tokens: 1000.

API key stored in `local.properties` as `OPENAI_API_KEY`, accessed via `BuildConfig`.

### UI
- Message list (LazyColumn) with user bubbles (right, blue) and assistant bubbles (left, gray)
- Text input at bottom with send button
- Loading indicator while awaiting response
- Chat history persisted in Room (last 50 messages)
- "Clear chat" option in top bar

---

## 21. Scientific Calculator (`CalculatorScreen.kt`)

Build a fully functional calculator in Compose without any external library.

### Features:
- Standard operations: +, −, ×, ÷
- Advanced: √, x², xⁿ, log, ln, sin, cos, tan, π, e, %
- Parentheses support
- Expression display (show full expression, not just running total)
- History list (last 20 calculations, stored in memory/Room)
- Backspace and Clear (C / AC)

### Logic:
Use a recursive descent parser or the `javax.script.ScriptEngine` (Rhino engine available in Android API 26+) for evaluating expressions safely.

Display: large font for current expression, smaller font for history.

---

## 22. Dictionary (`DictionaryScreen.kt`)

### API
Use the **Free Dictionary API** — no API key required:
```
GET https://api.dictionaryapi.dev/api/v2/entries/en/{word}
```

### UI
- Search bar at top
- Results show: word, phonetic, part of speech, definitions (numbered), examples
- "No results" message for unknown words
- Recent searches (stored in memory, max 20)

### Retrofit service
```kotlin
interface DictionaryApiService {
    @GET("api/v2/entries/en/{word}")
    suspend fun lookupWord(@Path("word") word: String): List<DictionaryResponse>
}
```

Parse `meanings[].definitions[].definition` and `meanings[].definitions[].example`.

---

## 23. Pomodoro Timer (`TimerScreen.kt`)

### Logic
- Default: 25 min work, 5 min short break, 15 min long break (after 4 cycles)
- States: IDLE, WORKING, SHORT_BREAK, LONG_BREAK
- Countdown timer via `CountDownTimer` or coroutine + `delay`
- On session complete: play a soft notification sound + post a system notification (if permission granted)
- Track total study minutes in DataStore

### UI
- Large circular progress ring (Canvas-drawn) showing time remaining
- Current mode label: "Focus", "Short Break", "Long Break"
- Controls: Start / Pause / Reset
- Cycle counter: "Cycle 2 of 4"
- Study streak display

---

## 24. Quick Notes (`NotesScreen.kt`)

- List of notes, tappable to open full editor
- Floating action button to create new note
- Note editor: title field + multi-line content field
- Auto-save on every keystroke (500ms debounce)
- Notes stored in Room database
- Swipe-to-delete with undo snackbar
- No rich formatting needed — plain text only

---

## 25. Unit Converter (`ConverterScreen.kt`)

### Categories (use a tab row or dropdown)
- Length: mm, cm, m, km, in, ft, mi
- Mass: mg, g, kg, lb, oz
- Temperature: °C, °F, K
- Area: cm², m², km², ft², acre
- Volume: ml, L, fl oz, cup, gallon

All conversions done with pure Kotlin math — no library needed.
Two numeric fields + unit pickers (dropdown menus). Conversion is live as the user types.

---

## 26. Formula Sheet (`FormulaSheetScreen.kt`)

A static, scrollable reference screen organized in collapsible sections:

- **Algebra**: quadratic formula, exponent laws, logarithm laws
- **Geometry**: circle area/circumference, triangle area, Pythagoras, surface area and volume of common solids
- **Trigonometry**: sin/cos/tan definitions, identities, unit circle values
- **Calculus**: basic derivative rules, integral rules, chain rule
- **Physics**: Newton's laws, kinematic equations, Ohm's law, energy formulas
- **Chemistry**: molar mass, ideal gas law, molarity formula

Use LazyColumn with expandable `Section` composable per category.
Render formulas as styled text (subscripts/superscripts via `SpannableString` or Compose `BaselineShift`).

---

## 27. Settings Screen (`SettingsScreen.kt`)

**Accessible only after owner PIN unlock.**

Sections:
- **Lock Controls**: "Lock Device Now" button (re-enters kiosk mode) | "Device is Unlocked" status
- **PIN Management**: "Change PIN" → current PIN + new PIN + confirm new PIN
- **Accessibility Service**: Status chip + "Enable" button (deep-links to system accessibility settings)
- **Device Admin**: Status chip + "Enable" button (for true kiosk without user confirmation)
- **Files**: "Delete All Files" (with confirmation) | "Storage Used: X MB"
- **Chat**: "Clear Chat History" (with confirmation)
- **About**: App version, purpose description

---

## 28. ShareReceiverActivity.kt

```kotlin
@AndroidEntryPoint
class ShareReceiverActivity : AppCompatActivity() {

    @Inject lateinit var fileRepository: FileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleShareIntent(intent)
        finish() // Close this activity immediately after handling
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) ?: return
        val mimeType = intent.type ?: "*/*"

        lifecycleScope.launch {
            val saved = fileRepository.saveFileFromUri(uri, mimeType)
            if (saved) {
                Toast.makeText(
                    this@ShareReceiverActivity,
                    "Homework saved to LockediN ✓",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
```

### `FileRepository.saveFileFromUri()`
```kotlin
suspend fun saveFileFromUri(uri: Uri, mimeType: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val fileName = getFileNameFromUri(uri) ?: "${System.currentTimeMillis()}.file"
            val destDir = File(context.filesDir, "homework").also { it.mkdirs() }
            val destFile = File(destDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val entity = FileEntity(
                name = fileName,
                path = destFile.absolutePath,
                mimeType = mimeType,
                sizeBytes = destFile.length(),
                receivedAt = System.currentTimeMillis()
            )
            fileDao.insert(entity)
            true
        } catch (e: Exception) {
            false
        }
    }
}
```

---

## 29. Dependency Injection (`AppModule.kt`)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideOpenAiRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideDictionaryRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences =
        AppPreferences(context)

    @Provides @Singleton
    fun provideLockManager(appPreferences: AppPreferences): LockManager =
        LockManager(appPreferences)
}
```

---

## 30. UX & Design Rules

1. **No external browser** — never open a WebView that allows URL navigation. The AI Chat and Dictionary use direct API calls only.
2. **No system share sheet** — never call `Intent.ACTION_SEND` from within the app. Files can only go *in*, not *out*.
3. **No permission prompts during lock** — request all runtime permissions on first launch, during setup, before kiosk mode activates.
4. **Screens feel contained** — use a custom TopAppBar with no system navigation icons. Never show a hamburger menu that links outside the app.
5. **Dark mode support** — implement both light and dark Material3 themes.
6. **No splash screen delay** — kiosk mode activates in `onCreate`, before the UI even renders.
7. **Quiet notifications only** — Pomodoro timer may post a notification, but it should not allow the user to deep-link anywhere dangerous. The notification has no actions.

---

## 31. Owner Setup Instructions (Include in README.md)

After installing the app on the sister's device:

### Step 1 — Set Device Owner (One-time, from your laptop)

Connect the phone to a laptop with USB debugging enabled, then run:
```bash
adb shell dpm set-device-owner com.lockedin/.feature.lock.DeviceAdminReceiver
```

> This gives the app true kiosk authority. Without this, the student *may* be able to exit Screen Pinning by holding Back + Recent simultaneously. With this, they cannot.

> Note: If the device has Google accounts set up, remove them before running this command, or it will fail. Re-add them after.

### Step 2 — Enable Accessibility Service

On the phone:  
Settings → Accessibility → Downloaded Apps → LockediN Guard → Enable

### Step 3 — Launch LockediN

Set up your PIN on first launch. The device enters lockdown.

### Step 4 — How to Unlock Anytime

Tap the LockediN logo in the top-right corner **5 times in quick succession** → enter your PIN.

---

## 32. Edge Cases to Handle

| Scenario | Expected Behavior |
|----------|------------------|
| Student power-cycles the phone | BOOT_COMPLETED receiver relaunches app, re-locks |
| Student installs ADB on a computer and tries adb shell | Cannot exit lock task mode via ADB if Device Owner is set |
| App crashes | Android restarts it (set as default home app via manifest) |
| Internet is off | AI Chat shows "No connection" toast. Dictionary shows offline message. All other tools work. |
| File from WhatsApp is very large (>100MB) | Show progress toast while copying. Do not freeze the UI. |
| PIN forgotten | Owner must reinstall the app (ADB: `adb uninstall com.lockedin`) |
| Student calls someone | Phone and SMS apps are accessible during a phone call (OS behavior). This is acceptable — lock resumes when call ends. |

---

## 33. API Keys Setup

Store API keys in `local.properties` (never commit this file to git):
```
OPENAI_API_KEY=your_openai_key_here
```

Access in `build.gradle.kts`:
```kotlin
val localProperties = Properties()
localProperties.load(rootProject.file("local.properties").inputStream())

android {
    defaultConfig {
        buildConfigField("String", "OPENAI_API_KEY",
            "\"${localProperties["OPENAI_API_KEY"]}\"")
    }
}
```

Use in code:
```kotlin
val apiKey = BuildConfig.OPENAI_API_KEY
```

---

## 34. Final Checklist Before Delivery

- [ ] App launches and immediately enters Lock Task Mode — no visible delay
- [ ] Home button disabled in lock mode
- [ ] Back button disabled in lock mode
- [ ] Recent apps button disabled in lock mode
- [ ] Phone power-cycled → app relaunches and re-locks
- [ ] WhatsApp file shared to LockediN → file appears in File Hub
- [ ] PDF opens correctly with page scrolling
- [ ] Image opens full-screen with pinch-to-zoom
- [ ] AI Chat sends a message and gets a response
- [ ] Calculator handles all operations including √ and sin/cos/tan
- [ ] Dictionary returns definitions for common English words
- [ ] Pomodoro timer counts down and plays notification at 0:00
- [ ] Notes save automatically
- [ ] Unit converter converts correctly in all categories
- [ ] Formula sheet is complete and scrollable
- [ ] Logo 5-tap + correct PIN → unlocks and shows Settings
- [ ] Wrong PIN → dialog dismisses silently, no error shown
- [ ] Change PIN works in Settings
- [ ] "Lock Device" in Settings re-activates lock mode
- [ ] App is themed consistently in both light and dark mode
- [ ] No crashes on Android 8.0 (API 26) through Android 14 (API 34)

---

*End of specification. Build every section completely. Do not use TODOs.*
