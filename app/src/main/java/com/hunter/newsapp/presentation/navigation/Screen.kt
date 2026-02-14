package com.hunter.newsapp.presentation.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object TopHeadlines : Screen("top_headlines")
    object Search : Screen("search")
    object Bookmark : Screen("bookmark")
    object Detail : Screen("detail/{articleUrl}") {
        fun createRoute(articleUrl: String): String {
            val encodedUrl = URLEncoder.encode(articleUrl, StandardCharsets.UTF_8.toString())
            return "detail/$encodedUrl"
        }
    }
}
