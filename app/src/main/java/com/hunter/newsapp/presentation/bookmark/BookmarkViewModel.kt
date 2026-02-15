package com.hunter.newsapp.presentation.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hunter.newsapp.domain.model.Article
import com.hunter.newsapp.domain.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    // Use replay = 1 so new collectors get the last emission
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    val bookmarkedArticles: Flow<PagingData<Article>> = refreshTrigger
        .flatMapLatest { repository.getBookmarkedArticles() }
        .cachedIn(viewModelScope)

    init {
        // Trigger initial load
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            repository.toggleBookmark(article)
            // Refresh the list after toggling
            refreshTrigger.emit(Unit)
        }
    }
}
