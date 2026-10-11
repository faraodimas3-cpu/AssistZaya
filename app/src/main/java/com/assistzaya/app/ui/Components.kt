package com.assistzaya.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.assistzaya.app.R
import com.assistzaya.app.data.Content
import com.assistzaya.app.data.ContentType
import com.assistzaya.app.ui.theme.*

fun typeLabel(t: ContentType): Int = when (t) {
    ContentType.ANIME -> R.string.type_anime
    ContentType.FILME -> R.string.type_movie
    ContentType.SERIE -> R.string.type_series
}

fun typeColor(t: ContentType): Color = when (t) {
    ContentType.ANIME -> Color(0xFFFFC107)
    ContentType.FILME -> Color(0xFF7CB342)
    ContentType.SERIE -> Color(0xFF90A4AE)
}

fun audioColor(a: String): Color = when (a) {
    "DUB" -> Color(0xFF7CB342)
    "LEG" -> Color(0xFF29B6F6)
    else -> Color(0xFFFF8F00)
}

@Composable
fun AgeBadge(age: String, modifier: Modifier = Modifier) {
    val c = when (age) {
        "L" -> Color(0xFF2E7D32)
        "10" -> Color(0xFF1565C0)
        "12" -> Color(0xFFF9A825)
        "14" -> Color(0xFFEF6C00)
        "16" -> Color(0xFFC62828)
        else -> Color.Black
    }
    Box(
        modifier.size(24.dp).clip(RoundedCornerShape(7.dp)).background(c.copy(alpha = 0.88f)),
        contentAlignment = Alignment.Center
    ) { Text(age, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
}

/** Capa do conteúdo. Sem imagem, mostra um degradê com o título (útil no catálogo de demonstração). */
@Composable
fun Poster(
    content: Content,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
    shape: Shape = RoundedCornerShape(14.dp),
    showBadge: Boolean = true
) {
    val url = if (wide) content.bannerUrl ?: content.posterUrl else content.posterUrl
    Box(
        modifier.clip(shape).then(if (shape == RectangleShape) Modifier else Modifier.border(1.dp, ZOutline, shape))
    ) {
        if (url != null) {
            AsyncImage(
                model = url, contentDescription = content.title,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(content.colorSeed), ZBg))),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    content.title, color = Color.White, fontWeight = FontWeight.Bold,
                    maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(10.dp)
                )
            }
        }
        if (showBadge) AgeBadge(content.ageRating, Modifier.align(Alignment.TopStart).padding(8.dp))
    }
}

@Composable
fun MetaLine(c: Content) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(typeLabel(c.type)), color = typeColor(c.type), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(" | ", color = ZMuted, fontSize = 11.sp)
        Text(c.audio, color = audioColor(c.audio), fontSize = 11.sp)
        Spacer(Modifier.weight(1f))
        Text(c.year.toString(), color = ZMuted, fontSize = 11.sp)
    }
}

@Composable
fun ContentCard(c: Content, onClick: () -> Unit, width: Dp = 118.dp) {
    Column(Modifier.width(width).clickable { onClick() }) {
        Poster(c, Modifier.fillMaxWidth().aspectRatio(2f / 3f))
        Spacer(Modifier.height(6.dp))
        Text(c.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
        MetaLine(c)
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String?, onMore: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 18.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, fontSize = 13.sp, color = ZMuted)
        }
        if (onMore != null) {
            TextButton(onClick = onMore) {
                Text(stringResource(R.string.more), color = ZRed)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = ZRed, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ContentRow(list: List<Content>, onClick: (Content) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(list, key = { it.id }) { c -> ContentCard(c, onClick = { onClick(c) }) }
    }
}

@Composable
fun StarRow(value: Int, onChange: ((Int) -> Unit)?, size: Dp = 24.dp) {
    Row {
        for (i in 1..5) {
            val mod = Modifier.size(size).let { if (onChange != null) it.clickable { onChange(i) } else it }
            Icon(if (i <= value) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = ZGold, modifier = mod)
        }
    }
}

@Composable
fun ZTopBar(title: String, nav: NavController) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) { Icon(Icons.Filled.Settings, stringResource(R.string.settings_title)) }
        Text(title, fontSize = 20.sp, color = ZMuted, modifier = Modifier.weight(1f).padding(start = 8.dp))
        IconButton(onClick = { nav.goTab(Routes.DOWNLOADS) }) { Icon(Icons.Filled.Download, stringResource(R.string.nav_downloads)) }
        IconButton(onClick = { nav.navigate(Routes.list("history")) }) { Icon(Icons.Filled.History, stringResource(R.string.history)) }
        IconButton(onClick = { nav.goTab(Routes.EXPLORE) }) { Icon(Icons.Filled.Search, stringResource(R.string.search_hint)) }
    }
}

@Composable
fun SimpleTopBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
        Text(title, fontSize = 20.sp, color = ZMuted, modifier = Modifier.weight(1f).padding(start = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        actions()
    }
}

@Composable
fun BottomBar(nav: NavController, route: String?) {
    val tabs: List<Triple<String, Int, ImageVector>> = listOf(
        Triple(Routes.HOME, R.string.nav_home, Icons.Filled.Home),
        Triple(Routes.GENRES, R.string.nav_genres, Icons.Filled.GridView),
        Triple(Routes.EXPLORE, R.string.nav_explore, Icons.Filled.Explore),
        Triple(Routes.DOWNLOADS, R.string.nav_downloads, Icons.Filled.Download),
        Triple(Routes.ACCOUNT, R.string.nav_account, Icons.Filled.AccountCircle)
    )
    NavigationBar(containerColor = ZBg) {
        tabs.forEach { (r, label, icon) ->
            NavigationBarItem(
                selected = route == r,
                onClick = { nav.goTab(r) },
                icon = { Icon(icon, null) },
                label = { Text(stringResource(label), fontSize = 11.sp, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White, selectedTextColor = Color.White,
                    indicatorColor = ZSurface2, unselectedIconColor = ZMuted, unselectedTextColor = ZMuted
                )
            )
        }
    }
}

@Composable
fun SwitchRow(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            if (subtitle != null) Text(subtitle, color = ZMuted, fontSize = 13.sp)
        }
        Switch(
            checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = ZRed, checkedThumbColor = Color.White)
        )
    }
}

@Composable
fun CardBox(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp)).background(ZSurface).border(1.dp, ZOutline, RoundedCornerShape(18.dp)),
        content = content
    )
}

@Composable
fun IconBubble(icon: ImageVector, size: Dp = 44.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(ZRedDark), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(size * 0.5f))
    }
}
