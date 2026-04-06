# CLAUDE.md — Android Kotlin Course Project Guide
> **For Claude Code:** Read this file before writing any code, creating any files,
> or modifying any Gradle configuration in this project.

---

## 1. WHO THIS PROJECT IS FOR

This is a course project for **STEP IT Academy — Android Mobile Application Development (Kotlin + XML UI)**.

- **Instructor:** Magn
- **GitHub Org:** `chamkartech` / `chamkartechcambodia-sudo`
- **Course:** Android Kotlin — Batch 1 (D11–D34)

Each module has its own repo (`android-kotlin-m1-navigation`, `android-kotlin-m2-lifecycle`, etc.).
When creating a new project, use the exact Gradle configuration documented here.

---

## 2. VERIFIED BUILD ENVIRONMENT

These versions are confirmed working. **Do NOT upgrade without instructor approval.**

| Tool | Version |
|---|---|
| Android Gradle Plugin (AGP) | **9.0.1** |
| Gradle Wrapper | **9.2.1** |
| JDK (Toolchain) | **21** |
| Kotlin | Bundled with AGP 9.0.1 (do NOT declare separately) |
| Min SDK | 24 |
| Target SDK | **36** |
| Compile SDK | **36** |

---

## 3. CRITICAL PLUGIN RULES — READ BEFORE TOUCHING ANY build.gradle.kts

### ❌ RULE 1 — NEVER apply `kotlin.android` in `app/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)   // ← FORBIDDEN — causes build failure
}
```

### ✅ CORRECT — only `android.application` in `app/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    // kotlin.android is intentionally absent
    // AGP 9.0.1 registers the 'kotlin' extension internally.
    // Applying it explicitly causes: "Cannot add extension with name 'kotlin'"
}
```

**Why:** AGP 9.0.1 automatically configures the Kotlin extension when it detects
a Kotlin source set. Applying `kotlin.android` again causes a fatal conflict.

---

### ❌ RULE 2 — NEVER use `safeargs.kotlin` plugin variant:
```toml
# WRONG — .kotlin variant checks for Kotlin plugin explicitly
# AGP 9.0.1 does NOT register it in a detectable way → build fails
navigation-safeargs = { id = "androidx.navigation.safeargs.kotlin", version.ref = "navigation" }
```

### ✅ CORRECT — always use `safeargs` (without `.kotlin`):
```toml
# CORRECT — only checks for Android plugin, works with AGP 9.0.1
navigation-safeargs = { id = "androidx.navigation.safeargs", version.ref = "navigation" }
```

---

### ❌ RULE 3 — NEVER use `kotlinOptions` block (removed in AGP 9.0.1):
```kotlin
// FORBIDDEN — kotlinOptions DSL was removed in AGP 9.0.1
kotlinOptions {
    jvmTarget = "21"
}
```

### ✅ CORRECT — use only `compileOptions`, AGP 9.0.1 syncs Kotlin jvmTarget automatically:
```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
// No kotlinOptions needed — AGP 9.0.1 reads jvmTarget from compileOptions automatically
```

---

## 4. EXACT FILE CONTENTS — COPY VERBATIM

### 4.1 `gradle/wrapper/gradle-wrapper.properties`
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionSha256Sum=72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f
distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

### 4.2 `gradle.properties`
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

### 4.3 `gradle/libs.versions.toml` (base — always start from this)
```toml
[versions]
agp = "9.0.1"
coreKtx = "1.18.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
appcompat = "1.7.1"
material = "1.13.0"
activity = "1.13.0"
constraintlayout = "2.2.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-activity = { group = "androidx.activity", name = "activity", version.ref = "activity" }
androidx-constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
```

### 4.4 `build.gradle.kts` (root / top-level)
```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Do NOT add kotlin.android here — AGP handles it internally
}
```

### 4.5 `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ProjectNameHere"
include(":app")
```

> **Note on escape sequences:** In Kotlin string literals inside `.kts` files,
> use `\\.` not `\.` in regex strings (e.g., `"com\\.android.*"`).

### 4.6 `app/build.gradle.kts` (standard template)
```kotlin
plugins {
    alias(libs.plugins.android.application)
    // kotlin.android intentionally absent — AGP 9.0.1 registers it internally
}

android {
    namespace = "com.example.android.MODULENAME"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.android.MODULENAME"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        // AGP 9.0.1: kotlinOptions block removed — jvmTarget synced automatically from compileOptions
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

---

## 5. HOW TO ADD NEW LIBRARIES

**Always** add new dependencies to `gradle/libs.versions.toml` first,
then reference them via `libs.*` alias in `build.gradle.kts`.
Never paste a raw Maven coordinate directly into `build.gradle.kts`.

### Example: Adding Navigation Component

**Step 1 — Add to `libs.versions.toml`:**
```toml
[versions]
# ...existing versions...
navigation = "2.9.7"

[libraries]
# ...existing libraries...
androidx-navigation-fragment-ktx = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigation" }
androidx-navigation-ui-ktx = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigation" }

[plugins]
# ...existing plugins...
# ⚠️ Use "safeargs" NOT "safeargs.kotlin" — the .kotlin variant fails with AGP 9.0.1
navigation-safeargs = { id = "androidx.navigation.safeargs", version.ref = "navigation" }
```

**Step 2 — Apply plugin in `app/build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.navigation.safeargs)  // ← Add SafeArgs here (OK — not kotlin.android)
}
```

**Step 3 — Add SafeArgs classpath in root `build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.navigation.safeargs) apply false
}
```

**Step 4 — Add dependencies in `app/build.gradle.kts`:**
```kotlin
dependencies {
    // ...
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
}
```

---

## 6. LIBRARY VERSIONS (COURSE-APPROVED)

When adding libraries for course modules, use these exact versions.
All must go through `libs.versions.toml`.

| Library | Version | toml key |
|---|---|---|
| Navigation Component | **2.9.7** | `navigation` |
| Lifecycle (ViewModel + LiveData) | 2.9.0 | `lifecycle` |
| Room | 2.7.1 | `room` |
| Retrofit | 2.11.0 | `retrofit` |
| OkHttp (logging) | 4.12.0 | `okhttp` |
| Moshi | 1.15.2 | `moshi` |
| Glide | 4.16.0 | `glide` |
| Hilt | 2.56.1 | `hilt` |
| WorkManager | 2.10.1 | `workmanager` |
| DataStore | 1.1.4 | `datastore` |
| Coroutines | 1.10.2 | `coroutines` |
| Firebase BOM | 33.12.0 | `firebase-bom` |
| Timber | 5.0.1 | `timber` |

### Full `libs.versions.toml` with all course libraries:
```toml
[versions]
agp = "9.0.1"
coreKtx = "1.18.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
appcompat = "1.7.1"
material = "1.13.0"
activity = "1.13.0"
constraintlayout = "2.2.1"
navigation = "2.9.7"
lifecycle = "2.9.0"
room = "2.7.1"
retrofit = "2.11.0"
okhttp = "4.12.0"
moshi = "1.15.2"
glide = "4.16.0"
hilt = "2.56.1"
workmanager = "2.10.1"
datastore = "1.1.4"
coroutines = "1.10.2"
firebaseBom = "33.12.0"
timber = "5.0.1"

[libraries]
# Core
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-activity = { group = "androidx.activity", name = "activity", version.ref = "activity" }
androidx-constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }

# Navigation
androidx-navigation-fragment-ktx = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigation" }
androidx-navigation-ui-ktx = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigation" }

# Lifecycle
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
androidx-lifecycle-livedata-ktx = { group = "androidx.lifecycle", name = "lifecycle-livedata-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }

# Room
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Networking
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "retrofit" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
moshi-kotlin = { group = "com.squareup.moshi", name = "moshi-kotlin", version.ref = "moshi" }
moshi-kotlin-codegen = { group = "com.squareup.moshi", name = "moshi-kotlin-codegen", version.ref = "moshi" }

# Image Loading
glide = { group = "com.github.bumptech.glide", name = "glide", version.ref = "glide" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }

# WorkManager
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workmanager" }

# DataStore
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Coroutines
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Firebase (use BOM — no version on individual libraries)
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth-ktx = { group = "com.google.firebase", name = "firebase-auth-ktx" }
firebase-firestore-ktx = { group = "com.google.firebase", name = "firebase-firestore-ktx" }

# Logging
timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
# ⚠️ Use "safeargs" NOT "safeargs.kotlin" — the .kotlin variant fails with AGP 9.0.1
navigation-safeargs = { id = "androidx.navigation.safeargs", version.ref = "navigation" }
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

---

## 7. MODULE-BY-MODULE DEPENDENCY GUIDE

Only add what the module actually needs. Do not add all dependencies to every module.

### M1 — Navigation (D11–D12)
Plugins: `android.application`, `navigation.safeargs`
Dependencies: base + `navigation-fragment-ktx`, `navigation-ui-ktx`

### M2 — Lifecycle (D13–D14)
Plugins: `android.application`
Dependencies: base + `lifecycle-viewmodel-ktx`, `lifecycle-runtime-ktx`, `timber`

### M3 — Architecture / ViewModel + LiveData (D15–D16)
Plugins: `android.application`, `navigation.safeargs`
Dependencies: base + `lifecycle-viewmodel-ktx`, `lifecycle-livedata-ktx`, `navigation-*`
BuildFeatures: `dataBinding = true` (Udacity projects use DataBinding with `@{}`)

### M4 — Room (D17–D18)
Plugins: `android.application`, `ksp`
Dependencies: base + `lifecycle-*`, `room-runtime`, `room-ktx`
KSP: `room-compiler` (use `ksp` not `kapt`)

### M5 — RecyclerView (D19–D20)
Same as M4 — RecyclerView is already in `androidx.recyclerview` (part of appcompat/material)

### M6 — Networking (D21–D22)
Plugins: `android.application`, `navigation.safeargs`, `ksp`
Dependencies: base + `lifecycle-*`, `navigation-*`, `retrofit`, `retrofit-converter-moshi`,
             `okhttp-logging`, `moshi-kotlin`, `glide`
KSP: `moshi-kotlin-codegen`

### M7 — Hilt (D23)
Plugins: `android.application`, `hilt.android`, `ksp`
Root plugins: add `hilt.android apply false`, `ksp apply false`
Dependencies: base + `hilt-android`
KSP: `hilt-compiler`

### M8 — Background (D24–D26)
Plugins: `android.application`, `hilt.android`, `ksp`
Dependencies: base + `hilt-*`, `work-runtime-ktx`, `datastore-preferences`, `kotlinx-coroutines-android`

### M9 — Firebase (D27–D28)
Plugins: `android.application`, `hilt.android`, `ksp`
Dependencies: base + `hilt-*`, `firebase-bom` (platform), `firebase-auth-ktx`, `firebase-firestore-ktx`
Requires: `google-services.json` in `/app` folder (instructor provides)
Root classpath: `com.google.gms:google-services:4.4.2`

### M10 — Polish / Testing (D29–D30)
Same as M6/M7 + testing libs

---

## 8. ROOM — USE KSP, NOT KAPT

For Room (and Moshi codegen and Hilt), use **KSP** (Kotlin Symbol Processing),
not the deprecated `kapt`.

**Step 1 — Add to root `build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false          // ← add this
}
```

**Step 2 — Add to `libs.versions.toml`:**
```toml
[versions]
ksp = "2.1.20-1.0.32"   # Must match kotlin version bundled with AGP

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

**Step 3 — Use in `app/build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)        // ← ksp(), not kapt()
}
```

---

## 9. ANDROID MANIFEST RULES

### Android 11+ (API 30+) Implicit Intent / Sharing
When using `ACTION_SEND` or any implicit intent, declare `<queries>` in `AndroidManifest.xml`.
`<queries>` is a **sibling** of `<application>`, NOT nested inside it.

```xml
<manifest>
    <queries>
        <intent>
            <action android:name="android.intent.action.SEND" />
            <data android:mimeType="text/plain" />
        </intent>
    </queries>

    <application
        android:allowBackup="true"
        ...>
    </application>
</manifest>
```

---

## 10. FORBIDDEN PATTERNS — NEVER GENERATE THESE

| Forbidden | Use Instead |
|---|---|
| `@Composable` / `setContent {}` | XML layouts + View Binding |
| `SharedPreferences` | `DataStore Preferences` |
| `AsyncTask` | Coroutines + `viewModelScope` |
| `Picasso` | `Glide` |
| `Volley` | `Retrofit` |
| `ListView` / `ArrayAdapter` | `RecyclerView` + `ListAdapter` + `DiffUtil` |
| `findViewById()` in new code | View Binding |
| `kapt` for Room/Hilt/Moshi | `ksp` |
| `kotlin.android` plugin in `app/build.gradle.kts` | Remove it — AGP handles it |
| `kotlinOptions { jvmTarget = "21" }` | Remove entirely — AGP 9.0.1 syncs from `compileOptions` |
| `safeargs.kotlin` plugin variant in toml | Use `androidx.navigation.safeargs` (no `.kotlin`) |
| Raw Maven coords in `build.gradle.kts` | Always via `libs.versions.toml` |
| String keys for navigation args | `SafeArgs` |
| Business logic in Fragment/Activity | Move to ViewModel + Repository |
| `observe(this, Observer { })` in Fragment | Use `observe(viewLifecycleOwner) { }` |
| Udacity/AOSP copyright headers in source files | Remove all `/* Copyright ... */` blocks |

---

## 11. PROJECT STRUCTURE TEMPLATE

```
android-kotlin-mX-MODULENAME/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/android/MODULENAME/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ui/
│   │   │   │   │   └── SCREENNAME/
│   │   │   │   │       ├── SCREENNAMEFragment.kt
│   │   │   │   │       └── SCREENNAMEViewModel.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── repository/
│   │   │   │   │   ├── local/         (Room: Entity, Dao, Database)
│   │   │   │   │   └── remote/        (Retrofit: ApiService, response models)
│   │   │   │   └── di/                (Hilt modules — only from M7 onward)
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   └── fragment_SCREENNAME.xml
│   │   │   │   ├── navigation/
│   │   │   │   │   └── nav_graph.xml  (only from M1 onward)
│   │   │   │   └── values/
│   │   │   │       ├── strings.xml
│   │   │   │       ├── colors.xml
│   │   │   │       └── themes.xml
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle.kts              ← root
├── gradle.properties
├── settings.gradle.kts
├── docs/
│   └── diagrams/                 ← architecture diagrams (.png)
└── README.md
```

---

## 12. SAFE ARGS — ARGUMENT DECLARATION RULE

SafeArgs `<argument>` is declared on the **receiver** (destination) fragment,
not the sender fragment.

```xml
<!-- nav_graph.xml -->
<fragment
    android:id="@+id/gameWonFragment"
    android:name="com.example.android.navigation.GameWonFragment"
    android:label="@string/won">

    <!-- Argument declared on DESTINATION, not on the action -->
    <argument
        android:name="numQuestions"
        app:argType="integer" />
    <argument
        android:name="numCorrect"
        app:argType="integer" />
</fragment>
```

---

## 13. VIEW BINDING — FRAGMENT PATTERN

Always null the binding in `onDestroyView()` to prevent memory leaks.

```kotlin
class ExampleFragment : Fragment() {

    private var _binding: FragmentExampleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExampleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use binding here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null   // ← REQUIRED — prevents memory leak
    }
}
```

---

## 14. GIT WORKFLOW

### Branch naming per module
```
main                                      ← complete instructor solution (fresh history)
Step.XX-Exercise-<topic>                  ← student exercise branch (has TODOs)
Step.XX-Solution-<topic>                  ← reference answer branch
```

### Commit convention
```
feat: implement login screen
fix: resolve crash in GameFragment
refactor: improve ViewModel logic
docs: update README with screenshots
test: add unit test for GameViewModel
chore: migrate build config to AGP 9.0.1
chore: Step.XX-Exercise-<topic>
```

### TODO comment format in starter code
```kotlin
// TODO 1: Find NavController using KTX extension
// val navController = ...

// TODO 2: Set up ActionBar with NavigationUI
// NavigationUI.setupActionBarWithNavController(...)
```

---

## 15. COMMON BUILD ERRORS AND FIXES

| Error | Cause | Fix |
|---|---|---|
| `Cannot add extension with name 'kotlin'` | `kotlin.android` applied in `app/build.gradle.kts` | Remove `alias(libs.plugins.kotlin.android)` from `app/build.gradle.kts` |
| `Unresolved reference 'kotlinOptions'` | AGP 9.0.1 removed `kotlinOptions {}` block entirely | Delete the entire `kotlinOptions { }` block — `compileOptions` with `VERSION_21` is sufficient |
| `safeargs plugin must be used with android plugin` | `.kotlin` variant checks for Kotlin plugin; AGP 9.0.1 doesn't register it explicitly | Change to `androidx.navigation.safeargs` (drop `.kotlin`) in `libs.versions.toml` |
| `Invalid Gradle JDK configuration found` | Android Studio points to wrong JDK | `File → Settings → Build Tools → Gradle → Gradle JDK` → select **Embedded JDK** |
| `Unresolved reference: libs` | Gradle cache issue or wrong file | Run `./gradlew --stop` then sync |
| `\.` in regex warning | Kotlin DSL requires `\\.` | Change `"com\.android.*"` to `"com\\.android.*"` in `settings.gradle.kts` |
| `resolveActivity() returns null` on API 30+ | Missing `<queries>` in manifest | Add `<queries>` as sibling of `<application>` in `AndroidManifest.xml` |
| `NavigationUI.navigateUp()` wrong behavior | Arguments in wrong order | Correct order: `navigateUp(navController, drawerLayout)` |
| Room compile error | Using `kapt` instead of `ksp` | Replace `kapt(libs.androidx.room.compiler)` with `ksp(libs.androidx.room.compiler)` |
| `popUpTo` not working on back press | Misunderstanding when it runs | `popUpTo` runs at **navigate time**, not at back-press time |
| `createNavigateOnClickListener()` crash | Called inside lambda | Pass it directly: `setOnClickListener(directions.actionX.createNavigateOnClickListener())` |
| `Unresolved reference: BR` | DataBinding used but not enabled | Set `dataBinding = true` in `buildFeatures` |

---

*Last updated: April 2026 — STEP IT Academy · Android Kotlin Course (XML UI) · Instructor Magn*
