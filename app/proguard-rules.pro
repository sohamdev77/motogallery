# MotoGallery Proguard Rules

# Keep Room schemas and entities
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Media3 ExoPlayer classes
-keep class androidx.media3.** { *; }

# Keep BiometricPrompt classes
-keep class androidx.biometric.** { *; }

# Keep Coil classes
-keep class coil.** { *; }

# Keep Data models
-keepclassmembers class com.motogallery.app.data.model.** { *; }
-keepclassmembers class com.motogallery.app.data.local.entity.** { *; }
