# Contributing to MotoGallery

Thank you for your interest in contributing to **MotoGallery**! MotoGallery is an open-source, privacy-first native Android gallery application built with Kotlin, Jetpack Compose, Material 3, and modern Android architecture.

## Code of Conduct

We are committed to providing a welcoming, inclusive, and harassment-free environment for everyone. Please be respectful and constructive in all discussions.

## Principles

1. **Strictly Offline**: No telemetry, analytics, ad networks, tracking SDKs, or cloud accounts.
2. **Native Android Best Practices**: Use MediaStore, Scoped Storage consent dialogs, Jetpack Compose, Room, and Media3 ExoPlayer.
3. **Hardware-Backed Security**: Private Vault encryption must always use authenticated encryption (AES-256-GCM) with Android Keystore backed keys. Plaintext media must never be written unencrypted to disk.

## Development Setup

1. **Prerequisites**:
   - JDK 17 (Eclipse Adoptium / Temurin recommended)
   - Android Studio Iguana or newer (or Android SDK 34)
2. **Clone & Build**:
   ```bash
   git clone https://github.com/<owner>/MotoGallery.git
   cd MotoGallery
   ./gradlew test
   ./gradlew assembleDebug
   ```
3. **Running on Device**:
   ```bash
   adb install -r app/build/outputs/apk/debug/MotoGallery-v1.0.0-debug.apk
   ```

## Pull Request Guidelines

- Ensure `./gradlew test` passes cleanly before submitting.
- Write unit tests for any new data transformations, crypto handlers, or formatters.
- Follow Kotlin official code style conventions.
- Maintain non-destructive editing guarantees (never overwrite original user media silently).

## Reporting Issues

If you find a bug or have a feature suggestion, please open a GitHub Issue with:
- Device model (e.g., Motorola Edge 50 Pro, Moto G84, etc.)
- Android OS version (e.g., Android 13, Android 14)
- Clear reproduction steps and relevant logcat output
