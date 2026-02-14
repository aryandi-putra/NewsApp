package com.hunter.newsapp.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object TopHeadlines : Screen("top_headlines")
    object Search : Screen("search")
    object Bookmark : Screen("bookmark")
    object Detail : Screen("detail/{articleUrl}") {
        fun createRoute(articleUrl: String) = "detail/$articleUrl"
    }
}
