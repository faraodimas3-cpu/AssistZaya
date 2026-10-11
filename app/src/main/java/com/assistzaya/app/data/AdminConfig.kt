package com.assistzaya.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Configuração controlada pela administração.
 * Aponte CONFIG_URL para um JSON do seu servidor, por exemplo: {"vipEnabled": true}
 * Usuários comuns não alteram esse valor: eles só podem ocultar o botão VIP durante a sessão.
 */
object AdminConfig {
    var CONFIG_URL: String = ""

    suspend fun fetchVipEnabled(): Boolean? = withContext(Dispatchers.IO) {
        if (CONFIG_URL.isBlank()) return@withContext null
        try {
            val c = URL(CONFIG_URL).openConnection() as HttpURLConnection
            c.connectTimeout = 8000
            c.readTimeout = 8000
            val text = c.inputStream.bufferedReader().use { it.readText() }
            JSONObject(text).optBoolean("vipEnabled", true)
        } catch (e: Exception) {
            null
        }
    }
}
