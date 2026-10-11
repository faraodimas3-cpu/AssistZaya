package com.assistzaya.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.assistzaya.app.AppViewModel
import com.assistzaya.app.R
import com.assistzaya.app.data.*
import com.assistzaya.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(vm: AppViewModel, nav: NavController, openSheet: (Content) -> Unit) {
    val all = vm.catalog
    val tTopToday = stringResource(R.string.sec_top_today)
    val sTopToday = stringResource(R.string.sec_top_today_sub)
    val tTopViewed = stringResource(R.string.sec_top_viewed)
    val sTopViewed = stringResource(R.string.sec_top_viewed_sub)
    val tNew = stringResource(R.string.sec_new)
    val sNew = stringResource(R.string.sec_new_sub)
    val tTrending = stringResource(R.string.sec_trending)
    val sTrending = stringResource(R.string.sec_trending_sub)
    val tPopular = stringResource(R.string.sec_popular)
    val sPopular = stringResource(R.string.sec_popular_sub)
    val tAnimes = stringResource(R.string.sec_animes)
    val tSeries = stringResource(R.string.sec_series)
    val tMovies = stringResource(R.string.sec_movies)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { ZTopBar(stringResource(R.string.nav_home), nav) }

        if (vm.toggle("home_banners")) {
            val featured = all.filter { TAG_TRENDING in it.tags }.ifEmpty { all.take(5) }
            item { Banners(featured, openSheet) }
        }

        if (vm.toggle("home_continue") && vm.continueEntries.isNotEmpty()) {
            item {
                SectionHeader(stringResource(R.string.sec_continue), stringResource(R.string.sec_continue_sub)) {
                    nav.navigate(Routes.list("continue"))
                }
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(vm.continueEntries.toList(), key = { it.contentId }) { e ->
                        val c = vm.content(e.contentId)
                        if (c != null) {
                            val ep = c.episodes.firstOrNull { it.id == e.episodeId }
                            WideCard(
                                c, if (ep != null) "EP ${ep.season}x${ep.number}" else c.year.toString(),
                                onClick = { nav.navigate(Routes.player(c.id, ep?.id)) },
                                onRemove = { vm.removeContinue(c.id) }
                            )
                        }
                    }
                }
            }
        }

        if (vm.toggle("home_latest_eps")) {
            val eps = all.filter { it.isSeries }.map { it to it.episodes.last() }
            if (eps.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.sec_latest_eps), stringResource(R.string.sec_latest_eps_sub)) }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(eps, key = { it.first.id }) { (c, ep) ->
                            WideCard(c, "EP ${ep.season}x${ep.number}", onClick = { openSheet(c) })
                        }
                    }
                }
            }
        }

        if (vm.toggle("home_reviews")) {
            val reviews = vm.ratings.values.filter { it.review.isNotBlank() }.sortedByDescending { it.time }
            if (reviews.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.sec_reviews), null) }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(reviews, key = { it.contentId }) { r ->
                            val c = vm.content(r.contentId)
                            if (c != null) ReviewCard(vm.account.name.ifBlank { stringResource(R.string.you) }, r, c) { nav.navigate(Routes.details(c.id)) }
                        }
                    }
                }
            }
        }

        if (vm.toggle("home_sections")) {
            if (vm.toggle("sec_top_today")) section(tTopToday, sTopToday, all.filter { TAG_TOP_TODAY in it.tags }, null, openSheet)
            if (vm.toggle("sec_top_viewed")) section(tTopViewed, sTopViewed, all.filter { TAG_TOP_VIEWED in it.tags }, null, openSheet)
            if (vm.toggle("sec_new")) section(tNew, sNew, all.filter { TAG_NEW in it.tags }, { nav.navigate(Routes.list("genre:")) }, openSheet)
            if (vm.toggle("sec_trending")) section(tTrending, sTrending, all.filter { TAG_TRENDING in it.tags }, null, openSheet)
            if (vm.toggle("sec_popular")) section(tPopular, sPopular, all.filter { TAG_POPULAR in it.tags }, null, openSheet)
            if (vm.toggle("sec_animes")) section(tAnimes, null, all.filter { it.type == ContentType.ANIME }, null, openSheet)
            if (vm.toggle("sec_series")) section(tSeries, null, all.filter { it.type == ContentType.SERIE }, null, openSheet)
            if (vm.toggle("sec_movies")) section(tMovies, null, all.filter { it.type == ContentType.FILME }, null, openSheet)
        }
    }
}

private fun LazyListScope.section(
    title: String, sub: String?, list: List<Content>, onMore: (() -> Unit)?, onClick: (Content) -> Unit
) {
    if (list.isEmpty()) return
    item { SectionHeader(title, sub, onMore) }
    item { ContentRow(list, onClick) }
}

@Composable
private fun Banners(list: List<Content>, onClick: (Content) -> Unit) {
    val pager = rememberPagerState { list.size }
    LaunchedEffect(list.size) {
        while (list.size > 1) {
            delay(4500)
            pager.animateScrollToPage((pager.currentPage + 1) % list.size)
        }
    }
    Box(Modifier.fillMaxWidth().height(300.dp)) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { i ->
            val c = list[i]
            Box(Modifier.fillMaxSize().clickable { onClick(c) }) {
                Poster(c, Modifier.fillMaxSize(), wide = true, shape = RectangleShape, showBadge = false)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, ZBg))))
                Box(
                    Modifier.align(Alignment.Center).size(48.dp).clip(CircleShape)
                        .border(2.dp, ZRed, CircleShape).background(Color(0x66000000)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.PlayArrow, null, tint = Color.White) }
                Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.TrendingUp, null, tint = ZRed, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.trending_today), color = ZMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                    }
                    Text(c.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in list.indices) {
                val selected = pager.currentPage == i
                Box(
                    Modifier.height(6.dp).width(if (selected) 28.dp else 6.dp).clip(RoundedCornerShape(3.dp))
                        .background(if (selected) ZRed else ZOutline)
                )
            }
        }
    }
}

@Composable
private fun WideCard(c: Content, subtitle: String, onClick: () -> Unit, onRemove: (() -> Unit)? = null) {
    Column(Modifier.width(240.dp).clickable { onClick() }) {
        Box {
            Poster(c, Modifier.fillMaxWidth().aspectRatio(16f / 9f), wide = true, showBadge = false)
            if (onRemove != null) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(6.dp).size(30.dp).clip(RoundedCornerShape(8.dp))
                        .background(Color(0x99000000)).clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(c.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
        Text(subtitle, color = ZMuted, fontSize = 13.sp)
    }
}

@Composable
private fun ReviewCard(author: String, r: Rating, c: Content, onClick: () -> Unit) {
    CardBox(Modifier.width(300.dp).clickable { onClick() }) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(ZRed), contentAlignment = Alignment.Center) {
                    Text(author.take(1).uppercase(), fontWeight = FontWeight.Bold)
                }
                Column(Modifier.padding(start = 10.dp)) {
                    Text(author, fontWeight = FontWeight.Bold)
                    StarRow(r.stars, null, 14.dp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(c.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(r.review, color = ZMuted, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}
