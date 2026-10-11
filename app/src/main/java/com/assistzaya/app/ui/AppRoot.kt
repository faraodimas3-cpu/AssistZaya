package com.assistzaya.app.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.assistzaya.app.AppViewModel
import com.assistzaya.app.R
import com.assistzaya.app.data.Content
import com.assistzaya.app.ui.theme.*

/** Abre o player: filmes direto; séries/animes continuam do último episódio ou começam do primeiro. */
fun watchContent(vm: AppViewModel, nav: NavController, c: Content) {
    if (c.isSeries) {
        val entry = vm.continueEntries.firstOrNull { it.contentId == c.id }
        val ep = c.episodes.firstOrNull { it.id == entry?.episodeId } ?: c.episodes.first()
        nav.navigate(Routes.player(c.id, ep.id))
    } else {
        nav.navigate(Routes.player(c.id, null))
    }
}

fun openCast(context: Context, vm: AppViewModel) {
    val intents = listOf(Intent(Settings.ACTION_CAST_SETTINGS), Intent("android.settings.WIFI_DISPLAY_SETTINGS"))
    for (i in intents) {
        try { context.startActivity(i); return } catch (_: Exception) { }
    }
    vm.messages.tryEmit(R.string.mirror_failed)
}

@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    val nav = rememberNavController()
    val context = LocalContext.current
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    var sheetFor by remember { mutableStateOf<Content?>(null) }
    var vipDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.messages.collect { Toast.makeText(context, context.getString(it), Toast.LENGTH_SHORT).show() }
    }

    val isTab = route in Routes.tabs
    Scaffold(
        containerColor = ZBg, contentColor = Color.White,
        bottomBar = { if (isTab) BottomBar(nav, route) }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            NavHost(navController = nav, startDestination = Routes.SPLASH) {
                composable(Routes.SPLASH) {
                    SplashScreen(
                        onReady = { nav.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                        onOpenDownloads = { nav.navigate(Routes.DOWNLOADS) { popUpTo(Routes.SPLASH) { inclusive = true } } }
                    )
                }
                composable(Routes.HOME) { HomeScreen(vm, nav) { sheetFor = it } }
                composable(Routes.GENRES) { GenresScreen(vm, nav) }
                composable(Routes.EXPLORE) { ExploreScreen(vm, nav) { sheetFor = it } }
                composable(Routes.DOWNLOADS) { DownloadsScreen(vm, nav) }
                composable(Routes.ACCOUNT) { AccountScreen(vm, nav) }
                composable(Routes.SETTINGS) { SettingsScreen(vm, nav) }
                composable(Routes.LANGUAGES) { LanguageScreen(nav) }
                composable(Routes.SUPPORT) { SupportScreen(nav) }
                composable(Routes.PRIVACY) { PrivacyScreen(vm, nav) }
                composable(Routes.TERMS) { TermsScreen(nav) }
                composable(Routes.DETAILS, listOf(navArgument("id") { type = NavType.StringType })) {
                    DetailsScreen(vm, nav, it.arguments?.getString("id").orEmpty())
                }
                composable(Routes.EPISODES, listOf(navArgument("id") { type = NavType.StringType })) {
                    EpisodesScreen(vm, nav, it.arguments?.getString("id").orEmpty())
                }
                composable(
                    Routes.PLAYER,
                    listOf(navArgument("id") { type = NavType.StringType }, navArgument("ep") { type = NavType.StringType })
                ) {
                    val ep = it.arguments?.getString("ep").orEmpty()
                    PlayerScreen(vm, nav, it.arguments?.getString("id").orEmpty(), if (ep == "-") null else ep)
                }
                composable(Routes.LIST, listOf(navArgument("kind") { type = NavType.StringType })) {
                    ListScreen(vm, nav, it.arguments?.getString("kind").orEmpty()) { c -> sheetFor = c }
                }
            }
            if (isTab && vm.vipEnabled && !vm.vipHidden) {
                VipButton(Modifier.align(Alignment.CenterEnd), onOpen = { vipDialog = true }, onHide = { vm.vipHidden = true })
            }
        }
    }

    sheetFor?.let { c ->
        ActionDialog(
            c,
            onDismiss = { sheetFor = null },
            onWatch = { sheetFor = null; watchContent(vm, nav, c) },
            onMirror = { sheetFor = null; openCast(context, vm) },
            onDownload = {
                sheetFor = null
                if (c.isSeries) nav.navigate(Routes.episodes(c.id)) else vm.startDownload(c, null)
            },
            onDetails = { sheetFor = null; nav.navigate(Routes.details(c.id)) }
        )
    }

    if (vipDialog) {
        AlertDialog(
            onDismissRequest = { vipDialog = false }, containerColor = ZSurface,
            title = { Text(stringResource(R.string.vip_title)) },
            text = { Text(stringResource(R.string.vip_text), color = ZMuted) },
            confirmButton = { TextButton(onClick = { vipDialog = false }) { Text(stringResource(R.string.close)) } }
        )
    }
}

@Composable
private fun VipButton(modifier: Modifier, onOpen: () -> Unit, onHide: () -> Unit) {
    Column(modifier.padding(end = 6.dp), horizontalAlignment = Alignment.End) {
        Box(
            Modifier.size(22.dp).clip(CircleShape).background(Color(0xCC000000)).clickable { onHide() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.Close, stringResource(R.string.vip_hide), Modifier.size(14.dp), tint = Color.White) }
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(listOf(ZGold, Color(0xFFE08A1E))))
                .clickable { onOpen() }.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.WorkspacePremium, null, Modifier.size(18.dp), tint = Color.Black)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.vip), color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionDialog(
    c: Content, onDismiss: () -> Unit, onWatch: () -> Unit,
    onMirror: () -> Unit, onDownload: () -> Unit, onDetails: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = ZSurface, contentColor = Color.White) {
            Column(Modifier.padding(bottom = 14.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(Icons.Filled.PlayArrow, 56.dp)
                    Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                        Text(stringResource(R.string.dialog_title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(c.title, color = ZMuted, fontSize = 14.sp, maxLines = 2)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, stringResource(R.string.close), tint = ZMuted) }
                }
                HorizontalDivider(color = ZOutline)
                Spacer(Modifier.height(8.dp))
                ActionRow(Icons.Filled.PlayArrow, R.string.act_watch, R.string.act_watch_sub, onWatch)
                ActionRow(Icons.Filled.Cast, R.string.act_mirror, R.string.act_mirror_sub, onMirror)
                ActionRow(Icons.Filled.Download, R.string.act_download, R.string.act_download_sub, onDownload)
                ActionRow(Icons.Filled.Info, R.string.act_details, R.string.act_details_sub, onDetails)
            }
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, title: Int, sub: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp)
            .clip(shape).border(1.dp, ZOutline, shape).clickable { onClick() }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(icon, 48.dp)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(stringResource(title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(stringResource(sub), color = ZMuted, fontSize = 13.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = ZMuted)
    }
}
