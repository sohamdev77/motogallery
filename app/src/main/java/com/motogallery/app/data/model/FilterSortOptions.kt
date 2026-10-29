package com.motogallery.app.data.model

enum class SortBy(val label: String) {
    DATE_DESC("Newest first"),
    DATE_ASC("Oldest first"),
    NAME_ASC("Name (A to Z)"),
    NAME_DESC("Name (Z to A)"),
    SIZE_DESC("Largest first"),
    SIZE_ASC("Smallest first")
}

enum class MediaTypeFilter(val label: String) {
    ALL("All media"),
    PHOTOS_ONLY("Photos only"),
    VIDEOS_ONLY("Videos only"),
    FAVORITES_ONLY("Favorites only")
}

data class FilterSortOptions(
    val sortBy: SortBy = SortBy.DATE_DESC,
    val mediaTypeFilter: MediaTypeFilter = MediaTypeFilter.ALL,
    val searchQuery: String = ""
)
