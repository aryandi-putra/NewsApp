package com.hunter.newsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hunter.newsapp.presentation.bookmark.BookmarkScreen
import com.hunter.newsapp.presentation.detail.NewsDetailScreen
import com.hunter.newsapp.presentation.navigation.Screen
import com.hunter.newsapp.presentation.search.SearchScreen
import com.hunter.newsapp.presentation.splash.SplashScreen
import com.hunter.newsapp.presentation.top_headlines.TopHeadlinesScreen
import com.hunter.newsapp.ui.theme.NewsAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsAppTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(route = Screen.Splash.route) {
                            SplashScreen(navController = navController)
                        }
                        composable(route = Screen.TopHeadlines.route) {
                            TopHeadlinesScreen(
                                onArticleClick = { article ->
                                    navController.navigate(Screen.Detail.createRoute(article.url))
                                }
                            )
                        }
                        composable(route = Screen.Search.route) {
                            SearchScreen()
                        }
                        composable(route = Screen.Bookmark.route) {
                            BookmarkScreen()
                        }
                        composable(route = Screen.Detail.route) { backStackEntry ->
                            val articleUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""
                            NewsDetailScreen(articleUrl = articleUrl)
                        }
                    }
                }
            }
        }
    }
}