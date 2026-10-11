package com.assistzaya.app.ui

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.assistzaya.app.AppViewModel
import com.assistzaya.app.R
import com.assistzaya.app.data.*
import com.assistzaya.app.ui.theme.*

@Composable
fun GenresScreen(vm: AppViewModel, nav: NavController) {
    val genres = (Catalog.baseGenres + vm.catalog.flatMap { it.genres }).distinct().sorted()
    Column(Modifier.fillMaxSize()) {
        ZTopBar(stringResource(R.string.nav_genres), nav)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(genres) { g ->
                val seed = vm.catalog.firstOrNull { g in it.genres }?.colorSeed
                    ?: (0xFF000000.toInt() or ((g.hashCode() * 2654435761L).toInt() and 0x00606060))
                Box(
                    Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(16.dp))
                        .border(1.dp, ZOutline, RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(seed), ZBg)))
                        .clickable { nav.navigate(Routes.list("genre:$g")) }
                ) {
                    Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(g, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreScreen(vm: AppViewModel, nav: NavController, openSheet: (Content) -> Unit) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<ContentType?>(null) }
    val results = vm.catalog.filter {
        (filter == null || it.type == filter) && (query.isBlank() || it.title.contains(query, ignoreCase = true))
    }
    Column(Modifier.fillMaxSize()) {
        ZTopBar(stringResource(R.string.nav_explore), nav)
        OutlinedTextField(
            value = query, onValueChange = { query = it }, singleLine = true,
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
        )
        LazyRow(contentPadding = PaddingValues(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text(stringResource(R.string.filter_all)) }) }
            item { FilterChip(selected = filter == ContentType.ANIME, onClick = { filter = ContentType.ANIME }, label = { Text(stringResource(R.string.sec_animes)) }) }
            item { FilterChip(selected = filter == ContentType.FILME, onClick = { filter = ContentType.FILME }, label = { Text(stringResource(R.string.sec_movies)) }) }
            item { FilterChip(selected = filter == ContentType.SERIE, onClick = { filter = ContentType.SERIE }, label = { Text(stringResource(R.string.sec_series)) }) }
        }
        PosterGrid(results, openSheet)
    }
}

@Composable
fun PosterGrid(list: List<Content>, onClick: (Content) -> Unit) {
    if (list.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.empty_list), color = ZMuted)
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(list, key = { it.id }) { c -> ContentCard(c, onClick = { onClick(c) }, width = 110.dp) }
    }
}

/** Lista genérica: minha lista, favoritos, histórico, continuar assistindo e gêneros. */
@Composable
fun ListScreen(vm: AppViewModel, nav: NavController, kindArg: String, openSheet: (Content) -> Unit) {
    val kind = Uri.decode(kindArg)
    val title: String
    val list: List<Content>
    when {
        kind == "mylist" -> { title = stringResource(R.string.my_list); list = vm.myList.mapNotNull { vm.content(it) } }
        kind == "favorites" -> { title = stringResource(R.string.favorites); list = vm.favorites.mapNotNull { vm.content(it) } }
        kind == "history" -> { title = stringResource(R.string.history); list = vm.history.mapNotNull { vm.content(it) } }
        kind == "continue" -> { title = stringResource(R.string.sec_continue); list = vm.continueEntries.mapNotNull { vm.content(it.contentId) } }
        kind.startsWith("genre:") -> {
            val g = kind.removePrefix("genre:")
            title = g.ifBlank { stringResource(R.string.sec_new) }
            list = if (g.isBlank()) vm.catalog.filter { TAG_NEW in it.tags } else vm.catalog.filter { g in it.genres }
        }
        else -> { title = ""; list = emptyList() }
    }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(title, onBack = { nav.popBackStack() }) {
            if (kind == "history" || kind == "continue") {
                IconButton(onClick = { if (kind == "history") vm.clearHistory() else vm.clearContinue() }) {
                    Icon(Icons.Filled.Delete, stringResource(R.string.remove))
                }
            }
        }
        PosterGrid(list, openSheet)
    }
}
