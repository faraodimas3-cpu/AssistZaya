package com.assistzaya.app.ui

import android.net.Uri
import androidx.navigation.NavController

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GENRES = "genres"
    const val EXPLORE = "explore"
    const val DOWNLOADS = "downloads"
    const val ACCOUNT = "account"
    const val SETTINGS = "settings"
    const val LANGUAGES = "languages"
    const val SUPPORT = "support"
    const val PRIVACY = "privacy"
    const val TERMS = "terms"
    const val DETAILS = "details/{id}"
    const val EPISODES = "episodes/{id}"
    const val PLAYER = "player/{id}/{ep}"
    const val LIST = "list/{kind}"

    val tabs = listOf(HOME, GENRES, EXPLORE, DOWNLOADS, ACCOUNT)

    fun details(id: String) = "details/$id"
    fun episodes(id: String) = "episodes/$id"
    fun player(id: String, ep: String?) = "player/$id/${ep ?: "-"}"
    fun list(kind: String) = "list/${Uri.encode(kind)}"
}

fun NavController.goTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
