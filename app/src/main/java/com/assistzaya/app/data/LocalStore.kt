package com.assistzaya.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Armazenamento local simples (SharedPreferences). Será substituído/complementado pelo backend. */
class LocalStore(context: Context) {
    private val sp = context.getSharedPreferences("assistzaya", Context.MODE_PRIVATE)

    fun getBool(k: String, def: Boolean) = sp.getBoolean(k, def)
    fun setBool(k: String, v: Boolean) = sp.edit().putBoolean(k, v).apply()
    fun getLong(k: String, def: Long) = sp.getLong(k, def)
    fun setLong(k: String, v: Long) = sp.edit().putLong(k, v).apply()

    fun getStringList(k: String): List<String> {
        val a = JSONArray(sp.getString(k, "[]"))
        return List(a.length()) { a.getString(it) }
    }

    fun setStringList(k: String, v: List<String>) =
        sp.edit().putString(k, JSONArray(v).toString()).apply()

    fun loadAccount() = Account(
        sp.getString("acc_name", "") ?: "",
        sp.getString("acc_email", "") ?: "",
        sp.getBoolean("acc_in", false)
    )

    fun saveAccount(a: Account) = sp.edit()
        .putString("acc_name", a.name).putString("acc_email", a.email).putBoolean("acc_in", a.signedIn).apply()

    fun loadRatings(): List<Rating> {
        val a = JSONArray(sp.getString("ratings", "[]"))
        return List(a.length()) {
            val o = a.getJSONObject(it)
            Rating(o.getString("contentId"), o.getInt("stars"), o.getInt("like"), o.optString("review"), o.getLong("time"))
        }
    }

    fun saveRatings(list: List<Rating>) {
        val a = JSONArray()
        list.forEach {
            a.put(
                JSONObject().put("contentId", it.contentId).put("stars", it.stars)
                    .put("like", it.like).put("review", it.review).put("time", it.time)
            )
        }
        sp.edit().putString("ratings", a.toString()).apply()
    }

    fun loadDownloads(): List<DownloadItem> {
        val a = JSONArray(sp.getString("downloads", "[]"))
        return List(a.length()) {
            val o = a.getJSONObject(it)
            DownloadItem(
                o.getString("key"), o.getString("contentId"),
                if (o.isNull("episodeId")) null else o.getString("episodeId"),
                o.getString("title"), o.optString("subtitle"), o.getString("filePath"),
                o.getLong("sizeBytes"), o.getLong("dateMillis")
            )
        }
    }

    fun saveDownloads(list: List<DownloadItem>) {
        val a = JSONArray()
        list.forEach {
            a.put(
                JSONObject().put("key", it.key).put("contentId", it.contentId)
                    .put("episodeId", it.episodeId ?: JSONObject.NULL).put("title", it.title)
                    .put("subtitle", it.subtitle).put("filePath", it.filePath)
                    .put("sizeBytes", it.sizeBytes).put("dateMillis", it.dateMillis)
            )
        }
        sp.edit().putString("downloads", a.toString()).apply()
    }

    fun loadContinue(): List<ContinueEntry> {
        val a = JSONArray(sp.getString("continue", "[]"))
        return List(a.length()) {
            val o = a.getJSONObject(it)
            ContinueEntry(o.getString("contentId"), if (o.isNull("episodeId")) null else o.getString("episodeId"))
        }
    }

    fun saveContinue(list: List<ContinueEntry>) {
        val a = JSONArray()
        list.forEach { a.put(JSONObject().put("contentId", it.contentId).put("episodeId", it.episodeId ?: JSONObject.NULL)) }
        sp.edit().putString("continue", a.toString()).apply()
    }

    fun clearAll() = sp.edit().clear().apply()
}
