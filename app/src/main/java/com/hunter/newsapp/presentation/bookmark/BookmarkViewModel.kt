package com.hunter.newsapp.presentation.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hunter.newsapp.domain.model.Article
import com.hunter.newsapp.domain.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    val bookmarkedArticles: Flow<PagingData<Article>> = repository.getBookmarkedArticles()
        .cachedIn(viewModelScope)

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            repository.toggleBookmark(article)
        }
    }
}
