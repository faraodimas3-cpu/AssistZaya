package com.assistzaya.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.assistzaya.app.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val store = LocalStore(app)

    val catalog: List<Content> = Catalog.items
    fun content(id: String): Content? = catalog.firstOrNull { it.id == id }

    /** Mensagens curtas (ids de string) mostradas como Toast pela interface. */
    val messages = MutableSharedFlow<Int>(extraBufferCapacity = 8)

    // ---------- Configurações ----------
    val toggles = mutableStateMapOf<String, Boolean>()
    fun toggle(key: String): Boolean = toggles[key] ?: Toggles.default(key)
    fun setToggle(key: String, value: Boolean) {
        toggles[key] = value
        store.setBool(key, value)
    }

    // ---------- VIP (controlado pela administração) ----------
    var vipEnabled by mutableStateOf(true)
    var vipHidden by mutableStateOf(false)

    // ---------- Conta ----------
    var account by mutableStateOf(store.loadAccount())
    fun signIn(name: String, email: String) {
        account = Account(name.ifBlank { email.substringBefore('@') }, email, true)
        store.saveAccount(account)
    }
    fun signOut() {
        account = Account()
        store.saveAccount(account)
    }

    // ---------- Listas ----------
    val myList = mutableStateListOf<String>().also { it.addAll(store.getStringList("mylist")) }
    val favorites = mutableStateListOf<String>().also { it.addAll(store.getStringList("favorites")) }
    val history = mutableStateListOf<String>().also { it.addAll(store.getStringList("history")) }

    private fun toggleId(list: MutableList<String>, key: String, id: String) {
        if (!list.remove(id)) list.add(0, id)
        store.setStringList(key, list)
    }
    fun toggleMyList(id: String) = toggleId(myList, "mylist", id)
    fun toggleFavorite(id: String) = toggleId(favorites, "favorites", id)

    fun addHistory(id: String) {
        history.remove(id)
        history.add(0, id)
        while (history.size > 100) history.removeAt(history.size - 1)
        store.setStringList("history", history)
    }
    fun clearHistory() {
        history.clear()
        store.setStringList("history", history)
    }

    // ---------- Continuar assistindo ----------
    val continueEntries = mutableStateListOf<ContinueEntry>().also { it.addAll(store.loadContinue()) }
    fun addContinue(contentId: String, episodeId: String?) {
        continueEntries.removeAll { it.contentId == contentId }
        continueEntries.add(0, ContinueEntry(contentId, episodeId))
        store.saveContinue(continueEntries)
    }
    fun removeContinue(contentId: String) {
        continueEntries.removeAll { it.contentId == contentId }
        store.saveContinue(continueEntries)
    }
    fun clearContinue() {
        continueEntries.clear()
        store.saveContinue(continueEntries)
    }

    private fun posKey(contentId: String, episodeId: String?) = "pos_${contentId}_${episodeId ?: "movie"}"
    fun getPosition(contentId: String, episodeId: String?): Long = store.getLong(posKey(contentId, episodeId), 0L)
    fun savePosition(contentId: String, episodeId: String?, ms: Long) =
        store.setLong(posKey(contentId, episodeId), ms)

    // ---------- Avaliações ----------
    val ratings = mutableStateMapOf<String, Rating>().also { m -> store.loadRatings().forEach { m[it.contentId] = it } }
    fun rate(r: Rating) {
        ratings[r.contentId] = r
        store.saveRatings(ratings.values.toList())
    }
    fun removeRating(contentId: String) {
        ratings.remove(contentId)
        store.saveRatings(ratings.values.toList())
    }
    /** Média e quantidade. Com backend, isso virá agregado do servidor. */
    fun average(contentId: String): Pair<Float, Int> {
        val r = ratings[contentId]
        return if (r != null && r.stars > 0) r.stars.toFloat() to 1 else 0f to 0
    }

    // ---------- Downloads offline ----------
    fun dlKey(contentId: String, episodeId: String?) = "${contentId}_${episodeId ?: "movie"}"

    val downloads = mutableStateListOf<DownloadItem>().also { list ->
        list.addAll(store.loadDownloads().filter { File(it.filePath).exists() })
    }
    val downloading = mutableStateMapOf<String, Float>()

    fun localFile(contentId: String, episodeId: String?): File? =
        downloads.firstOrNull { it.key == dlKey(contentId, episodeId) }
            ?.let { File(it.filePath) }?.takeIf { it.exists() }

    fun downloadsSize(): Long = downloads.sumOf { it.sizeBytes }

    fun startDownload(content: Content, episode: Episode?) {
        val key = dlKey(content.id, episode?.id)
        if (downloading.containsKey(key) || downloads.any { it.key == key }) {
            messages.tryEmit(R.string.download_exists); return
        }
        val ctx = getApplication<Application>()
        if (!isOnline(ctx)) { messages.tryEmit(R.string.no_connection_toast); return }
        if (toggle("dl_wifi_only") && isMetered(ctx)) { messages.tryEmit(R.string.download_wifi_only_msg); return }
        val url = episode?.videoUrl ?: content.videoUrl ?: return

        downloading[key] = 0f
        messages.tryEmit(R.string.download_started)
        viewModelScope.launch(Dispatchers.IO) {
            val dir = File(ctx.filesDir, "downloads").apply { mkdirs() }
            val file = File(dir, "$key.mp4")
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 20000
                val total = conn.contentLengthLong
                var done = 0L
                var lastPct = -1
                conn.inputStream.use { input ->
                    file.outputStream().use { out ->
                        val buf = ByteArray(64 * 1024)
                        while (true) {
                            val n = input.read(buf)
                            if (n < 0) break
                            out.write(buf, 0, n)
                            done += n
                            if (total > 0) {
                                val pct = (done * 100 / total).toInt()
                                if (pct != lastPct) {
                                    lastPct = pct
                                    withContext(Dispatchers.Main) { downloading[key] = pct / 100f }
                                }
                            }
                        }
                    }
                }
                val sub = episode?.let { "EP ${it.season}x${it.number}" } ?: ""
                val item = DownloadItem(key, content.id, episode?.id, content.title, sub, file.absolutePath, file.length(), System.currentTimeMillis())
                withContext(Dispatchers.Main) {
                    downloads.add(0, item)
                    downloading.remove(key)
                    store.saveDownloads(downloads)
                    messages.tryEmit(R.string.download_done)
                }
            } catch (e: Exception) {
                file.delete()
                withContext(Dispatchers.Main) {
                    downloading.remove(key)
                    messages.tryEmit(R.string.download_failed)
                }
            }
        }
    }

    fun removeDownload(item: DownloadItem) {
        File(item.filePath).delete()
        downloads.remove(item)
        store.saveDownloads(downloads)
    }

    // ---------- Privacidade: apagar dados locais ----------
    fun deleteMyData() {
        downloads.toList().forEach { File(it.filePath).delete() }
        downloads.clear(); myList.clear(); favorites.clear(); history.clear()
        continueEntries.clear(); ratings.clear()
        store.clearAll()
        account = Account()
        toggles.clear()
        Toggles.all.forEach { toggles[it] = Toggles.default(it) }
        messages.tryEmit(R.string.data_deleted)
    }

    init {
        Toggles.all.forEach { toggles[it] = store.getBool(it, Toggles.default(it)) }
        viewModelScope.launch { AdminConfig.fetchVipEnabled()?.let { vipEnabled = it } }
    }
}
