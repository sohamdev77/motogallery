# Changelog

All notable changes to MotoGallery will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [v1.2.0] — 2026-10-30

### Added
- **Fresh App Icon Language**:
  - Replaced launcher icons across all mipmap densities (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) with a brand new design inspired by the galaxy mountain visual reference.
  - Features a cosmic nebula sky, glowing full moon, soft frosted rounded mountain peaks, and glowing neon purple/cyan squircle rim with adaptive icon compliance.
- **Seasonal Halloween Fun Update**:
  - **Halloween Visual Theme**: Festive orange-and-black seasonal palette featuring `#FF7518` pumpkin orange accents, `#FFA726` spooky amber highlights, `#090608` deep eerie obsidian surfaces, and subtle purple glow effects.
  - **Ambient Themed Decorations**: Delicate canvas-drawn cobwebs in top corners and floating night-sky bats gliding smoothly across headers.
  - **Settings Pumpkin Easter Egg**: A playful, scary smiling Jack-o'-lantern appears in Settings with an animated grin widening from ear to ear, subtle warmth pulse, and auto-dismiss after 2.6 seconds (with tap-to-dismiss). Triggers automatically once per app session or on demand.
  - **Seasonal Theme / Special Effects Settings**: Dedicated control section in Settings allowing users to toggle:
    - *Enable Halloween Theme*
    - *Enable Special Animations* (inhibits floating/motion while keeping static Halloween theme)
    - *Enable Flying Bats*
    - *Enable Pumpkin Easter Egg*
- **Swipe Clean (Tinder-Style Media Cleanup)**:
  - Card-based photo and video review interface in a dedicated **Swipe** navigation tab.
  - Drag left to queue for trash, drag right to keep, drag up for instant fullscreen preview, and tap video cards to play/pause inline previews.
  - Hardware-accelerated GPU graphics layer with card tilt rotation, spring physics, and haptic feedback.
  - Smart category filters: All Photos, All Videos, Screenshots, Large Files ($\ge 25$ MB), Old Media ($> 6$ months), Downloads, and Camera Folder.
  - Real-time recoverable storage indicator and safe deletion flow through Android Scoped Storage confirmation dialogs (`MediaStore.createTrashRequest`).
- **Built-in Wallpaper Themes Engine**:
  - Personalize MotoGallery with 8 bundled offline wallpapers: Midnight Aurora, Sakura Dream, Cyberpunk Neon, AMOLED Black, Ocean Depths, Sunset Glow, Cosmic Galaxy, and Minimal Frost.
  - Live dimming slider (0% to 80%), Dark/Light/System theme modes, custom accent colors, and instant persistence via Jetpack DataStore.
- **Automated Unit Tests**:
  - `SeasonalThemeTest`: Validates default Halloween configurations, color hex integrity, animation toggle inhibition logic, and single-session easter egg memory.
  - Maintained 100% pass rate across all existing test suites (`SwipeCleanTest`, `WallpaperThemeTest`, `MediaDeletionAndVaultIsolationTest`, `VaultCryptoTest`, etc.).

---

## [v1.1.0] — 2026-10-30

### Added
- Interactive Swipe Clean cleanup flow with undo history.
- Built-in Wallpaper Themes picker screen with real-time dimming.
- MediaStore trash flow and Vault media isolation upon import.

---

## [v1.0.0] — 2026-10-30

### Added
- Initial release of MotoGallery — Native Android Gallery Application.
- MediaStore auto-discovery and high-performance chronological timeline grid.
- Dedicated Videos feed with hardware-accelerated Media3 ExoPlayer (gestures, PiP, resume).
- Hardware-backed AES-256-GCM Private Vault with Android Keystore, biometric authentication, and background auto-lock.
- Dual-mode Recently Deleted trash (MediaStore system trash + vault shredded trash).
- Non-destructive Photo Editor (transform, crop, adjustments, ColorMatrix filters) and Video Editor (trimming, audio muting, stream muxing).
- 100% offline, zero network permissions, zero trackers.
