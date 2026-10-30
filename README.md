# MotoGallery — Native Android Gallery Application

> I use a Motorola phone, and Google Photos is the default way I end up managing my pictures and videos. But I never really liked relying on Google Photos just to view, organize, or play files already stored on my phone.
>
> So I built MotoGallery — my own Android gallery app. No unnecessary cloud features, no accounts, no advertisements. Just my photos, my videos, and the tools I actually need.

---

[![Android CI](https://github.com/sohamdev77/MotoGallery/actions/workflows/android.yml/badge.svg)](https://github.com/sohamdev77/MotoGallery/actions/workflows/android.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-blue.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-orange.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.05.00-green.svg)](https://developer.android.com/jetpack/compose)

MotoGallery is a real, native Android gallery application crafted specifically for users who want complete ownership and privacy over their photos and videos. Built with Kotlin, Jetpack Compose, Material 3, MediaStore, Media3 ExoPlayer, Room, and hardware-backed Android Keystore cryptography, MotoGallery is 100% offline, lightning-fast, and designed with a premium dark-first Obsidian aesthetic tailored for modern OLED displays.

---

## Highlights & Features

### 1. Photo Gallery & Chronological Grid
- **MediaStore Discovery**: Automatically detects all accessible local images and videos on your phone without demanding root or raw filesystem privileges.
- **Fast Timeline Grid**: High-performance `LazyVerticalGrid` grouped by date ("Today", "Yesterday", "October 2026", etc.) that effortlessly navigates libraries with 20,000+ media items.
- **Dynamic Column Switcher**: Quick-switch between 2, 3, 4, or 5 grid columns with tap or pinch.
- **Fullscreen Viewer**: Smooth pinch-to-zoom (up to 5x), double-tap zoom (1x -> 2.5x -> 1x), clamped boundary panning, and seamless swiping between items via `HorizontalPager`.
- **Comprehensive EXIF Metadata**: Inspect camera make, model, lens, aperture ($f$/stop), shutter speed, ISO, focal length, resolution, GPS coordinates (with "View on Map"), and file path.
- **Multi-Selection Toolbar**: Long-press to activate bulk operations: share via system sharesheet, toggle favorite, copy, move, move to vault, or delete.
- **Scoped Storage Compliance**: Uses Android platform consent dialogs (`MediaStore.createDeleteRequest`, `MediaStore.createTrashRequest`) for public media. Never deletes files silently.

### 2. Video Gallery & Media3 ExoPlayer
- **Dedicated Videos Feed**: Browse all recorded and downloaded videos with duration badges and resolution markers.
- **Fullscreen Media3 ExoPlayer**: Hardware-accelerated video playback supporting portrait and auto-landscape orientation.
- **Intuitive Gesture Controls**:
  - **Left Screen Vertical Drag**: Live screen brightness adjustment.
  - **Right Screen Vertical Drag**: Media stream volume control.
  - **Double-Tap / Buttons**: Jump backward 10s or fast forward 10s.
- **Playback Speed Selector**: Choose between 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, and 2.0x.
- **Picture-in-Picture (PiP)**: Keep watching in floating window while navigating other apps.
- **Resume Playback**: Automatically remembers where you stopped watching.
- **Graceful Format Error Recovery**: Clean error banner with one-tap option to delegate unsupported codecs to external video players.

### 3. Encrypted Private Vault
- **Hardware-Backed Cryptography**: Actual authenticated encryption using **AES-256-GCM** with a 128-bit authentication tag and a unique 12-byte random IV per file.
- **Android Keystore Provider**: Cryptographic keys are protected in the device's hardware-backed Secure Element (TEE/StrongBox).
- **RAM-Only In-Memory Decryption**: Private photos and videos are decrypted strictly into volatile memory byte buffers for display and immediately garbage collected; decrypted files are **never** written to flash storage or cache disks.
- **Biometric & PIN Authentication**: Unlock with a 4–6 digit master PIN or fingerprint/face unlock via `BiometricPrompt`. Passwords are protected with 32-byte cryptographic salts and 10,000-round SHA-256 iterative stretching.
- **Verified Import Workflow**: MotoGallery encrypts the media, generates an encrypted thumbnail, and verifies decryption integrity **before** prompting the user to remove the public original via system permission dialog.
- **Automatic Background Lock**: Locks instantly or after a user-configured delay (Immediately, 30s, 1m, 5m) when the app is backgrounded or the screen sleeps.
- **Restore Media**: Seamlessly decrypt and restore private media back to public gallery folders (`Pictures/MotoGallery` or `Movies/MotoGallery`).

### 4. Recently Deleted & Dual-Mode Trash
- **Public MediaStore Trash**: Integrates with Android 11+ `MediaStore.createTrashRequest` with 30-day system retention.
- **App-Managed Vault Trash**: 30-day retention countdown badge for encrypted vault items. Automatically shreds expired files on startup.
- **Permanent Shredding**: Overwrites ciphertext bytes with random data before unlinking to prevent flash memory remnants.

### 5. Built-in Photo Editor
- **Transform**: Rotate 90° clockwise/counter-clockwise, flip horizontal, flip vertical, and fine straighten slider ($-45^\circ$ to $+45^\circ$).
- **Aspect Ratio Cropping**: Freeform, 1:1 (Square), 4:3, 16:9, and 9:16.
- **Live Color Adjustments**: Sliders ($-100$ to $+100$) for Brightness, Contrast, Saturation, Warmth (color temperature), and Shadows/Highlights.
- **ColorMatrix Preset Filters**: Original, Vivid, Moto Neon (signature violet/pink cyberpunk grade), Golden Hour, Cool Ice, Deep B&W, Sepia, Dramatic Noir.
- **Undo / Redo Stack**: Step back and forth through edits with full state history.
- **Non-Destructive Export**: Exports edited copies to `Pictures/MotoGallery/` as new files, keeping the original untouched.

### 6. Built-in Video Editor
- **Timeline Trimming**: Dual-handle slider to accurately trim start and end timestamps.
- **Audio Mute**: One-tap switch to strip audio tracks.
- **Hardware-Accelerated Stream Muxing**: Uses `MediaExtractor` and `MediaMuxer` for zero quality loss, fast exports, and low battery consumption.
- **Export Progress**: Real-time percentage indicator and cancellation support. Exports new clips to `Movies/MotoGallery/`.

### 7. Swipe Clean (Tinder-Style Cleanup)
- **Fluid Card Interface**: Review photos and videos one by one with smooth drag animations, spring physics, card rotation tilt, and haptic feedback.
- **Intuitive Gestures**:
  - **Swipe Left**: Queue media for trash with red indicator.
  - **Swipe Right**: Keep media with emerald indicator.
  - **Swipe Up**: Open instant fullscreen high-res preview.
  - **Tap Video**: Play / pause inline preview seamlessly with hardware-accelerated Media3 ExoPlayer.
  - **Undo**: Instantly reverse the previous action and restore the card stack.
  - **Tappable Controls**: Dedicated Keep, Trash, Preview, and Undo buttons for quick one-handed tapping.
- **Smart Cleanup Categories**:
  - **All Photos** & **All Videos**
  - **Screenshots** (automatically detects screenshot folders)
  - **Large Files** (files $\ge$ 25 MB)
  - **Old Media** (photos & videos taken over 6 months ago)
  - **Downloads** & **Camera Folder**
- **Advanced Sorting & Size Filters**: Sort by Newest, Oldest, Largest, or Smallest; filter by minimum file size (Any, 5 MB, 10 MB, 25 MB, 50 MB, 100 MB).
- **Live Storage Dashboard**: Real-time counter of reviewed, kept, and trashed items alongside actual recoverable disk space calculated from MediaStore file sizes (e.g. **1.42 GB potentially recoverable**).
- **Safe Scoped Storage Deletion**: Queues items during the session; prompts user with an itemized summary before invoking Android system consent dialogs (`MediaStore.createTrashRequest`). No files are ever silently deleted.
- **Strict Privacy Isolation**: Encrypted Private Vault media is strictly quarantined and never included in the swipe queue.

### 8. Built-in Wallpaper Themes (Whole-App Customization)
- **Settings → Appearance → Wallpaper Themes**: Choose from a curated gallery of built-in ambient wallpapers.
- **8 Bundled Offline Themes**:
  1. **Midnight Aurora**: Deep night sky illuminated by vivid emerald and purple aurora borealis.
  2. **Sakura Dream**: Serene blush pink and cherry blossom aesthetic.
  3. **Cyberpunk Neon**: High-contrast futuristic neon violet and electric blue.
  4. **AMOLED Black**: Minimalist pitch-black backdrop with crisp accents for maximum battery savings.
  5. **Ocean Depths**: Calming deep abyssal blue and turquoise underwater gradient.
  6. **Sunset Glow**: Warm twilight radiant orange, coral, and magenta.
  7. **Cosmic Galaxy**: Deep outer space nebula with celestial star fields.
  8. **Minimal Frost**: Soft neutral silver gradients and frosted glass aesthetics.
- **Whole-App Surface Integration**: Wallpaper renders cleanly across Photos, Albums, Videos, Swipe Clean, Recently Deleted, and Settings via frosted, semi-translucent Material 3 cards while preserving typography readability and WCAG accessibility contrast.
- **Customization Controls**:
  - **Adjustable Dimming Slider**: Dial in wallpaper dimming from 0% to 80% to balance art vs. readability.
  - **Theme Modes**: Dark, Light, or Follow System.
  - **Custom Accent Colors**: Pick between Violet, Cyan, Coral, Emerald, Sunset Gold, or Electric Pink.
  - **Instant Application**: Changes apply immediately across all screens without reloading or restarting the app.
  - **Persistent**: Preferences saved locally via Jetpack DataStore.
  - **Distraction-Free Media**: Fullscreen photo viewer and ExoPlayer video player automatically isolate media against a pure black canvas.

### 9. Halloween Fun Update (Seasonal Visuals & Easter Egg)
- **Festive Halloween Aesthetic**:
  - **Color Palette**: Signature Pumpkin Orange (`#FF7518`), Spooky Amber (`#FFA726`), Deep Eerie Obsidian (`#090608`), and eerie purple accent shadows (`#2B0E38`).
  - Warm spooky glows across navigation, cards, and interactive elements.
- **Ambient Halloween Decorations**:
  - **Cobweb Corners**: Delicate procedural spiderweb corner accents on top corners.
  - **Flying Bats Animation**: Lightweight, graceful bat silhouettes gliding smoothly across the top screen area.
- **Settings Pumpkin Easter Egg**:
  - A hidden, playful animated Jack-o'-lantern pops up in Settings with a creeping smile that widens into a cheeky carved grin, complete with candlelit interior glow!
  - Auto-dismisses smoothly after 2.6 seconds or on tap.
  - Session-aware: activates once per app session to avoid interruption.
- **Accessibility & Special Animations Toggles** (**Settings → Seasonal Theme / Special Effects**):
  - **Enable Halloween Theme**: Toggle seasonal colors on/off.
  - **Enable Special Animations**: Master motion toggle. When disabled, bats and animated popups are completely silenced while keeping the static theme intact.
  - **Enable Flying Bats**: Individual toggle for bat flight.
  - **Enable Pumpkin Easter Egg**: Individual toggle for the playful settings appearance.

---

## Interface Design & Visual Direction

MotoGallery is crafted with a clean, modern aesthetic featuring soft rounded corners, smooth gradients, and elegant surfaces inspired by the cosmic mountain visual language:

| Element | Hex Code | Purpose |
|---|---|---|
| **Obsidian Dark** | `#0B1020` | Root background (deep battery-saving black) |
| **Surface Dark** | `#131A2E` | Cards, navigation bars, dialog containers |
| **Surface Highlight** | `#1A233D` | Active pills, borders, selected states |
| **Violet Accent** | `#A855F7` | Primary brand accent & active indicators |
| **Pink Accent** | `#EC65B5` | Secondary accent & favorite heart badge |
| **Orange Accent** | `#FFA34D` | Tertiary accent & warning indicators |
| **Halloween Pumpkin** | `#FF7518` | Seasonal Halloween primary brand accent |
| **Halloween Amber** | `#FFA726` | Seasonal warm spooky glow & highlight |
| **Halloween Dark** | `#090608` | Seasonal eerie obsidian background |

- **6-Tab Navigation**: `Photos` | `Swipe` | `Albums` | `Videos` | `Private` | `Settings`
- **Dynamic Color Support**: Optional toggle in Settings to adapt colors to Android 12+ wallpaper themes.
- **Whole-App Built-in Wallpapers**: 8 bundled themes with real-time dimming and frosted glass surfaces.
- **Seasonal Halloween Mode**: Dedicated seasonal toggle layer with optional animations.
- **Optional Clean Light Theme**: For bright outdoor viewing.

---

## Architecture & Tech Stack

```
com.motogallery.app
├── data/
│   ├── local/          # Room DB (favorites, custom albums, vault entities, DAOs)
│   ├── mediastore/     # MediaStore cursor queries, reactive ContentObserver, ExifParser
│   ├── model/          # MediaItem, Album, VaultItem, TrashedMediaItem, FilterSortOptions
│   ├── preferences/    # DataStore / AppPreferences (theme, columns, auto-lock delay)
│   └── vault/          # VaultCryptoManager (AES-256-GCM), BiometricAuthManager, VaultLockManager
├── media/
│   ├── editor/         # PhotoEditorEngine, VideoEditorEngine, PhotoFilter
│   └── player/         # VideoPlayerManager (Media3 ExoPlayer wrapper)
├── ui/
│   ├── components/     # MotoTopBar, MotoBottomNavBar, MediaThumbnailCard, ZoomableImageView
│   ├── navigation/     # AppNavGraph, Screen sealed routes
│   ├── screens/        # Photos, Albums, Videos, Viewer, Vault, Trash, Editor, Settings
│   └── theme/          # Color tokens, Typography, MotoGalleryTheme
└── util/               # PermissionUtils, DateFormatter, FileSizeFormatter, IntentUtils
```

---

## Android Storage & Permissions Model

MotoGallery does not use unrestricted direct filesystem access or dangerous `MANAGE_EXTERNAL_STORAGE` permissions:
- **Android 14+ (API 34+)**: Supports `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, and `READ_MEDIA_VISUAL_USER_SELECTED` (Partial Media Access). Displays an in-app banner with a "Manage" button to select more photos or grant full access.
- **Android 13 (API 33)**: Supports granular `READ_MEDIA_IMAGES` and `READ_MEDIA_VIDEO`.
- **Android 8.0 – 12 (API 26–32)**: Uses compatible `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` (API $\le$ 28).
- **EXIF GPS**: Requests `ACCESS_MEDIA_LOCATION` on Android 10+ to view location data in the EXIF viewer.
- **Scoped Storage Consent**: When deleting, trashing, or renaming public media, MotoGallery launches the system `IntentSender` dialog (`MediaStore.createDeleteRequest`, `MediaStore.createTrashRequest`).

---

## Security & Vault Recovery Risks

> [!CAUTION]
> **Important Vault Backup Risk Disclosure**
> 
> Files stored in the Private Vault are encrypted with a device-unique master key generated in your phone's hardware Android Keystore.
> 
> Because this key is tied to your physical device's secure hardware chip:
> 1. **Uninstalling MotoGallery will permanently delete your encrypted vault files.**
> 2. **Factory resetting your device will erase the hardware keystore keys, making encrypted vault files permanently unrecoverable.**
> 3. Cloud backups do not and cannot export hardware-backed private keys.
> 
> **Always restore your private photos and videos back to the public gallery before uninstalling the application or switching devices.**

---

## Installation Guide (Motorola & Android Devices)

### Option A: Download Pre-Built APK from GitHub Releases
1. Open the repository's **Releases** page on your Motorola phone browser.
2. Under the latest release, download `MotoGallery-v1.2.0-debug.apk`.
3. Open the downloaded `.apk` from your notification shade or Downloads folder.
4. When prompted by Android ("For your security, your phone is not allowed to install unknown apps from this source"), tap **Settings** and toggle **Allow from this source**.
5. Tap **Install** and launch **MotoGallery**.

### Option B: Download Build Artifact from GitHub Actions
1. Navigate to the **Actions** tab in this repository.
2. Click on the latest workflow run on `main`.
3. Scroll down to the **Artifacts** section and download `MotoGallery-Debug-APK.zip`.
4. Extract the zip file and transfer `MotoGallery-v1.2.0-debug.apk` to your phone via USB or Google Drive / Files.

---

## Building from Source

### Prerequisites
- JDK 17 (Temurin 17 or OpenJDK 17)
- Android SDK with Platform `android-34` and Build Tools `34.0.0`
- Gradle 8.7 (bundled via `./gradlew`)

### Build Commands
```bash
# Clone the repository
git clone https://github.com/sohamdev77/MotoGallery.git
cd MotoGallery

# Run automated unit tests
./gradlew test

# Assemble debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/MotoGallery-v1.2.0-debug.apk
```

---

## Quality Assurance & Motorola Device Test Checklist

The following manual test checklist is verified on Motorola devices (Moto G series, Motorola Edge series):

- [x] **New Visual Language & App Icon**: High-resolution galaxy mountain squircle launcher icons properly rendered across all density buckets (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) and adaptive icon masks.
- [x] **Halloween Seasonal Theme**: Vibrant `#FF7518` pumpkin orange and deep `#090608` obsidian theme activates cleanly across all gallery feeds, albums, videos, swipe clean, recently deleted, and settings.
- [x] **Decorative Flying Bats & Cobwebs**: Procedural cobweb corners render gracefully; lightweight animated bats float across screen headers without impacting scroll performance or intercepting touches.
- [x] **Settings Pumpkin Easter Egg**: Scary smiling pumpkin pops up with an animated widening grin and warm candlelit glow on initial entry, auto-dismissing after 2.6s or upon tap.
- [x] **Reduced Motion & Accessibility Controls**: Toggling off "Special Animations" instantly silences flying bats and pumpkin popup while preserving static Halloween theming.
- [x] **Offline Reliability**: App functions with zero network connectivity in Airplane Mode. No crashes, no network timeouts, no analytics polling.
- [x] **Screen Rotation**: Fullscreen video player and photo editor retain playback state, zoom level, and adjustments across 90° and 180° orientation changes.
- [x] **Background Auto-Lock**: Vault immediately locks when switching to the home screen or another app. PIN or fingerprint is required upon return.
- [x] **Biometric Authentication**: Fingerprint unlock immediately unlocks vault; cancelling falls back to numeric PIN keypad.
- [x] **Large Library Performance**: Scrolling through 5,000+ images remains fluid at 60/120Hz; memory cache is constrained to 25% heap to prevent OutOfMemoryErrors.
- [x] **Partial Media Access (Android 14)**: Granting only selected photos displays the permission banner with working "Manage" button.
- [x] **Public Original Deletion Consent**: Importing media to vault proposes deletion of the public original via system consent dialog without deleting files silently.
- [x] **Non-Destructive Editing**: Exporting an edited photo or trimmed video generates a new file in `Pictures/MotoGallery` or `Movies/MotoGallery` without altering the original.
- [x] **Swipe Clean Gestures**: Card dragging tilts with spring physics; left-swipe queues for trash with red badge, right-swipe keeps with emerald badge, up-swipe triggers fullscreen preview.
- [x] **Swipe Clean Inline Video**: Tapping cards with video files toggles smooth ExoPlayer preview playback inline without UI lag.
- [x] **Swipe Clean Live Dashboard**: Accurately aggregates items reviewed, kept, trashed, and total recoverable storage calculated from raw byte sizes.
- [x] **Swipe Clean Scoped Storage Confirmation**: Prompts with confirmation dialog and system consent (`createTrashRequest`) upon completing or finishing a session.
- [x] **Wallpaper Themes Customization**: Selecting any of the 8 bundled themes updates background immediately across all main tabs and settings screens with translucent frosted surfaces.
- [x] **Wallpaper Dimming & Modes**: Dimming slider adjusts scrim dynamically (0–80%); dark/light/system modes and custom accent colors persist across app process kills via DataStore.

---

## Changelog

### v1.2.0 — Swipe Clean + Wallpaper Themes + Halloween Fun Update (Current)
- **New App Icon & Visual Inspiration**:
  - Replaced legacy launcher icons with a custom high-definition galaxy mountain visual language.
  - Deep cosmic nebula sky, glowing full moon, and frosted mountain shapes inside a neon squircle.
  - Complete Android adaptive icon support across `mipmap-anydpi-v26` and all pixel density buckets (`mdpi` through `xxxhdpi`).
- **Halloween Fun Update (Seasonal Visual Mode)**:
  - Curated Halloween palette: Pumpkin Orange (`#FF7518`), Spooky Amber (`#FFA726`), Eerie Obsidian Dark (`#090608`), and eerie purple accent shadows.
  - Ambient decorations: procedural canvas cobweb corners and floating nocturnal bat silhouettes gliding smoothly across screen headers.
  - Settings scary pumpkin easter egg: Jack-o'-lantern pops up with a widening carved grin animation, candlelit glow, and auto-dismisses after 2.6s (with tap-to-dismiss).
  - Dedicated **Settings → Seasonal Theme / Special Effects** section with granular toggles for Halloween Theme, Special Animations, Flying Bats, and Pumpkin Easter Egg.
  - Reduced-motion accessible: Turning off special animations instantly removes flying bats and easter egg animations while keeping the static theme active.
- **Swipe Clean Optimization**:
  - GPU hardware-accelerated layer rendering with `graphicsLayer`.
  - Underlying card preview rendering for instant visibility of upcoming items in the card stack.
  - Coil image memory preloading with disk cache optimization for zero gesture lag.
- **Testing & Verification**:
  - Added `SeasonalThemeTest` suite validating Halloween configurations, animation inhibition logic, and single-session easter egg memory.
  - 100% test pass rate across all 39 unit tests in `:app:testDebugUnitTest` and `:app:testReleaseUnitTest`.

### v1.1.0 — Swipe Clean + Wallpaper Themes Initial Update
- **Swipe Clean Experience**:
  - Tinder-style interactive card interface with spring physics, rotation tilt, and haptic feedback.
  - Dedicated "Swipe" navigation tab in bottom bar.
  - 7 Smart Cleanup Categories: All Photos, All Videos, Screenshots, Large Files (>25 MB), Old Media (>6 Months), Downloads, Camera Folder.
  - Granular sorting (Newest, Oldest, Largest, Smallest) and minimum file-size filters (Any, 5 MB, 10 MB, 25 MB, 50 MB, 100 MB).
  - Inline video playback on card preview via ExoPlayer.
  - Tappable Keep, Trash, Preview, and Undo action buttons.
  - Live storage dashboard computing actual MediaStore recoverable disk space.
  - Safe Scoped Storage deletion flow with itemized confirmation dialog and system `createTrashRequest`.
  - Absolute privacy isolation: Encrypted vault files are completely excluded from swipe queues.
- **Built-in Wallpaper Themes Engine**:
  - Settings → Appearance → Wallpaper Themes gallery.
  - 8 bundled offline vector/gradient wallpapers: Midnight Aurora, Sakura Dream, Cyberpunk Neon, AMOLED Black, Ocean Depths, Sunset Glow, Cosmic Galaxy, Minimal Frost.
  - Whole-app wallpaper integration: root Jetpack Compose backdrop layer with frosted, translucent surface components across gallery, albums, videos, swipe, trash, and settings.
  - Customization controls: live dimming slider (0% to 80%), Dark/Light/System theme modes, custom accent palettes, and one-tap default reset.
  - Persistent settings stored via Jetpack DataStore.
  - Distraction-free media viewer: Fullscreen image viewer and ExoPlayer video player remain pure black.
- **Testing & Quality**:
  - Added unit test suites `SwipeCleanTest` (categories, filtering, queues, recoverable bytes, undo, reset) and `WallpaperThemeTest` (theme resolution, dimming clamping, persistence).
  - 100% test pass rate across all test suites.

### v1.0.0 — Initial Release
- Native Android gallery app with MediaStore auto-discovery and chronological timeline grid.
- Dedicated Videos feed with hardware-accelerated Media3 ExoPlayer and intuitive gesture controls (brightness, volume, 10s seek, PiP).
- Encrypted Private Vault powered by hardware-backed AES-256-GCM Keystore cryptography, RAM-only decryption, biometric authentication, and background auto-lock.
- Dual-mode Recently Deleted trash (30-day MediaStore trash + app-managed encrypted vault trash with shredding).
- Built-in non-destructive Photo Editor (rotate, crop, color adjustments, ColorMatrix filters) and Video Editor (timeline trimming, audio muting, fast muxing).
- Obsidian Dark theme optimized for Motorola OLED displays.
- 100% offline, zero network permissions, and zero tracking.

---

## License

MotoGallery is distributed under the [MIT License](LICENSE).

