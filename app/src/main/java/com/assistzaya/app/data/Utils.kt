package com.assistzaya.app.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.text.DateFormat
import java.util.Date
import java.util.Locale

fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val net = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(net) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

fun isMetered(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.isActiveNetworkMetered
}

fun formatSize(bytes: Long): String {
    val mb = bytes / 1048576.0
    return if (mb >= 1024) String.format(Locale.getDefault(), "%.2f GB", mb / 1024)
    else String.format(Locale.getDefault(), "%.1f MB", mb)
}

fun formatDate(ms: Long): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(ms))

fun formatTime(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, sec)
    else String.format(Locale.US, "%d:%02d", m, sec)
}
