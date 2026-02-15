package com.hunter.newsapp.presentation.top_headlines

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.hunter.newsapp.domain.model.Article
import com.hunter.newsapp.presentation.common.ErrorState
import com.hunter.newsapp.presentation.common.NewsItem
import com.hunter.newsapp.presentation.common.SizedCircularProgressIndicator
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TopHeadlinesScreen(
    viewModel: TopHeadlinesViewModel = hiltViewModel(),
    onArticleClick: (Article) -> Unit
) {
    val articles = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Only show full-screen loading on explicit pull-to-refresh, not on tab switch
    val isRefreshing by remember {
        derivedStateOf { 
            articles.loadState.refresh is LoadState.Loading && articles.itemCount == 0 
        }
    }
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = articles.loadState.refresh is LoadState.Loading,
        onRefresh = { articles.refresh() }
    )

    // Handle refresh errors
    LaunchedEffect(articles.loadState.refresh) {
        viewModel.handleRefreshError(articles.loadState.refresh)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Collect UI events for Snackbar
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = if (event.actionLabel == null) 
                            SnackbarDuration.Short 
                        else 
                            SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed && event.actionLabel == "Retry") {
                        articles.refresh()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Headlines") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            // Show loading spinner during refresh (clears list first)
            if (isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState
                ) {
                    items(
                        count = articles.itemCount,
                        key = { index -> articles[index]?.url ?: index }
                    ) { index ->
                        val article = articles[index]
                        article?.let {
                            NewsItem(
                                article = it,
                                onClick = { onArticleClick(it) },
                                onBookmarkClick = { viewModel.toggleBookmark(it) }
                            )
                        }
                    }

                    // Endless scroll loading indicator at the BOTTOM
                    when {
                        articles.loadState.append is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SizedCircularProgressIndicator(size = 24.dp)
                                }
                            }
                        }
                        articles.loadState.append is LoadState.Error -> {
                            item {
                                val e = articles.loadState.append as LoadState.Error
                                ErrorState(
                                    message = e.error.localizedMessage ?: "Failed to load more",
                                    onRetry = { articles.retry() }
                                )
                            }
                        }
                    }
                }
            }

            // Pull-to-refresh indicator at the TOP
            PullRefreshIndicator(
                refreshing = articles.loadState.refresh is LoadState.Loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            
            // Empty state (only when not loading)
            if (articles.itemCount == 0 && articles.loadState.refresh !is LoadState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No headlines available.")
                }
            }
        }
    }
}
