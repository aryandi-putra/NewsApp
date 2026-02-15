package com.hunter.newsapp.data.repository

import androidx.paging.PagingData
import com.hunter.newsapp.domain.model.Article
import com.hunter.newsapp.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeNewsRepository : NewsRepository {
    
    private val bookmarks = mutableSetOf<String>()
    private val articles = mutableListOf<Article>()
    private val topHeadlinesFlow = MutableStateFlow<PagingData<Article>>(PagingData.empty())
    private val searchResultsFlow = MutableStateFlow<PagingData<Article>>(PagingData.empty())
    private val bookmarkedArticlesFlow = MutableStateFlow<PagingData<Article>>(PagingData.empty())
    
    var shouldThrowError = false
    var errorToThrow: Exception? = null
    
    fun setTopHeadlines(data: PagingData<Article>) {
        topHeadlinesFlow.value = data
    }
    
    fun setSearchResults(data: PagingData<Article>) {
        searchResultsFlow.value = data
    }
    
    fun setBookmarkedArticles(data: PagingData<Article>) {
        bookmarkedArticlesFlow.value = data
    }
    
    fun addArticles(newArticles: List<Article>) {
        articles.addAll(newArticles)
    }
    
    override fun getTopHeadlines(): Flow<PagingData<Article>> {
        if (shouldThrowError) throw errorToThrow ?: Exception("Network error")
        return topHeadlinesFlow
    }
    
    override fun searchNews(query: String): Flow<PagingData<Article>> {
        if (shouldThrowError) throw errorToThrow ?: Exception("Network error")
        return searchResultsFlow
    }
    
    override fun getBookmarkedArticles(): Flow<PagingData<Article>> {
        if (shouldThrowError) throw errorToThrow ?: Exception("Network error")
        return bookmarkedArticlesFlow
    }
    
    override suspend fun toggleBookmark(article: Article) {
        if (shouldThrowError) throw errorToThrow ?: Exception("Database error")
        
        if (bookmarks.contains(article.url)) {
            bookmarks.remove(article.url)
        } else {
            bookmarks.add(article.url)
        }
        
        // Update the article in the list
        val index = articles.indexOfFirst { it.url == article.url }
        if (index != -1) {
            articles[index] = article.copy(isBookmarked = bookmarks.contains(article.url))
        }
    }
    
    override suspend fun isBookmarked(url: String): Boolean {
        return bookmarks.contains(url)
    }
    
    override suspend fun getArticleByUrl(url: String): Article? {
        return articles.find { it.url == url }
    }
    
    override suspend fun upsertArticle(article: Article) {
        val index = articles.indexOfFirst { it.url == article.url }
        if (index != -1) {
            articles[index] = article
        } else {
            articles.add(article)
        }
    }
    
    fun clearBookmarks() {
        bookmarks.clear()
    }
    
    fun getBookmarkCount(): Int = bookmarks.size
}
