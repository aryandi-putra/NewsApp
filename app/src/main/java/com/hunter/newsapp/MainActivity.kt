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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val showBottomBar = currentDestination?.route != Screen.Splash.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                val items = listOf(
                                    Triple(Screen.TopHeadlines, "Headlines", Icons.Default.Home),
                                    Triple(Screen.Search, "Search", Icons.Default.Search),
                                    Triple(Screen.Bookmark, "Bookmarks", Icons.Default.Bookmark)
                                )
                                items.forEach { (screen, label, icon) ->
                                    NavigationBarItem(
                                        icon = { Icon(icon, contentDescription = label) },
                                        label = { Text(label) },
                                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
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
                            SearchScreen(
                                onArticleClick = { article ->
                                    navController.navigate(Screen.Detail.createRoute(article.url))
                                }
                            )
                        }
                        composable(route = Screen.Bookmark.route) {
                            BookmarkScreen()
                        }
                        composable(route = Screen.Detail.route) {
                            NewsDetailScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}