package com.hunter.newsapp.data.repository

import androidx.paging.*
import com.hunter.newsapp.data.local.NewsDatabase
import com.hunter.newsapp.data.mapper.toDomain
import com.hunter.newsapp.data.mapper.toEntity
import com.hunter.newsapp.data.paging.SearchPagingSource
import com.hunter.newsapp.data.paging.TopHeadlinesRemoteMediator
import com.hunter.newsapp.data.remote.NewsApiService
import com.hunter.newsapp.domain.model.Article
import com.hunter.newsapp.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val apiService: NewsApiService,
    private val database: NewsDatabase
) : NewsRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getTopHeadlines(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 5, prefetchDistance = 1),
            remoteMediator = TopHeadlinesRemoteMediator(apiService, database),
            pagingSourceFactory = { database.articleDao().getTopHeadlines() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun searchNews(query: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 5, prefetchDistance = 1),
            pagingSourceFactory = { SearchPagingSource(apiService, database.articleDao(), query) }
        ).flow
    }

    override fun getBookmarkedArticles(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = { database.articleDao().getBookmarkedArticlesPaging() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override suspend fun toggleBookmark(article: Article) {
        val isBookmarked = database.articleDao().isBookmarked(article.url) ?: false
        if (database.articleDao().getArticleByUrl(article.url) == null) {
            database.articleDao().insertArticles(listOf(article.toEntity().copy(isBookmarked = !isBookmarked)))
        } else {
            database.articleDao().updateBookmarkStatus(article.url, !isBookmarked)
        }
    }

    override suspend fun isBookmarked(url: String): Boolean {
        return database.articleDao().isBookmarked(url) ?: false
    }

    override suspend fun getArticleByUrl(url: String): Article? {
        return database.articleDao().getArticleByUrl(url)?.toDomain()
    }

    override suspend fun upsertArticle(article: Article) {
        if (database.articleDao().getArticleByUrl(article.url) == null) {
            database.articleDao().insertArticles(listOf(article.toEntity()))
        }
    }
}
