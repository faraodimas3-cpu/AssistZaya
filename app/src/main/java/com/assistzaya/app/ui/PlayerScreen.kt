package com.assistzaya.app.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.assistzaya.app.AppViewModel
import com.assistzaya.app.R
import com.assistzaya.app.data.formatTime
import com.assistzaya.app.data.isOnline
import com.assistzaya.app.ui.theme.*
import kotlinx.coroutines.delay

fun Context.findActivity(): Activity? {
    var c = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/** AssistZaya Player: reproduz vídeo online ou baixado, com progresso, volume, tela cheia e retomada. */
@Composable
fun PlayerScreen(vm: AppViewModel, nav: NavController, contentId: String, episodeId: String?) {
    val content = vm.content(contentId)
    if (content == null) {
        LaunchedEffect(Unit) { nav.popBackStack() }
        return
    }
    val context = LocalContext.current
    val view = LocalView.current
    val episode = content.episodes.firstOrNull { it.id == episodeId }
    val title = if (episode != null) "${content.title} · EP ${episode.season}x${episode.number}" else content.title
    val local = remember(contentId, episodeId) { vm.localFile(contentId, episodeId) }
    val uri: Uri = if (local != null) Uri.fromFile(local) else Uri.parse(episode?.videoUrl ?: content.videoUrl ?: "")

    val exo = remember { ExoPlayer.Builder(context).build() }
    var isPlaying by remember { mutableStateOf(true) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var controlsVisible by remember { mutableStateOf(true) }
    var fullscreen by remember { mutableStateOf(false) }

    val audio = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVol = remember { audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
    var volume by remember { mutableFloatStateOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()) }

    DisposableEffect(Unit) {
        exo.setMediaItem(MediaItem.fromUri(uri))
        val start = if (vm.toggle("play_resume")) vm.getPosition(contentId, episodeId) else 0L
        if (start > 0) exo.seekTo(start)
        exo.prepare()
        exo.playWhenReady = true
        vm.addHistory(contentId)
        vm.addContinue(contentId, episodeId)
        view.keepScreenOn = true

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) duration = exo.duration.coerceAtLeast(0)
                if (state == Player.STATE_ENDED) {
                    vm.savePosition(contentId, episodeId, 0L)
                    vm.removeContinue(contentId)
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                val offline = local == null && !isOnline(context)
                vm.messages.tryEmit(if (offline) R.string.no_connection_toast else R.string.play_failed)
            }
        }
        exo.addListener(listener)
        onDispose {
            if (exo.playbackState != Player.STATE_ENDED) vm.savePosition(contentId, episodeId, exo.currentPosition)
            exo.removeListener(listener)
            exo.release()
            view.keepScreenOn = false
        }
    }

    // Atualiza posição na tela e salva o progresso a cada 5 s.
    LaunchedEffect(exo) {
        while (true) {
            position = exo.currentPosition
            if (duration <= 0) duration = exo.duration.coerceAtLeast(0)
            delay(500)
        }
    }
    LaunchedEffect(exo) {
        while (true) {
            delay(5000)
            if (exo.isPlaying) vm.savePosition(contentId, episodeId, exo.currentPosition)
        }
    }
    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(3500)
            controlsVisible = false
        }
    }

    // Tela cheia: gira para paisagem e esconde as barras do sistema.
    val activity = context.findActivity()
    DisposableEffect(fullscreen) {
        val window = activity?.window
        if (activity != null && window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (fullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose { }
    }
    DisposableEffect(Unit) {
        onDispose {
            val window = activity?.window
            if (activity != null && window != null) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    BackHandler(enabled = fullscreen) { fullscreen = false }

    Box(
        Modifier.fillMaxSize().background(Color.Black).clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null
        ) { controlsVisible = !controlsVisible }
    ) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { useController = false; player = exo } },
            modifier = Modifier.fillMaxSize()
        )

        if (controlsVisible) {
            Box(Modifier.fillMaxSize().background(Color(0x66000000))) {
                // Topo
                Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White)
                    }
                    Column(Modifier.padding(start = 6.dp)) {
                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                        Text(stringResource(R.string.player_name), color = ZMuted, fontSize = 12.sp)
                    }
                }
                // Centro
                Row(Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { exo.seekTo((exo.currentPosition - 10_000).coerceAtLeast(0)) }) {
                        Icon(Icons.Filled.Replay10, null, tint = Color.White, modifier = Modifier.size(38.dp))
                    }
                    IconButton(onClick = { if (exo.isPlaying) exo.pause() else exo.play() }, modifier = Modifier.size(72.dp)) {
                        Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(60.dp))
                    }
                    IconButton(onClick = { exo.seekTo((exo.currentPosition + 10_000).coerceAtMost(duration.coerceAtLeast(1))) }) {
                        Icon(Icons.Filled.Forward10, null, tint = Color.White, modifier = Modifier.size(38.dp))
                    }
                }
                // Base
                Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatTime(position), color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = position.coerceAtMost(duration.coerceAtLeast(1)).toFloat(),
                            onValueChange = { position = it.toLong(); exo.seekTo(it.toLong()) },
                            valueRange = 0f..duration.coerceAtLeast(1).toFloat(),
                            colors = SliderDefaults.colors(thumbColor = ZRed, activeTrackColor = ZRed, inactiveTrackColor = Color(0x55FFFFFF)),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text(formatTime(duration), color = Color.White, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VolumeUp, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Slider(
                            value = volume,
                            onValueChange = { volume = it; audio.setStreamVolume(AudioManager.STREAM_MUSIC, it.toInt(), 0) },
                            valueRange = 0f..maxVol.toFloat(),
                            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color(0x55FFFFFF)),
                            modifier = Modifier.width(150.dp).padding(horizontal = 8.dp)
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { fullscreen = !fullscreen }) {
                            Icon(if (fullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen, null, tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
