package com.hunter.newsapp.presentation.top_headlines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hunter.newsapp.data.connectivity.ConnectivityObserver
import com.hunter.newsapp.domain.model.Article
import com.hunter.newsapp.domain.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class UiEvent {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : UiEvent()
}

@HiltViewModel
class TopHeadlinesViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    val pagingDataFlow: Flow<PagingData<Article>> = repository.getTopHeadlines()
        .cachedIn(viewModelScope)

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(replay = 0)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var wasOffline = false

    init {
        viewModelScope.launch {
            connectivityObserver.observe().collect { isOnline ->
                _isOnline.value = isOnline
                if (!isOnline) {
                    wasOffline = true
                    _uiEvent.emit(UiEvent.ShowSnackbar(
                        message = "You're offline. Showing cached data.",
                        actionLabel = "Dismiss"
                    ))
                } else if (wasOffline) {
                    wasOffline = false
                    _uiEvent.emit(UiEvent.ShowSnackbar(
                        message = "Back online",
                        actionLabel = null
                    ))
                }
            }
        }
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            repository.toggleBookmark(article)
        }
    }

    fun handleRefreshError(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            viewModelScope.launch {
                val message = when (loadState.error) {
                    is IOException -> "Network error. Showing cached data."
                    is HttpException -> "Server error. Showing cached data."
                    else -> "Error loading data. Showing cached data."
                }
                _uiEvent.emit(UiEvent.ShowSnackbar(
                    message = message,
                    actionLabel = "Retry"
                ))
            }
        }
    }
}
