package com.assistzaya.app.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import com.assistzaya.app.AppViewModel
import com.assistzaya.app.BuildConfig
import com.assistzaya.app.R
import com.assistzaya.app.data.Toggles
import com.assistzaya.app.data.formatSize
import com.assistzaya.app.ui.theme.*

@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavController) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf(R.string.tab_cfg_home, R.string.tab_cfg_experience, R.string.tab_cfg_system)
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(stringResource(R.string.settings_title), onBack = { nav.popBackStack() })
        TabRow(selectedTabIndex = tab, containerColor = Color.Transparent, contentColor = Color.White) {
            tabs.forEachIndexed { i, t ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(stringResource(t), fontWeight = FontWeight.Bold) })
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            when (tab) {
                0 -> HomeSettings(vm)
                1 -> ExperienceSettings(vm)
                else -> SystemSettings(vm, nav)
            }
        }
    }
}

@Composable
private fun Header(icon: ImageVector, title: String, sub: String) {
    CardBox(Modifier.padding(top = 12.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(icon, 52.dp)
            Column(Modifier.padding(start = 14.dp)) {
                Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text(sub, color = ZMuted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun HomeSettings(vm: AppViewModel) {
    Header(Icons.Filled.Home, stringResource(R.string.cfg_home_title), stringResource(R.string.cfg_home_sub))
    CardBox {
        SwitchRow(stringResource(R.string.cfg_banners), stringResource(R.string.cfg_banners_sub), vm.toggle("home_banners")) { vm.setToggle("home_banners", it) }
        SwitchRow(stringResource(R.string.sec_continue), stringResource(R.string.sec_continue_sub), vm.toggle("home_continue")) { vm.setToggle("home_continue", it) }
        SwitchRow(stringResource(R.string.sec_latest_eps), stringResource(R.string.sec_latest_eps_sub), vm.toggle("home_latest_eps")) { vm.setToggle("home_latest_eps", it) }
        SwitchRow(stringResource(R.string.sec_reviews), null, vm.toggle("home_reviews")) { vm.setToggle("home_reviews", it) }
        SwitchRow(stringResource(R.string.cfg_sections), stringResource(R.string.cfg_sections_sub), vm.toggle("home_sections")) { vm.setToggle("home_sections", it) }
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { Toggles.homeSections.forEach { vm.setToggle(it, false) } },
                shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Filled.VisibilityOff, null, tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.hide_all), color = Color.White)
            }
            Button(
                onClick = { Toggles.homeSections.forEach { vm.setToggle(it, true) } },
                colors = ButtonDefaults.buttonColors(containerColor = ZRedDark),
                shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Filled.Visibility, null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.show_all))
            }
        }
        Text(stringResource(R.string.cfg_sections_individual), color = ZGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
        val sub = stringResource(R.string.cfg_show_section)
        listOf(
            "sec_animes" to R.string.sec_animes, "sec_trending" to R.string.sec_trending, "sec_movies" to R.string.sec_movies,
            "sec_new" to R.string.sec_new, "sec_series" to R.string.sec_series, "sec_popular" to R.string.sec_popular,
            "sec_top_today" to R.string.sec_top_today, "sec_top_viewed" to R.string.sec_top_viewed
        ).forEach { (key, label) ->
            SwitchRow(stringResource(label), sub, vm.toggle(key)) { vm.setToggle(key, it) }
        }
    }
}

@Composable
private fun ExperienceSettings(vm: AppViewModel) {
    Header(Icons.Filled.Info, stringResource(R.string.tab_cfg_experience), stringResource(R.string.notifications_sub))
    CardBox {
        Text(stringResource(R.string.notifications), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 14.dp))
        SwitchRow(stringResource(R.string.notifications), stringResource(R.string.notifications_sub), vm.toggle("notif_enabled")) { vm.setToggle("notif_enabled", it) }
    }
    CardBox {
        Text(stringResource(R.string.playback), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 14.dp))
        SwitchRow(stringResource(R.string.playback_resume), stringResource(R.string.playback_resume_sub), vm.toggle("play_resume")) { vm.setToggle("play_resume", it) }
    }
    CardBox {
        SwitchRow(stringResource(R.string.warn_offline), stringResource(R.string.warn_offline_sub), vm.toggle("warn_offline")) { vm.setToggle("warn_offline", it) }
    }
}

@Composable
private fun SystemSettings(vm: AppViewModel, nav: NavController) {
    val context = LocalContext.current
    Header(Icons.Filled.Settings, stringResource(R.string.tab_cfg_system), stringResource(R.string.language))
    CardBox {
        NavRow(Icons.Filled.Language, stringResource(R.string.language)) { nav.navigate(Routes.LANGUAGES) }
        NavRow(Icons.Filled.Person, stringResource(R.string.nav_account)) { nav.goTab(Routes.ACCOUNT) }
        NavRow(Icons.Filled.Lock, stringResource(R.string.privacy)) { nav.navigate(Routes.PRIVACY) }
        NavRow(Icons.Filled.Description, stringResource(R.string.terms)) { nav.navigate(Routes.TERMS) }
        NavRow(Icons.Filled.SupportAgent, stringResource(R.string.support)) { nav.navigate(Routes.SUPPORT) }
    }
    CardBox {
        Text(stringResource(R.string.downloads), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 14.dp))
        SwitchRow(stringResource(R.string.dl_wifi_only), null, vm.toggle("dl_wifi_only")) { vm.setToggle("dl_wifi_only", it) }
        NavRow(Icons.Filled.Download, stringResource(R.string.downloads_title)) { nav.goTab(Routes.DOWNLOADS) }
    }
    CardBox {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.storage), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.storage_used, formatSize(vm.downloadsSize())), color = ZMuted)
            Button(
                onClick = { context.cacheDir.deleteRecursively(); vm.messages.tryEmit(R.string.cache_cleared) },
                colors = ButtonDefaults.buttonColors(containerColor = ZRedDark), shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Delete, null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.clear_cache))
            }
        }
    }
    CardBox {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.Filled.Info, 44.dp)
            Column(Modifier.padding(start = 14.dp)) {
                Text(stringResource(R.string.about), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(stringResource(R.string.version, BuildConfig.VERSION_NAME), color = ZMuted)
            }
        }
    }
}

@Composable
private fun NavRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ZRed)
        Text(label, Modifier.weight(1f).padding(start = 14.dp), fontSize = 16.sp)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = ZMuted)
    }
}

private val languages = listOf(
    "pt-PT" to "Português", "pt-BR" to "Português (Brasil)", "en" to "English", "es" to "Español",
    "fr" to "Français", "de" to "Deutsch", "it" to "Italiano", "ja" to "日本語", "ko" to "한국어",
    "zh" to "中文", "ar" to "العربية", "hi" to "हिन्दी", "ru" to "Русский", "tr" to "Türkçe",
    "id" to "Bahasa Indonesia", "nl" to "Nederlands", "pl" to "Polski", "uk" to "Українська",
    "th" to "ไทย", "vi" to "Tiếng Việt"
)

@Composable
fun LanguageScreen(nav: NavController) {
    var current by remember { mutableStateOf(AppCompatDelegate.getApplicationLocales().toLanguageTags()) }

    fun choose(tag: String) {
        current = tag
        AppCompatDelegate.setApplicationLocales(
            if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag)
        )
    }

    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(stringResource(R.string.language), onBack = { nav.popBackStack() })
        Column(Modifier.verticalScroll(rememberScrollState())) {
            LangRow(stringResource(R.string.use_device_language), current.isEmpty()) { choose("") }
            languages.forEach { (tag, name) -> LangRow(name, current == tag) { choose(tag) } }
        }
    }
}

@Composable
private fun LangRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(name, Modifier.weight(1f), fontSize = 16.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        if (selected) Icon(Icons.Filled.Check, null, tint = ZRed)
    }
}
