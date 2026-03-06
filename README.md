# Scripture Widgets â€” Android App

## Architecture Overview (Clean Architecture + MVVM)

```
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚  Presentation Layer (Jetpack Compose + ViewModels)        â”‚
â”‚  MainActivity Â· TodayScreen Â· BrowseScreen                â”‚
â”‚  FavoritesScreen Â· SettingsScreen Â· OnboardingScreen      â”‚
â”‚  ScriptureWidgets (Glance) Â· WorkerViewModels             â”‚
â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
â”‚  Domain Layer (Pure Kotlin)                               â”‚
â”‚  Models Â· VerseRepository interface Â· UseCases            â”‚
â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
â”‚  Data Layer                                               â”‚
â”‚  VerseRepositoryImpl Â· Room Database Â· Retrofit API       â”‚
â”‚  DataStore Â· BillingService                               â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

## Project Structure

```
app/src/main/
â”œâ”€â”€ java/com/scripturewidgets/
â”‚   â”œâ”€â”€ ScriptureApp.kt                     # Application class + Hilt + WorkManager
â”‚   â”œâ”€â”€ di/
â”‚   â”‚   â””â”€â”€ AppModule.kt                    # Hilt modules (DB, Network, Repository)
â”‚   â”œâ”€â”€ domain/
â”‚   â”‚   â”œâ”€â”€ model/Models.kt                 # BibleVerse, WidgetTheme, WidgetConfigâ€¦
â”‚   â”‚   â””â”€â”€ repository/VerseRepository.kt   # Repository interface
â”‚   â”œâ”€â”€ data/
â”‚   â”‚   â”œâ”€â”€ VerseRepositoryImpl.kt          # Offline-first impl (Room + Retrofit)
â”‚   â”‚   â”œâ”€â”€ PreferencesDataStore.kt         # DataStore for widget config & prefs
â”‚   â”‚   â”œâ”€â”€ billing/BillingService.kt       # Google Play Billing Library v7
â”‚   â”‚   â”œâ”€â”€ local/
â”‚   â”‚   â”‚   â”œâ”€â”€ ScriptureDatabase.kt        # Room DB + 120-verse seed data
â”‚   â”‚   â”‚   â”œâ”€â”€ dao/VerseDao.kt             # All SQL queries + favorites
â”‚   â”‚   â”‚   â””â”€â”€ entities/Entities.kt        # VerseEntity, FavoriteEntity
â”‚   â”‚   â””â”€â”€ remote/
â”‚   â”‚       â”œâ”€â”€ api/BibleApiService.kt      # Retrofit interface
â”‚   â”‚       â””â”€â”€ dto/Dtos.kt                 # API response models
â”‚   â”œâ”€â”€ presentation/
â”‚   â”‚   â”œâ”€â”€ MainActivity.kt                 # Single Activity + Navigation
â”‚   â”‚   â”œâ”€â”€ theme/Theme.kt                  # Material3 dynamic color theme
â”‚   â”‚   â”œâ”€â”€ viewmodel/ViewModels.kt         # Today/Browse/Favorites/Settings VMs
â”‚   â”‚   â”œâ”€â”€ screens/
â”‚   â”‚   â”‚   â”œâ”€â”€ today/TodayScreen.kt
â”‚   â”‚   â”‚   â”œâ”€â”€ Screens.kt                  # Browse + Favorites + Settings screens
â”‚   â”‚   â”‚   â””â”€â”€ onboarding/OnboardingScreen.kt
â”‚   â”‚   â””â”€â”€ widget/
â”‚   â”‚       â””â”€â”€ ScriptureWidgets.kt         # Glance widgets (Small/Medium/Large)
â”‚   â””â”€â”€ worker/
â”‚       â””â”€â”€ Workers.kt                      # DailyVerseWorker + BootReceiver
â””â”€â”€ res/
    â”œâ”€â”€ values/{strings,colors,themes}.xml
    â””â”€â”€ xml/{small,medium,large}_widget_info.xml
```

---

## Android Studio Setup (Step by Step)

### Step 1: Create the Project

1. Open **Android Studio Ladybug** (2024.2.x) or newer
2. **File â†’ New â†’ New Project â†’ Empty Activity**
3. Settings:
   - **Name:** `Scripture Widgets`
   - **Package:** `com.scripturewidgets`
   - **Save Location:** your folder
   - **Language:** Kotlin
   - **Minimum SDK:** API 26 (Android 8.0)
   - **Build config language:** Kotlin DSL (`.kts`)
4. Click **Finish**

---

### Step 2: Replace Gradle Files

Copy the provided files exactly:

| Provided file | Destination |
|---|---|
| `build.gradle.kts` (project) | Root `build.gradle.kts` |
| `gradle/libs.versions.toml` | `gradle/libs.versions.toml` |
| `app/build.gradle.kts` | `app/build.gradle.kts` |

Then sync: **File â†’ Sync Project with Gradle Files**

---

### Step 3: Copy Source Files

Copy all `.kt` files to the matching paths inside `app/src/main/java/com/scripturewidgets/`.
Copy all `res/` files to `app/src/main/res/`.
Replace `AndroidManifest.xml`.

---

### Step 4: API Key Setup

1. Register at **https://scripture.api.bible** (free)
2. Create an application to receive your API key
3. In `app/build.gradle.kts`, replace:
   ```kotlin
   buildConfigField("String", "BIBLE_API_KEY", "\"YOUR_API_KEY_HERE\"")
   ```
   with:
   ```kotlin
   buildConfigField("String", "BIBLE_API_KEY", "\"abc12345-your-actual-key\"")
   ```

> **Without an API key** the app works fully offline using the 120+ pre-seeded Room verses. The API key only enables live fetching.

---

### Step 5: Add Missing Resources

#### Notification icon (`res/drawable/ic_notification.xml`)
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"
    android:tint="#FFFFFF">
  <path android:fillColor="@android:color/white"
    android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z"/>
</vector>
```

#### Splash background color (`res/values/colors.xml` â€” already provided)

#### App Icon
Generate icons at **https://icon.kitchen** using a cross or Bible motif.
Place in `res/mipmap-*/ic_launcher.png` and `ic_launcher_round.png`.

#### Widget Preview layout (`res/layout/widget_preview.xml`)
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#614385"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="16dp">
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="&quot;For God so loved the worldâ€¦&quot;"
        android:textColor="#FFFFFF"
        android:textStyle="italic"
        android:textSize="14sp" />
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="John 3:16 (NIV)"
        android:textColor="#CCFFFFFF"
        android:textSize="11sp" />
</LinearLayout>
```

---

### Step 6: In-App Purchases Setup

1. Create app in **Google Play Console** (play.google.com/console)
2. Go to **Monetize â†’ Products â†’ In-app products**
3. Create these products (matching `ProductSku` in `BillingService.kt`):
   - `com.scripturewidgets.premiumthemes` â€” One-time, $1.99
   - `com.scripturewidgets.removeads` â€” One-time, $0.99
   - `com.scripturewidgets.yearly` â€” Subscription, $4.99/year
4. Add your test account under **Settings â†’ License testing**
5. Upload a signed APK/AAB to internal testing before billing will work

---

### Step 7: Package-Level Kotlinx Fix

Since `Screens.kt` contains multiple packages in one file (for brevity), split it into separate files:

- `BrowseScreen.kt` â†’ package `com.scripturewidgets.presentation.screens.browse`
- `FavoritesScreen.kt` â†’ package `com.scripturewidgets.presentation.screens.favorites`
- `SettingsScreen.kt` â†’ package `com.scripturewidgets.presentation.screens.settings`

---

## Key Dependency Notes

### Glance (Widget) Version
Make sure you''re using `glance-appwidget:1.1.0+`:
```toml
glance = "1.1.0"
```
This version includes `AppWidgetStateDefinition` and Compose-based backgrounds.

### Room + KSP
Use KSP (not KAPT) for Room code generation â€” it''s significantly faster:
```kotlin
ksp(libs.room.compiler)
```

### Hilt Workers
Don''t forget `@HiltAndroidApp` on `ScriptureApp` and `@AndroidEntryPoint` on `MainActivity`.
Workers require `@HiltWorker` + `@AssistedInject`.

---

## Testing Guide

### Emulator Testing

```bash
# Recommended emulator: Pixel 8, API 35
# In Android Studio: Tools â†’ Device Manager â†’ Create Device

# Run app:
./gradlew :app:installDebug

# Run unit tests:
./gradlew :app:test

# Run instrumented tests:
./gradlew :app:connectedAndroidTest
```

### Widget Testing

1. Run app on emulator or device
2. Long-press home screen â†’ **Widgets** â†’ scroll to **Scripture Widgets**
3. Drag widget to home screen â€” test all 3 sizes
4. Open app â†’ Settings â†’ change theme â†’ return to home screen to see update

### WorkManager Testing

```kotlin
// In your test or debug screen, trigger immediately:
WidgetRefreshWorker.enqueue(context)

// Or test the daily worker directly:
WorkManager.getInstance(context)
    .enqueue(OneTimeWorkRequestBuilder<DailyVerseWorker>().build())
```

### Billing Testing

Use Google''s test accounts and test SKUs:
- `android.test.purchased` â€” always succeeds
- `android.test.canceled` â€” always cancels
- `android.test.item_unavailable` â€” returns unavailable

---

## Google Play Store Submission

### Before Submission Checklist

- [ ] Replace `com.scripturewidgets` with your unique package name
- [ ] Replace `YOUR_API_KEY_HERE` with real key (or confirm offline works)
- [ ] Create signing keystore: `keytool -genkey -v -keystore release.jks ...`
- [ ] Configure signing in `app/build.gradle.kts`
- [ ] Generate signed AAB: **Build â†’ Generate Signed Bundle**
- [ ] Add `proguard-rules.pro` entries for Retrofit and Room
- [ ] Add Privacy Policy URL (required for Play Store)
- [ ] Prepare screenshots: phone (16:9), tablet (optional)
- [ ] Write 80-char short description and 4000-char long description
- [ ] Set content rating (Everyone/Rated for 3+)
- [ ] Set up Play Store listing categories: Lifestyle / Books & Reference

### Proguard Rules (`app/proguard-rules.pro`)
```
# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-dontwarn retrofit2.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.scripturewidgets.**$$serializer { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Hilt
-keep class dagger.hilt.** { *; }
```

---

## Architecture Decisions

### Why Offline-First?

Users add widgets to their home screen and expect them to show content even without internet. The 120+ pre-seeded verses ensure the widget ALWAYS has content. The API is purely additive.

### Why DataStore over SharedPreferences?

DataStore provides:
- Type-safe keys
- Coroutine-friendly async API
- Works with Kotlin Flows
- No `ANR` risk (no blocking main thread calls)

### Why Glance instead of RemoteViews?

Glance is the modern, Compose-based widget API. It:
- Uses familiar Compose syntax
- Handles state with `AppWidgetStateDefinition`
- Automatically handles widget size variants
- Type-safe, no XML layouts needed for widget content

### Why WorkManager over AlarmManager?

WorkManager:
- Survives device reboots (automatically)
- Respects Doze mode and battery optimization
- Handles backoff/retry automatically
- Integrates with Hilt via `@HiltWorker`

---

## Monetization Strategy

| Feature | Free | Premium ($4.99/yr) |
|---------|------|---------------------|
| All 120+ offline verses | âœ… | âœ… |
| Sunrise, Forest, Ocean, Midnight, Parchment themes | âœ… | âœ… |
| Cross, Lavender, Golden themes | âŒ | âœ… |
| Ads | Shown | Hidden |
| All categories | âœ… | âœ… |
| Daily notifications | âœ… | âœ… |

---

*Built with Jetpack Compose + Glance + Room + Hilt + WorkManager | Target: Android 8.0+ (API 26+)*
