package com.assistzaya.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.assistzaya.app.AppViewModel
import com.assistzaya.app.R
import com.assistzaya.app.data.formatDate
import com.assistzaya.app.data.formatSize
import com.assistzaya.app.ui.theme.*

@Composable
fun DownloadsScreen(vm: AppViewModel, nav: NavController) {
    Column(Modifier.fillMaxSize()) {
        ZTopBar(stringResource(R.string.downloads_title), nav)
        Text(
            stringResource(R.string.download_note), color = ZMuted, fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (vm.downloads.isEmpty() && vm.downloading.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.downloads_empty), color = ZMuted)
            }
            return
        }

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            // Em andamento
            items(vm.downloading.keys.toList(), key = { "dl_$it" }) { key ->
                val p = vm.downloading[key] ?: 0f
                CardBox {
                    Column(Modifier.padding(14.dp)) {
                        Text(key.substringBefore("_").let { id -> vm.content(id)?.title ?: id }, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.downloading), color = ZMuted, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { p }, color = ZRed, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            // Concluídos
            items(vm.downloads.toList(), key = { it.key }) { d ->
                CardBox {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { nav.navigate(Routes.player(d.contentId, d.episodeId)) }) {
                            IconBubble(Icons.Filled.PlayArrow, 44.dp)
                        }
                        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            Text(d.title, fontWeight = FontWeight.Bold)
                            if (d.subtitle.isNotBlank()) Text(d.subtitle, color = ZMuted, fontSize = 13.sp)
                            Text(stringResource(R.string.downloaded_on, formatDate(d.dateMillis)), color = ZMuted, fontSize = 12.sp)
                            Text(formatSize(d.sizeBytes), color = ZMuted, fontSize = 12.sp)
                            Text(stringResource(R.string.watch_offline), color = ZRed, fontSize = 12.sp,
                                modifier = Modifier.clickable { nav.navigate(Routes.player(d.contentId, d.episodeId)) })
                        }
                        IconButton(onClick = { vm.removeDownload(d) }) {
                            Icon(Icons.Filled.Delete, stringResource(R.string.remove), tint = ZMuted)
                        }
                    }
                }
            }
        }
    }
}
