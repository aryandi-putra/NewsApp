package com.hunter.newsapp.domain.repository

import androidx.paging.PagingData
import com.hunter.newsapp.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getTopHeadlines(): Flow<PagingData<Article>>
    fun searchNews(query: String): Flow<PagingData<Article>>
    fun getBookmarkedArticles(): Flow<PagingData<Article>>
    suspend fun toggleBookmark(article: Article)
    suspend fun isBookmarked(url: String): Boolean
    suspend fun getArticleByUrl(url: String): Article?
}
