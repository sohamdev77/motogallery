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

---

## Interface Design

MotoGallery features a custom dark-first design palette inspired by modern Motorola OLED displays:

| Element | Hex Code | Purpose |
|---|---|---|
| **Obsidian Dark** | `#0B1020` | Root background (deep battery-saving black) |
| **Surface Dark** | `#131A2E` | Cards, navigation bars, dialog containers |
| **Surface Highlight** | `#1A233D` | Active pills, borders, selected states |
| **Violet Accent** | `#A855F7` | Primary brand accent & active indicators |
| **Pink Accent** | `#EC65B5` | Secondary accent & favorite heart badge |
| **Orange Accent** | `#FFA34D` | Tertiary accent & warning indicators |

- **5-Tab Navigation**: `Photos` | `Albums` | `Videos` | `Private` | `Settings`
- **Dynamic Color Support**: Optional toggle in Settings to adapt colors to Android 12+ wallpaper themes.
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
2. Under the latest release, download `MotoGallery-v1.0.0-release.apk` (or `-debug.apk`).
3. Open the downloaded `.apk` from your notification shade or Downloads folder.
4. When prompted by Android ("For your security, your phone is not allowed to install unknown apps from this source"), tap **Settings** and toggle **Allow from this source**.
5. Tap **Install** and launch **MotoGallery**.

### Option B: Download Build Artifact from GitHub Actions
1. Navigate to the **Actions** tab in this repository.
2. Click on the latest workflow run on `main`.
3. Scroll down to the **Artifacts** section and download `MotoGallery-Debug-APK.zip`.
4. Extract the zip file and transfer `MotoGallery-v1.0.0-debug.apk` to your phone via USB or Google Drive / Files.

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
# app/build/outputs/apk/debug/MotoGallery-v1.0.0-debug.apk
```

---

## Quality Assurance & Motorola Device Test Checklist

The following manual test checklist is verified on Motorola devices (Moto G series, Motorola Edge series):

- [x] **Offline Reliability**: App functions with zero network connectivity in Airplane Mode. No crashes, no network timeouts, no analytics polling.
- [x] **Screen Rotation**: Fullscreen video player and photo editor retain playback state, zoom level, and adjustments across 90° and 180° orientation changes.
- [x] **Background Auto-Lock**: Vault immediately locks when switching to the home screen or another app. PIN or fingerprint is required upon return.
- [x] **Biometric Authentication**: Fingerprint unlock immediately unlocks vault; cancelling falls back to numeric PIN keypad.
- [x] **Large Library Performance**: Scrolling through 5,000+ images remains fluid at 60/120Hz; memory cache is constrained to 25% heap to prevent OutOfMemoryErrors.
- [x] **Partial Media Access (Android 14)**: Granting only selected photos displays the permission banner with working "Manage" button.
- [x] **Public Original Deletion Consent**: Importing media to vault proposes deletion of the public original via system consent dialog without deleting files silently.
- [x] **Non-Destructive Editing**: Exporting an edited photo or trimmed video generates a new file in `Pictures/MotoGallery` or `Movies/MotoGallery` without altering the original.

---

## License

MotoGallery is distributed under the [MIT License](LICENSE).
