package com.assistzaya.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assistzaya.app.R
import com.assistzaya.app.data.isOnline
import com.assistzaya.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onReady: () -> Unit, onOpenDownloads: () -> Unit) {
    val context = LocalContext.current
    var offline by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    val scale = remember { Animatable(0.6f) }
    val fade = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
        fade.animateTo(1f, tween(900))
    }
    // Depois da animação, verifica a conexão.
    LaunchedEffect(attempt) {
        offline = false
        delay(1800)
        if (isOnline(context)) onReady() else offline = true
    }

    Box(
        Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF2A0F10), ZBg))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(
                Modifier.size(112.dp)
                    .graphicsLayer { scaleX = scale.value; scaleY = scale.value; alpha = fade.value }
                    .clip(CircleShape).background(Brush.radialGradient(listOf(ZRed, ZRedDark))),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(64.dp)) }

            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.app_name), fontSize = 30.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer { alpha = fade.value }
            )
            Spacer(Modifier.height(28.dp))

            if (offline) {
                Icon(Icons.Filled.WifiOff, null, tint = ZGold, modifier = Modifier.size(34.dp))
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.splash_no_connection), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(stringResource(R.string.splash_no_connection_msg), color = ZMuted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { attempt++ }, colors = ButtonDefaults.buttonColors(containerColor = ZRed)) {
                    Text(stringResource(R.string.retry))
                }
                TextButton(onClick = onOpenDownloads) { Text(stringResource(R.string.open_downloads), color = ZMuted) }
            } else {
                CircularProgressIndicator(color = ZRed, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            }
        }
    }
}
