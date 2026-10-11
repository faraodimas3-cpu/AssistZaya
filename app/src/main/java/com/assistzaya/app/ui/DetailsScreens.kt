package com.assistzaya.app.ui

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.assistzaya.app.AppViewModel
import com.assistzaya.app.R
import com.assistzaya.app.data.*
import com.assistzaya.app.ui.theme.*
import java.util.Locale

@Composable
fun DetailsScreen(vm: AppViewModel, nav: NavController, id: String) {
    val c = vm.content(id)
    if (c == null) {
        LaunchedEffect(Unit) { nav.popBackStack() }
        return
    }
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var showRate by remember { mutableStateOf(false) }
    val inList = id in vm.myList
    val fav = id in vm.favorites
    val entry = vm.continueEntries.firstOrNull { it.contentId == id }
    val (avg, count) = vm.average(id)
    val mine = vm.ratings[id]

    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("", onBack = { nav.popBackStack() }) {
            IconButton(onClick = { vm.toggleMyList(id) }) {
                Icon(if (inList) Icons.Filled.PlaylistAddCheck else Icons.Filled.PlaylistAdd, stringResource(R.string.add_list))
            }
            IconButton(onClick = { vm.toggleFavorite(id) }) {
                Icon(if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, stringResource(R.string.favorite), tint = if (fav) ZRed else Color.White)
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box(
                Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(c.colorSeed), ZBg))).padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) { Poster(c, Modifier.width(160.dp).aspectRatio(2f / 3f)) }

            Text(c.title, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp))
            if (count > 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    StarRow(Math.round(avg), null, 18.dp)
                    Text("  ${String.format(Locale.getDefault(), "%.1f", avg)} ($count)", color = ZMuted)
                }
            }

            // Assistir / continuar
            Button(
                onClick = {
                    if (c.isSeries) {
                        val ep = c.episodes.firstOrNull { it.id == entry?.episodeId } ?: c.episodes.first()
                        nav.navigate(Routes.player(c.id, ep.id))
                    } else nav.navigate(Routes.player(c.id, null))
                },
                shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = ZRed),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp).height(54.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (entry != null) R.string.resume else R.string.start_over), fontWeight = FontWeight.Bold)
            }
            if (entry != null) {
                OutlinedButton(
                    onClick = {
                        val epId = if (c.isSeries) c.episodes.first().id else null
                        vm.savePosition(c.id, epId, 0L)
                        nav.navigate(Routes.player(c.id, epId))
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp).height(50.dp)
                ) { Text(stringResource(R.string.start_over), color = Color.White) }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (c.isSeries) {
                    Button(
                        onClick = { nav.navigate(Routes.episodes(c.id)) }, shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZRed), modifier = Modifier.weight(1f).height(50.dp)
                    ) { Text(stringResource(R.string.view_episodes), fontWeight = FontWeight.Bold) }
                } else {
                    OutlinedButton(
                        onClick = { vm.startDownload(c, null) }, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(Icons.Filled.Download, null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.act_download), color = Color.White)
                    }
                }
                OutlinedButton(
                    onClick = { openCast(context, vm) }, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Filled.Cast, null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.act_mirror), color = Color.White)
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { showRate = true }, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f).height(50.dp)) {
                    Icon(Icons.Filled.StarBorder, null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.rate), color = Color.White)
                }
                OutlinedButton(
                    onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${c.title} - AssistZaya")
                        }
                        context.startActivity(Intent.createChooser(send, null))
                    },
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Filled.Share, null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.share), color = Color.White)
                }
            }

            // Abas
            val tabs = listOf(R.string.tab_overview, R.string.tab_info, R.string.tab_videos)
            TabRow(selectedTabIndex = tab, containerColor = Color.Transparent, contentColor = Color.White, modifier = Modifier.padding(top = 14.dp)) {
                tabs.forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(stringResource(t), fontWeight = FontWeight.Bold) })
                }
            }

            when (tab) {
                0 -> {
                    CardBox(Modifier.padding(top = 10.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.synopsis), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(c.description, fontSize = 16.sp)
                        }
                    }
                    SectionHeader(stringResource(R.string.reviews_title), stringResource(R.string.reviews_sub))
                    if (mine != null && (mine.stars > 0 || mine.review.isNotBlank())) {
                        CardBox {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(40.dp).clip(CircleShape).background(ZRed), contentAlignment = Alignment.Center) {
                                        Text(vm.account.name.ifBlank { stringResource(R.string.you) }.take(1).uppercase(), fontWeight = FontWeight.Bold)
                                    }
                                    Column(Modifier.padding(start = 10.dp)) {
                                        Text(vm.account.name.ifBlank { stringResource(R.string.you) }, fontWeight = FontWeight.Bold)
                                        StarRow(mine.stars, null, 16.dp)
                                    }
                                    Spacer(Modifier.weight(1f))
                                    if (mine.like == 1) Icon(Icons.Filled.ThumbUp, null, tint = ZGold)
                                    if (mine.like == -1) Icon(Icons.Filled.ThumbDown, null, tint = ZMuted)
                                }
                                if (mine.review.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(mine.review, color = ZMuted)
                                }
                            }
                        }
                    } else {
                        Text(stringResource(R.string.no_reviews), color = ZMuted, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
                1 -> CardBox(Modifier.padding(top = 10.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoLine(stringResource(R.string.info_type), stringResource(typeLabel(c.type)))
                        InfoLine(stringResource(R.string.info_genres), c.genres.joinToString(", "))
                        InfoLine(stringResource(R.string.info_year), c.year.toString())
                        InfoLine(stringResource(R.string.info_rating), c.ageRating)
                        InfoLine(stringResource(R.string.info_audio), c.audio)
                    }
                }
                else -> Column(Modifier.padding(top = 10.dp)) {
                    if (c.isSeries) {
                        c.episodes.forEach { ep ->
                            CardBox {
                                Row(
                                    Modifier.clickable { nav.navigate(Routes.player(c.id, ep.id)) }.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconBubble(Icons.Filled.PlayArrow, 40.dp)
                                    Column(Modifier.padding(start = 12.dp)) {
                                        Text("EP ${ep.season}x${ep.number}", fontWeight = FontWeight.Bold)
                                        Text(ep.date, color = ZMuted, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        CardBox {
                            Row(
                                Modifier.clickable { nav.navigate(Routes.player(c.id, null)) }.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconBubble(Icons.Filled.PlayArrow, 40.dp)
                                Text(c.title, Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showRate) {
        RatingDialog(
            c, mine, onDismiss = { showRate = false },
            onSave = { vm.rate(it); showRate = false },
            onRemove = { vm.removeRating(c.id); showRate = false }
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row {
        Text(label, color = ZMuted, modifier = Modifier.width(110.dp))
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RatingDialog(content: Content, existing: Rating?, onDismiss: () -> Unit, onSave: (Rating) -> Unit, onRemove: () -> Unit) {
    var stars by remember { mutableIntStateOf(existing?.stars ?: 0) }
    var like by remember { mutableIntStateOf(existing?.like ?: 0) }
    var text by remember { mutableStateOf(existing?.review ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = ZSurface,
        title = { Text(stringResource(R.string.rate_title)) },
        text = {
            Column {
                Text(content.title, color = ZMuted)
                Spacer(Modifier.height(10.dp))
                StarRow(stars, { stars = it }, 38.dp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = like == 1, onClick = { like = if (like == 1) 0 else 1 },
                        label = { Text(stringResource(R.string.liked)) },
                        leadingIcon = { Icon(Icons.Filled.ThumbUp, null, Modifier.size(18.dp)) }
                    )
                    FilterChip(
                        selected = like == -1, onClick = { like = if (like == -1) 0 else -1 },
                        label = { Text(stringResource(R.string.disliked)) },
                        leadingIcon = { Icon(Icons.Filled.ThumbDown, null, Modifier.size(18.dp)) }
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = text, onValueChange = { text = it },
                    placeholder = { Text(stringResource(R.string.review_hint)) },
                    modifier = Modifier.fillMaxWidth(), minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(Rating(content.id, stars, like, text.trim(), System.currentTimeMillis())) },
                enabled = stars > 0 || like != 0 || text.isNotBlank()
            ) { Text(stringResource(R.string.send), color = ZRed) }
        },
        dismissButton = {
            Row {
                if (existing != null) TextButton(onClick = onRemove) { Text(stringResource(R.string.remove_rating), color = ZMuted) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = ZMuted) }
            }
        }
    )
}

@Composable
fun EpisodesScreen(vm: AppViewModel, nav: NavController, id: String) {
    val c = vm.content(id)
    if (c == null) {
        LaunchedEffect(Unit) { nav.popBackStack() }
        return
    }
    val context = LocalContext.current
    var expanded by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
            Column(Modifier.padding(start = 8.dp)) {
                Text(c.title, fontSize = 20.sp, color = ZMuted, maxLines = 1)
                Text("${stringResource(R.string.season)} 1", color = ZMuted, fontSize = 15.sp)
            }
        }
        Column(Modifier.verticalScroll(rememberScrollState())) {
            c.episodes.forEach { ep ->
                val open = expanded == ep.id
                val key = vm.dlKey(c.id, ep.id)
                val progress = vm.downloading[key]
                val downloaded = vm.localFile(c.id, ep.id) != null
                CardBox {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.width(110.dp).aspectRatio(16f / 10f).clip(RoundedCornerShape(12.dp))
                                    .background(Brush.verticalGradient(listOf(Color(c.colorSeed), ZBg)))
                                    .clickable { nav.navigate(Routes.player(c.id, ep.id)) },
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text("EP ${ep.season}x${ep.number}", fontWeight = FontWeight.Bold)
                                Text(ep.date, color = ZMuted, fontSize = 12.sp)
                            }
                            IconButton(onClick = { expanded = if (open) null else ep.id }) {
                                Icon(if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null)
                            }
                        }
                        if (open) {
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { openCast(context, vm) }, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Filled.Cast, null, tint = Color.White)
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.act_mirror), color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { vm.startDownload(c, ep) }, enabled = !downloaded && progress == null,
                                    shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Download, null, tint = Color.White)
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.act_download), color = Color.White)
                                }
                            }
                            if (progress != null) {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(progress = { progress }, color = ZRed, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}
