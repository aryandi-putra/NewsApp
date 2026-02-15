package com.hunter.newsapp.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hunter.newsapp.domain.model.Article
import com.hunter.newsapp.domain.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val repository: NewsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val articleUrl: String = savedStateHandle.get<String>("articleUrl")?.let {
        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
    } ?: ""

    private val _article = MutableStateFlow<Article?>(null)
    val article = _article.asStateFlow()

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            _article.value = repository.getArticleByUrl(articleUrl)
        }
    }

    fun toggleBookmark() {
        val currentArticle = _article.value ?: return
        viewModelScope.launch {
            repository.toggleBookmark(currentArticle)
            // Refresh local state
            _article.value = currentArticle.copy(isBookmarked = !currentArticle.isBookmarked)
        }
    }
}
