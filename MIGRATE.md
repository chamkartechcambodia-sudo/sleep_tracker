# MIGRATE.md — Udacity Project Migration Guide
> **For Claude Code:** This file tells you exactly how to migrate a Udacity Android
> project to the STEP IT Academy build configuration.
>
> **Always read `CLAUDE.md` first** before starting any migration.
> This file only covers the migration workflow — all config rules live in `CLAUDE.md`.

---

## OVERVIEW

Udacity course projects (ud9012) use an outdated Gradle build setup:
- Old `build.gradle` (Groovy DSL, not KTS)
- AGP 3.x / 4.x with outdated dependency declarations
- `kotlin-android-extensions` (deprecated — replaced by View Binding)
- `kapt` instead of `ksp`
- `kotlinOptions { jvmTarget }` (removed in AGP 9.0.1)
- Hardcoded version numbers instead of version catalog
- Old `compileSdkVersion` / `targetSdkVersion` integer syntax
- Udacity/AOSP copyright headers in every source file

Udacity source code also uses **deprecated/removed Kotlin & AndroidX APIs** that must be fixed:
- `NavHostFragment.findNavController(fragment)` → removed, use `findNavController()` extension
- `Transformations.map { }` → removed in Lifecycle 2.9.0, use `liveData.map { }` extension
- `ViewModelProvider.Factory.create<T : ViewModel?>` → nullable removed in Lifecycle 2.9.0
- `observe(owner, Observer { })` → use lambda syntax `observe(owner) { }`

**Goal:** Keep 100% of the original Kotlin source code and XML resources.
Only replace the build system files and clean up incompatible patterns.
The app must behave identically after migration.

---

## WHAT YOU KEEP (never touch these)

```
app/src/main/java/**/*.kt          ← ALL Kotlin source files — keep logic, only fix patterns
app/src/main/res/**                ← ALL resources (layouts, drawables, nav graphs, strings)
app/src/main/AndroidManifest.xml   ← Keep, only add <queries> if needed for API 30+
app/src/test/**                    ← Test files — keep as-is
app/src/androidTest/**             ← Instrumented tests — keep as-is
app/proguard-rules.pro             ← Keep as-is
screenshots/                       ← Keep as-is (used in README)
```

---

## WHAT YOU REPLACE (migration targets)

```
build.gradle              → build.gradle.kts        (root)
app/build.gradle          → app/build.gradle.kts    (app module)
settings.gradle           → settings.gradle.kts
gradle.properties                                    (update content)
gradle/wrapper/gradle-wrapper.properties             (update versions)
                          → gradle/libs.versions.toml  (new file — create)
README.md                                            (replace with STEP IT Academy content)
```

---

## STEP-BY-STEP MIGRATION WORKFLOW

### STEP 0 — Preparation

Before anything else, read the existing build files and make a checklist:

```bash
cat build.gradle
cat app/build.gradle
cat settings.gradle
```

Note which features are active in the Udacity project:
- [ ] Does it use DataBinding? (`dataBinding { enabled = true }` or `@{}` in XML)
- [ ] Does it use Navigation SafeArgs? (`apply plugin: 'androidx.navigation.safeargs.kotlin'`)
- [ ] Does it use Room? (`apply plugin: 'kotlin-kapt'` + room dependencies)
- [ ] Does it use Hilt / Retrofit / Glide?
- [ ] What is the current minSdk?
- [ ] Does source code have `observe(this, Observer { })` patterns? → fix to `observe(viewLifecycleOwner) { }`
- [ ] Does source code have `observe(owner, Observer { })` patterns? → fix to `observe(owner) { }`
- [ ] Does source code have `NavHostFragment.findNavController(this)` calls? → fix to `findNavController()`
- [ ] Does source code have `Transformations.map(liveData) { }` calls? → fix to `liveData.map { }`
- [ ] Does source code have `ViewModelProvider.Factory` with `<T : ViewModel?>` signature? → fix to `<T : ViewModel>`
- [ ] Does source code have `kotlinx.android.synthetic` imports? (need replacing with ViewBinding)

---

### STEP 1 — Create the destination project folder

Copy the entire Udacity project to a new folder with the STEP IT Academy naming convention:

```bash
# Windows PowerShell
Copy-Item -Recurse -Force "SOURCE_PATH\udacity-project-name" "DEST_PATH\android-kotlin-mX-projectname"

# Then delete the old .git folder from destination — we want fresh history
Remove-Item -Recurse -Force "DEST_PATH\android-kotlin-mX-projectname\.git"
```

---

### STEP 2 — Delete old Groovy build files from destination

```bash
Remove-Item "DEST_PATH\build.gradle"
Remove-Item "DEST_PATH\app\build.gradle"
Remove-Item "DEST_PATH\settings.gradle"
# Keep gradle.properties — you will overwrite its content
# Keep gradle-wrapper.properties — you will overwrite its content
```

---

### STEP 3 — Create `gradle/wrapper/gradle-wrapper.properties`

**Overwrite completely:**

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

---

### STEP 4 — Update `gradle.properties`

**Overwrite completely:**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

---

### STEP 5 — Create `settings.gradle.kts`

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

rootProject.name = "PROJECT_NAME_HERE"   // ← use the STEP IT Academy project name
include(":app")
```

---

### STEP 6 — Create `gradle/libs.versions.toml`

Start from the base catalog in `CLAUDE.md` Section 6 (full catalog with all course libraries).
Then **remove** the `[libraries]` entries for libraries this specific Udacity project does NOT use.
Keep the `[versions]` section complete (it doesn't hurt to have unused versions).

**Decision table — which libraries to include:**

| Udacity project uses... | Include in toml |
|---|---|
| Navigation Component | `navigation-fragment-ktx`, `navigation-ui-ktx` |
| SafeArgs | also add `navigation-safeargs` to `[plugins]` — ⚠️ use `androidx.navigation.safeargs` NOT `safeargs.kotlin` |
| DataBinding | nothing extra — DataBinding is a `buildFeatures` flag, not a dependency |
| ViewModel + LiveData | `lifecycle-viewmodel-ktx`, `lifecycle-livedata-ktx` |
| Room | `room-runtime`, `room-ktx`, `room-compiler` + add `ksp` plugin |
| Retrofit | `retrofit`, `retrofit-converter-moshi`, `okhttp-logging` |
| Moshi | `moshi-kotlin`, `moshi-kotlin-codegen` + add `ksp` plugin |
| Gson | `retrofit-converter-gson` (only if project uses `GsonConverterFactory`) |
| Glide | `glide` |
| Hilt | `hilt-android`, `hilt-compiler` + `hilt-android` plugin + `ksp` plugin |
| WorkManager | `work-runtime-ktx` |
| DataStore | `datastore-preferences` |
| Timber | `timber` |
| Firebase | `firebase-bom`, `firebase-auth-ktx`, `firebase-firestore-ktx` |
| Coroutines | `kotlinx-coroutines-android` |

**⚠️ Critical — SafeArgs plugin ID:**
```toml
# WRONG — causes "safeargs plugin must be used with android plugin" error on AGP 9.0.1
navigation-safeargs = { id = "androidx.navigation.safeargs.kotlin", version.ref = "navigation" }

# CORRECT
navigation-safeargs = { id = "androidx.navigation.safeargs", version.ref = "navigation" }
```

**Special case — Gson vs Moshi:**
Udacity ud9012 uses Gson in some projects. If the original project uses `GsonConverterFactory`, keep Gson:
```toml
[libraries]
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
gson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }
```

---

### STEP 7 — Create root `build.gradle.kts`

**Base (no extra plugins):**
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
}
```

**If project uses SafeArgs:**
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.navigation.safeargs) apply false
}
```

**If project uses Room or Moshi codegen (without Hilt):**
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false
}
```

**If project uses Hilt:**
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
}
```

---

### STEP 8 — Create `app/build.gradle.kts`

Build it section by section.

#### 8.1 Plugins block

```kotlin
plugins {
    alias(libs.plugins.android.application)
    // ❌ DO NOT add kotlin.android — AGP 9.0.1 handles it internally
    // Only add these if the project actually needs them:
    // alias(libs.plugins.navigation.safeargs)   // if project uses SafeArgs
    // alias(libs.plugins.ksp)                   // if project uses Room/Hilt/Moshi codegen
    // alias(libs.plugins.hilt.android)          // if project uses Hilt
}
```

#### 8.2 android block

```kotlin
android {
    namespace = "com.example.android.PACKAGENAME"  // ← match original package name
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.android.PACKAGENAME"
        minSdk = 24         // use original minSdk if it was higher
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
        // ❌ DO NOT add kotlinOptions block — removed in AGP 9.0.1
        // jvmTarget is synced automatically from compileOptions
    }

    // ❌ NO kotlinOptions block here — it was removed in AGP 9.0.1

    buildFeatures {
        viewBinding = true
        // dataBinding = true    // ← add ONLY if original project uses DataBinding
                                 // (check for <layout> tags in XML and @{} expressions)
    }
}
```

#### 8.3 dependencies block

```kotlin
dependencies {
    // Core (always include)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Add the specific libraries this project needs (see STEP 6 table)
    // Navigation:
    // implementation(libs.androidx.navigation.fragment.ktx)
    // implementation(libs.androidx.navigation.ui.ktx)

    // Lifecycle:
    // implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // implementation(libs.androidx.lifecycle.livedata.ktx)
    // implementation(libs.androidx.lifecycle.runtime.ktx)

    // Room (use ksp, not kapt):
    // implementation(libs.androidx.room.runtime)
    // implementation(libs.androidx.room.ktx)
    // ksp(libs.androidx.room.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

---

### STEP 9 — Handle DataBinding vs View Binding

Check the original XML layouts:
```bash
grep -r "layout>" app/src/main/res/layout/
grep -r "@{" app/src/main/res/layout/
grep -r "@=" app/src/main/res/layout/
```

**If DataBinding is used (XML has `<layout>` root tags and `@{}` expressions):**
- Set `dataBinding = true` in `buildFeatures`
- Keep all `<layout>`, `<data>`, and `@{}` in XML files as-is
- Keep `DataBindingUtil.setContentView()` / `DataBindingUtil.inflate()` calls as-is

**If DataBinding is NOT used:**
- Set only `viewBinding = true`

**When in doubt, enable both:**
```kotlin
buildFeatures {
    viewBinding = true
    dataBinding = true
}
```

---

### STEP 10 — Fix source code issues after migration

After replacing build files, scan and fix these patterns in source code.

#### 10.1 Remove `kotlinx.android.synthetic` imports (deprecated)

```bash
grep -r "kotlinx.android.synthetic" app/src/
```

If found, replace with View Binding pattern (see CLAUDE.md Section 13).

#### 10.2 Fix `observe(this, Observer { })` in Fragments

Search:
```bash
grep -rn "observe(this" app/src/
grep -rn "observe(this," app/src/
```

**Before (wrong — uses Fragment itself as LifecycleOwner):**
```kotlin
import androidx.lifecycle.Observer
// ...
viewModel.score.observe(this, Observer { value ->
    binding.scoreText.text = value.toString()
})
```

**After (correct — uses viewLifecycleOwner):**
```kotlin
// import androidx.lifecycle.Observer  ← remove this import if no longer needed
// ...
viewModel.score.observe(viewLifecycleOwner) { value ->
    binding.scoreText.text = value.toString()
}
```

> **Rule:** In Fragments, ALWAYS use `viewLifecycleOwner` in `observe()` calls.
> Using `this` can cause crashes when the Fragment is detached but still observing.

#### 10.3 Fix namespace vs applicationId

```kotlin
android {
    namespace = "com.example.android.trivia"   // matches package in .kt files
    defaultConfig {
        applicationId = "com.example.android.trivia"
    }
}
```

#### 10.4 Room — replace kapt with ksp

```kotlin
// Before (old Groovy):
kapt "androidx.room:room-compiler:2.x.x"

// After (new KTS):
ksp(libs.androidx.room.compiler)
```

Also remove `apply plugin: 'kotlin-kapt'` — it is gone entirely.

#### 10.5 Fix `NavHostFragment.findNavController` (removed in Navigation 2.x)

Search:
```bash
grep -rn "NavHostFragment.findNavController" app/src/
```

**Before (static method — removed):**
```kotlin
import androidx.navigation.fragment.NavHostFragment.findNavController
// ...
findNavController(this).navigate(action)
```

**After (extension function on Fragment):**
```kotlin
import androidx.navigation.fragment.findNavController
// ...
findNavController().navigate(action)
```

#### 10.6 Fix `Transformations.map` (removed in Lifecycle 2.9.0)

Search:
```bash
grep -rn "Transformations" app/src/
```

**Before (`Transformations` class — removed in 2.9.0):**
```kotlin
import androidx.lifecycle.Transformations
// ...
val currentTimeString = Transformations.map(currentTime) { time ->
    DateUtils.formatElapsedTime(time)
}
```

**After (extension function from `lifecycle-livedata-ktx`):**
```kotlin
import androidx.lifecycle.map
// ...
val currentTimeString = currentTime.map { time ->
    DateUtils.formatElapsedTime(time)
}
```

> `lifecycle-livedata-ktx` must be in dependencies — see CLAUDE.md Section 7.

#### 10.7 Fix `ViewModelProvider.Factory` nullable signature (Lifecycle 2.9.0)

Search:
```bash
grep -rn "ViewModel?>" app/src/
```

**Before (nullable `ViewModel?` — removed in 2.9.0):**
```kotlin
override fun <T : ViewModel?> create(modelClass: Class<T>): T {
```

**After (non-nullable):**
```kotlin
override fun <T : ViewModel> create(modelClass: Class<T>): T {
```

#### 10.8 Fix `observe()` with explicit Observer class

Search:
```bash
grep -rn ", Observer {" app/src/
grep -rn ", Observer<" app/src/
```

**Before (verbose Observer class):**
```kotlin
import androidx.lifecycle.Observer
// ...
viewModel.score.observe(viewLifecycleOwner, Observer { value ->
    binding.scoreText.text = value.toString()
})
```

**After (Kotlin lambda — cleaner, no import needed):**
```kotlin
// Remove: import androidx.lifecycle.Observer
// ...
viewModel.score.observe(viewLifecycleOwner) { value ->
    binding.scoreText.text = value.toString()
}
```

---

### STEP 11 — Remove Udacity copyright headers

All Udacity source files contain an Apache License 2.0 copyright header.
**Remove them from every file** — they do not belong in STEP IT Academy repos.

#### 11.1 Kotlin files (`.kt`)

The block to remove looks like this (always at the very top of the file):
```kotlin
/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * ...
 */

```

After removal, the file must start directly with `package com.example.android...`.

#### 11.2 XML files (`.xml`)

The block to remove in XML files looks like this (appears after `<?xml version...?>`):
```xml
<!--
  ~ Copyright 2018, The Android Open Source Project
  ~ ...
  -->
```

After removal, the file must start directly with the root XML element.

#### 11.3 Files to check

Check ALL files under:
- `app/src/main/java/**/*.kt`
- `app/src/main/res/**/*.xml`
- `app/src/main/AndroidManifest.xml`
- `app/src/test/**/*.kt`
- `app/src/androidTest/**/*.kt`

---

### STEP 12 — Initialize fresh git history

The new repo must have **no Udacity commit history**.

```bash
# From inside the destination project folder:

# 1. Delete the copied .git folder (already done in STEP 1 if you followed it)
Remove-Item -Recurse -Force .git

# 2. Initialize fresh repo
git init

# 3. Set branch to main
git checkout -b main

# 4. Stage everything
git add .

# 5. Initial commit
git commit -m "feat: initial project setup — <ProjectName> (AGP 9.0.1)"

# 6. Add remote (chamkartechcambodia-sudo org)
git remote add origin https://github.com/chamkartechcambodia-sudo/<repo-name>.git
```

---

### STEP 13 — Create all exercise branches

Each Udacity step branch must be recreated in the new repo with:
- The step's source code (app/src) from the Udacity branch
- The new build files from `main` (never the old Groovy files)
- Copyright headers removed
- `observe()` pattern fixed
- A single fresh commit (no Udacity history)

#### 13.1 — List all Udacity step branches

```bash
# From inside the SOURCE (Udacity) project:
git branch -r | grep "origin/Step"
```

#### 13.2 — For each step branch, run this workflow:

```bash
# In SOURCE project: checkout the step branch
cd SOURCE_PROJECT
git checkout origin/Step.XX-Exercise-Topic --detach

# In DESTINATION project: create branch from main
cd DEST_PROJECT
git checkout main
git checkout -b Step.XX-Exercise-Topic

# Replace app/src with the step's source code
Remove-Item -Recurse -Force app\src
Copy-Item -Recurse -Force "SOURCE_PROJECT\app\src" "app\src"

# Fix: remove copyright headers from all .kt and .xml files under app/src
# Fix: replace observe(this, Observer{}) with observe(viewLifecycleOwner){} in Fragments
# Fix: remove unused "import androidx.lifecycle.Observer" after the above fix

# Commit
git add .
git commit -m "chore: Step.XX-Exercise-Topic"
```

Repeat for every Exercise and Solution branch.

#### 13.3 — Shortcut: automate with a script

```python
import subprocess, shutil, os, re

SOURCE = r"PATH\TO\udacity-project"
DEST   = r"PATH\TO\android-kotlin-mX-projectname"

# Get all Step branches from source project
result = subprocess.run("git branch -r", cwd=SOURCE, shell=True, capture_output=True, text=True)
branches = [
    line.strip().replace("origin/", "")
    for line in result.stdout.splitlines()
    if "Step." in line
]

# ── Kotlin fixes ──────────────────────────────────────────────────────────────

def remove_kt_copyright(content):
    """Remove /* Copyright ... */ block at top of file."""
    return re.sub(r"^/\*.*?\*/\s*\n", "", content, flags=re.DOTALL)

def fix_observe_this(content):
    """observe(this, Observer { → observe(viewLifecycleOwner) {"""
    return re.sub(
        r"\.observe\s*\(\s*this\s*,\s*Observer\s*\{",
        ".observe(viewLifecycleOwner) {",
        content
    )

def fix_observe_owner(content):
    """observe(anyOwner, Observer { → observe(anyOwner) {"""
    return re.sub(
        r"\.observe\s*\(([^,)]+),\s*Observer\s*\{",
        r".observe(\1) {",
        content
    )

def fix_observer_import(content):
    """Remove unused Observer import."""
    if "Observer {" not in content and "Observer<" not in content:
        content = content.replace("import androidx.lifecycle.Observer\n", "")
    return content

def fix_nav_host_find_nav(content):
    """NavHostFragment.findNavController(this) → findNavController()"""
    content = content.replace(
        "import androidx.navigation.fragment.NavHostFragment.findNavController",
        "import androidx.navigation.fragment.findNavController"
    )
    content = re.sub(r"findNavController\s*\(\s*this\s*\)", "findNavController()", content)
    return content

def fix_transformations_map(content):
    """Transformations.map(liveData) { → liveData.map {"""
    content = content.replace(
        "import androidx.lifecycle.Transformations",
        "import androidx.lifecycle.map"
    )
    # Transformations.map(someVar) → someVar.map
    content = re.sub(
        r"Transformations\.map\s*\((\w+)\)",
        r"\1.map",
        content
    )
    return content

def fix_viewmodel_factory(content):
    """<T : ViewModel?> → <T : ViewModel> in Factory create()"""
    return content.replace(
        "override fun <T : ViewModel?> create",
        "override fun <T : ViewModel> create"
    )

def fix_kt_file(path):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    content = remove_kt_copyright(content)
    content = fix_observe_this(content)
    content = fix_observe_owner(content)
    content = fix_observer_import(content)
    content = fix_nav_host_find_nav(content)
    content = fix_transformations_map(content)
    content = fix_viewmodel_factory(content)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)

# ── XML fixes ─────────────────────────────────────────────────────────────────

def fix_xml_file(path):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    content = re.sub(r"<!--\s*~?\s*Copyright.*?-->\s*\n?", "", content, flags=re.DOTALL)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)

# ── Branch loop ───────────────────────────────────────────────────────────────

def run(cmd, cwd):
    return subprocess.run(cmd, cwd=cwd, shell=True, capture_output=True, text=True)

for branch in branches:
    # Checkout source branch (detached)
    run(f"git checkout origin/{branch} --detach", SOURCE)
    # Create dest branch from main
    run("git checkout main", DEST)
    run(f"git checkout -b {branch}", DEST)
    # Replace app/src entirely
    shutil.rmtree(os.path.join(DEST, "app", "src"))
    shutil.copytree(os.path.join(SOURCE, "app", "src"), os.path.join(DEST, "app", "src"))
    # Apply all fixes
    for root, dirs, files in os.walk(os.path.join(DEST, "app", "src")):
        for fname in files:
            fpath = os.path.join(root, fname)
            if fname.endswith(".kt"):
                fix_kt_file(fpath)
            elif fname.endswith(".xml"):
                fix_xml_file(fpath)
    # Commit
    run("git add .", DEST)
    run(f'git commit -m "chore: {branch}"', DEST)
    print(f"Done: {branch}")

print("\nAll branches created.")
```

---

### STEP 14 — Write a proper README.md

Replace the Udacity README entirely. The README must contain:

```markdown
# <Project Name> — Android Kotlin M<X>

**STEP IT Academy** — Android Mobile Application Development (Kotlin + XML UI)
> Module X — <Topic>: <Key concepts>

---

## Screenshots
![screen0](screenshots/screen0.png) ![screen1](screenshots/screen1.png) ...

## About
<Game/app description>

## Architecture
- Pattern: MVVM
- Key concepts: <ViewModel, LiveData, DataBinding, Navigation, etc.>
- Architecture diagram (ASCII or image)

## Project Structure
<File tree with descriptions>

## Tech Stack
| Library | Version |
...

## Build Requirements
| Tool | Version |
...
JDK setup instructions

## How to Work with This Repo
Branch structure explanation + workflow per step

## Exercise Steps
| Step | Branch | Topic | Key Files |

## Navigation Flow
<ASCII diagram>

## Course Info
Instructor, org, batch
```

---

### STEP 15 — Verify the migration

```bash
./gradlew assembleDebug
```

**Common errors and fixes:**

| Error message | Cause | Fix |
|---|---|---|
| `Cannot add extension with name 'kotlin'` | `kotlin.android` applied in `app/build.gradle.kts` | Remove `alias(libs.plugins.kotlin.android)` |
| `Unresolved reference 'kotlinOptions'` | `kotlinOptions {}` block removed in AGP 9.0.1 | Delete the entire `kotlinOptions { }` block |
| `safeargs plugin must be used with android plugin` | `.kotlin` variant checks for Kotlin plugin, fails with AGP 9.0.1 | Change to `androidx.navigation.safeargs` (drop `.kotlin`) in `libs.versions.toml` |
| `Invalid Gradle JDK configuration found` | Android Studio points to wrong JDK | `File → Settings → Gradle → Gradle JDK` → select **Embedded JDK** |
| `Unresolved reference 'findNavController'` | Old `NavHostFragment.findNavController(fragment)` static method removed | Change import to `androidx.navigation.fragment.findNavController` and call `findNavController()` (no argument) |
| `Unresolved reference 'Transformations'` | `Transformations` class removed in Lifecycle 2.9.0 | Replace `import androidx.lifecycle.Transformations` with `import androidx.lifecycle.map`; change `Transformations.map(x) {` to `x.map {` |
| `'create' overrides nothing` in ViewModelFactory | `<T : ViewModel?>` nullable signature removed in Lifecycle 2.9.0 | Change to `<T : ViewModel>` (remove `?`) |
| `Unresolved reference: BR` | DataBinding used but not enabled | Set `dataBinding = true` in `buildFeatures` |
| `Unresolved reference: binding` | Missing ViewBinding inflate pattern | See CLAUDE.md Section 13 |
| `error: cannot find symbol` on Room entity | Room compiler not running | Check `ksp(libs.androidx.room.compiler)` in dependencies |
| `Plugin [id: 'androidx.navigation.safeargs'] was not found` | SafeArgs missing in root build file | Add `alias(libs.plugins.navigation.safeargs) apply false` to root `build.gradle.kts` |
| `Namespace not specified` | Missing namespace in android block | Add `namespace = "com.example.android.xxx"` to android block |

---

### STEP 16 — Push all branches to GitHub

```bash
# Push main branch
git push -u origin main

# Push all step branches at once
git push origin --all
```

---

## UDACITY PROJECT REFERENCE TABLE

Quick reference for each ud9012 project — what the migrated version needs.

| Udacity Project | Module | Key Libraries | DataBinding? | SafeArgs? | KSP? |
|---|---|---|---|---|---|
| Android Trivia | M1 — Navigation | Navigation | ✅ Yes | ✅ Yes | No |
| Dessert Pusher | M2 — Lifecycle | Lifecycle, Timber | No | No | No |
| Guess It | M3 — Architecture | Lifecycle, Navigation | ✅ Yes | ✅ Yes | No |
| Sleep Tracker | M4 — Room | Lifecycle, Navigation, Room, Coroutines | ✅ Yes | ✅ Yes | ✅ Room |
| Sleep Tracker | M5 — RecyclerView | same as M4 | ✅ Yes | ✅ Yes | ✅ Room |
| Mars Photos | M6 — Networking | Lifecycle, Navigation, Retrofit, Moshi, Glide | ✅ Yes | ✅ Yes | ✅ Moshi |
| DevByte | M8 — WorkManager | Lifecycle, Navigation, Retrofit, Room, WorkManager | ✅ Yes | No | ✅ Room |
| GDG Finder | M8 — DataStore | Lifecycle, Navigation, Retrofit, DataStore | ✅ Yes | ✅ Yes | No |

**Note on DataBinding:** All Udacity ud9012 projects use DataBinding. Always enable it.

---

## QUICK CHECKLIST

Use this before committing a migrated project:

```
□ Copied source project to new STEP IT Academy folder name
□ Deleted: .git folder from destination (fresh history)
□ Deleted: build.gradle (Groovy)
□ Deleted: app/build.gradle (Groovy)
□ Deleted: settings.gradle (Groovy)
□ Created: build.gradle.kts (KTS root)
□ Created: app/build.gradle.kts (KTS app)
□ Created: settings.gradle.kts
□ Created: gradle/libs.versions.toml
□ Updated: gradle-wrapper.properties → Gradle 9.2.1
□ Updated: gradle.properties → AndroidX, nonTransitiveRClass

□ Verified: kotlin.android plugin is NOT in app/build.gradle.kts
□ Verified: kotlinOptions block is NOT present anywhere
□ Verified: safeargs plugin uses "androidx.navigation.safeargs" (no .kotlin)
□ Verified: namespace is set in android block
□ Verified: compileSdk = 36, targetSdk = 36
□ Verified: compileOptions uses JavaVersion.VERSION_21
□ Verified: viewBinding = true (and dataBinding = true if needed)
□ Verified: ksp() used instead of kapt() for Room/Hilt/Moshi

□ Fixed: observe(this, Observer{}) → observe(viewLifecycleOwner) {} in Fragments
□ Fixed: observe(owner, Observer{}) → observe(owner) {} (all Observer lambda patterns)
□ Fixed: unused "import androidx.lifecycle.Observer" removed
□ Fixed: NavHostFragment.findNavController(this) → findNavController() extension
□ Fixed: import androidx.navigation.fragment.NavHostFragment.findNavController → androidx.navigation.fragment.findNavController
□ Fixed: Transformations.map(liveData) { } → liveData.map { } (Lifecycle 2.9.0)
□ Fixed: import androidx.lifecycle.Transformations → androidx.lifecycle.map
□ Fixed: ViewModelProvider.Factory create<T : ViewModel?> → create<T : ViewModel> (remove ?)
□ Fixed: no kotlinx.android.synthetic imports remain

□ Cleaned: ALL copyright headers removed from .kt files
□ Cleaned: ALL copyright headers removed from .xml files
□ Cleaned: ALL copyright headers removed from AndroidManifest.xml

□ Written: README.md replaced with STEP IT Academy content
□ Initialized: fresh git repo (git init → git checkout -b main → git add . → git commit)
□ Set remote: git remote add origin https://github.com/chamkartechcambodia-sudo/<repo>.git

□ Created: all Step.XX-Exercise branches (one per Udacity exercise branch)
□ Created: all Step.XX-Solution branches (one per Udacity solution branch)
□ Verified: each branch has fresh commit, no Udacity history
□ Verified: each branch uses new build files (from main), not old Groovy files
□ Verified: copyright headers removed on every branch

□ Verified: ./gradlew assembleDebug succeeds with no errors
□ Verified: app runs on emulator/device
□ Pushed: git push -u origin main && git push origin --all
□ Verified: git branch -r shows all expected branches on origin
```

---

*STEP IT Academy · Android Kotlin Course (XML UI) · Migration Guide · Instructor Magn*
*Last updated: April 2026*
