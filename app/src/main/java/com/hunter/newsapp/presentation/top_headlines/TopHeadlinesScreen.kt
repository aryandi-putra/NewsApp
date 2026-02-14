package com.hunter.newsapp.presentation.top_headlines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.hunter.newsapp.presentation.common.NewsItem
import com.hunter.newsapp.presentation.common.ErrorState
import com.hunter.newsapp.presentation.common.SizedCircularProgressIndicator
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.foundation.layout.size
import com.hunter.newsapp.domain.model.Article

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TopHeadlinesScreen(
    viewModel: TopHeadlinesViewModel = hiltViewModel(),
    onArticleClick: (Article) -> Unit
) {
    val articles = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val isRefreshing = articles.loadState.refresh is LoadState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { articles.refresh() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Headlines") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(count = articles.itemCount) { index ->
                    val article = articles[index]
                    article?.let {
                        NewsItem(
                            article = it,
                            onClick = { onArticleClick(it) },
                            onBookmarkClick = { viewModel.toggleBookmark(it) }
                        )
                    }
                }

                articles.apply {
                    when {
                        loadState.append is LoadState.Loading -> {
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
                        loadState.refresh is LoadState.Error -> {
                            val e = articles.loadState.refresh as LoadState.Error
                            item {
                                ErrorState(
                                    message = e.error.localizedMessage ?: "Network Failure",
                                    onRetry = { retry() }
                                )
                            }
                        }
                        loadState.append is LoadState.Error -> {
                            val e = articles.loadState.append as LoadState.Error
                            item {
                                ErrorState(
                                    message = e.error.localizedMessage ?: "Failed to load more",
                                    onRetry = { retry() }
                                )
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            
            // Offline/Empty indicator
            if (articles.itemCount == 0 && articles.loadState.refresh is LoadState.NotLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No headlines available offline.")
                }
            }
        }
    }
}

