package com.assistzaya.app.data

enum class ContentType { ANIME, FILME, SERIE }

const val TAG_TRENDING = "trending"
const val TAG_POPULAR = "popular"
const val TAG_NEW = "new"
const val TAG_TOP_TODAY = "top_today"
const val TAG_TOP_VIEWED = "top_viewed"

data class Episode(
    val id: String,
    val season: Int,
    val number: Int,
    val title: String,
    val videoUrl: String,
    val date: String
)

data class Content(
    val id: String,
    val title: String,
    val type: ContentType,
    val genres: List<String>,
    val ageRating: String,
    val year: Int,
    val audio: String,
    val description: String,
    val posterUrl: String?,
    val bannerUrl: String?,
    val colorSeed: Int,
    val videoUrl: String?,
    val episodes: List<Episode>,
    val tags: Set<String>
) {
    val isSeries: Boolean get() = episodes.isNotEmpty()
}

data class Account(val name: String = "", val email: String = "", val signedIn: Boolean = false)

/** like: 1 = gostei, -1 = não gostei, 0 = nenhum */
data class Rating(val contentId: String, val stars: Int, val like: Int, val review: String, val time: Long)

data class DownloadItem(
    val key: String,
    val contentId: String,
    val episodeId: String?,
    val title: String,
    val subtitle: String,
    val filePath: String,
    val sizeBytes: Long,
    val dateMillis: Long
)

data class ContinueEntry(val contentId: String, val episodeId: String?)

object Toggles {
    val homeSections = listOf(
        "sec_top_today", "sec_top_viewed", "sec_new", "sec_trending",
        "sec_popular", "sec_animes", "sec_series", "sec_movies"
    )
    val all = listOf("home_banners", "home_continue", "home_latest_eps", "home_reviews", "home_sections") +
        homeSections + listOf("notif_enabled", "warn_offline", "play_resume", "dl_wifi_only")

    fun default(key: String) = key != "dl_wifi_only"
}
