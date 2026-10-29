package com.motogallery.app.data.model

enum class SwipeCategory(val displayName: String) {
    ALL_PHOTOS("All Photos"),
    ALL_VIDEOS("All Videos"),
    SCREENSHOTS("Screenshots"),
    LARGE_FILES("Large Files (>25MB)"),
    OLD_MEDIA("Old Media (>6 Mo)"),
    DOWNLOADS("Downloads"),
    CAMERA("Camera Folder")
}

enum class SwipeSort(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    SIZE_DESC("Largest First"),
    SIZE_ASC("Smallest First")
}

enum class SwipeMinSize(val displayName: String, val minBytes: Long) {
    ALL("All Sizes", 0L),
    MB_5("5+ MB", 5L * 1024 * 1024),
    MB_10("10+ MB", 10L * 1024 * 1024),
    MB_25("25+ MB", 25L * 1024 * 1024),
    MB_50("50+ MB", 50L * 1024 * 1024),
    MB_100("100+ MB", 100L * 1024 * 1024)
}

enum class SwipeDirection {
    LEFT,   // Trash
    RIGHT,  // Keep
    UP      // Fullscreen Preview
}

data class SwipeActionRecord(
    val mediaItem: MediaItem,
    val direction: SwipeDirection,
    val timestamp: Long = System.currentTimeMillis()
)
