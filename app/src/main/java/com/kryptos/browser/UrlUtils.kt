package com.kryptos.browser

import android.net.Uri

object UrlUtils {
    fun getBaseDomain(url: String): String? {
        return try {
            var host = Uri.parse(url).host ?: return null
            if (host.startsWith("www.")) host = host.substring(4)
            host.lowercase()
        } catch (e: Exception) {
            null
        }
    }
}
