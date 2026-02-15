package com.hunter.newsapp.presentation.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onArticleClick: (Article) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val articles = viewModel.searchResults.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Search News") })
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search for news...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (searchQuery.isBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Type something to search for news.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(count = articles.itemCount) { index ->
                        val article = articles[index]
                        article?.let {
                            NewsItem(
                                article = it,
                                onClick = { article ->
                                    onArticleClick(article)
                                },
                                onBookmarkClick = { viewModel.toggleBookmark(it) }
                            )
                        }
                    }

                    articles.apply {
                        when {
                            loadState.refresh is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier.fillParentMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
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

                if (articles.itemCount == 0 && articles.loadState.refresh is LoadState.NotLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results found for \"$searchQuery\"")
                    }
                }
            }
        }
    }
}
