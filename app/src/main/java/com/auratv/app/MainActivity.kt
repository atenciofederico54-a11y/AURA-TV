package com.auratv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.auratv.app.data.CatalogRepository
import com.auratv.app.data.LiveChannel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { AuraTvApp() } }
}

private const val PLAYLIST_URL = "https://iptv-org.github.io/iptv/index.m3u"
private enum class Section(val label: String) { HOME("Inicio"), LIVE("TV en vivo"), MOVIES("Películas"), SERIES("Series"), FAVORITES("Favoritos"), SEARCH("Buscar") }
private data class Card(val title: String, val group: String, val url: String? = null)
private val placeholders = listOf(Card("Continuar viendo", "Historial"), Card("Películas", "Colección"), Card("Series", "Colección"))

@Composable private fun AuraTvApp() {
    val repository = remember { CatalogRepository() }; val scope = rememberCoroutineScope()
    var section by remember { mutableStateOf(Section.HOME) }; var selected by remember { mutableStateOf<Card?>(null) }
    var channels by remember { mutableStateOf<List<LiveChannel>>(emptyList()) }; var loading by remember { mutableStateOf(true) }; var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { scope.launch { runCatching { repository.loadM3u(PLAYLIST_URL) }.onSuccess { channels = it }.onFailure { error = "No se pudo actualizar la lista." }; loading = false } }
    selected?.url?.let { FullScreenPlayer(it) { selected = null }; return }
    Row(Modifier.fillMaxSize().background(Color(0xFF09101A))) {
        Navigation(section) { section = it; selected = null }
        Column(Modifier.fillMaxSize().padding(36.dp)) {
            Text("AURA TV", color = Color(0xFFB6F500), fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(18.dp)); Text(section.label, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp)); Text("Usá las flechas y Aceptar del control remoto.", color = Color(0xFFB7C0CD)); Spacer(Modifier.height(32.dp))
            Catalog(section, channels, loading, error) { selected = it }
        }
    }
}

@Composable private fun Navigation(selected: Section, onSelect: (Section) -> Unit) = Column(Modifier.width(190.dp).fillMaxHeight().background(Color(0xFF101B2A)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Section.entries.forEach { item -> Text(item.label, color = if (item == selected) Color(0xFF09101A) else Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (item == selected) Color(0xFFB6F500) else Color.Transparent).clickable { onSelect(item) }.focusable().padding(14.dp)) }
}

@Composable private fun Catalog(section: Section, channels: List<LiveChannel>, loading: Boolean, error: String?, onSelect: (Card) -> Unit) {
    if (section == Section.LIVE && loading) { Text("Actualizando canales…", color = Color(0xFFB7C0CD)); return }
    if (section == Section.LIVE && error != null) { Text(error, color = Color(0xFFB7C0CD)); return }
    val live = channels.map { Card(it.name, it.group, it.streamUrl) }
    val visible = when (section) { Section.HOME -> placeholders + live.take(20); Section.LIVE -> live; Section.SEARCH -> placeholders + live; Section.MOVIES -> placeholders.filter { it.title == "Películas" }; Section.SERIES -> placeholders.filter { it.title == "Series" }; Section.FAVORITES -> emptyList() }
    if (visible.isEmpty()) Text("Todavía no hay contenido en esta sección.", color = Color(0xFFB7C0CD)) else LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) { items(visible) { card ->
        Column(Modifier.size(230.dp, 150.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF1D2B3E)).clickable { onSelect(card) }.focusable().padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) { Text(card.group.uppercase(), color = Color(0xFFB6F500), fontSize = 12.sp); Text(card.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
    } }
}

@Composable private fun FullScreenPlayer(url: String, onExit: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = remember(url) { ExoPlayer.Builder(context).build().apply { setMediaItem(MediaItem.fromUri(url)); prepare(); playWhenReady = true } }
    var failure by remember(url) { mutableStateOf<String?>(null) }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                failure = "No se pudo reproducir este canal. Probá otro o verificá que la transmisión siga disponible."
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(factory = { PlayerView(it).apply { this.player = player; useController = true } }, modifier = Modifier.fillMaxSize())
        Column(Modifier.padding(20.dp)) {
            Text("Volver", color = Color.White, modifier = Modifier.clickable { onExit() }.focusable().padding(10.dp))
            failure?.let { Text(it, color = Color.White, modifier = Modifier.background(Color(0xCC7A1C1C)).padding(12.dp)) }
        }
    }
}
