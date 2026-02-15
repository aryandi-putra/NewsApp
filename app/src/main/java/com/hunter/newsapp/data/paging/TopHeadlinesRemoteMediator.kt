package com.hunter.newsapp.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.hunter.newsapp.BuildConfig
import com.hunter.newsapp.data.local.NewsDatabase
import com.hunter.newsapp.data.local.entity.ArticleEntity
import com.hunter.newsapp.data.local.entity.RemoteKey
import com.hunter.newsapp.data.mapper.toEntity
import com.hunter.newsapp.data.remote.NewsApiService

@OptIn(ExperimentalPagingApi::class)
class TopHeadlinesRemoteMediator(
    private val apiService: NewsApiService,
    private val database: NewsDatabase
) : RemoteMediator<Int, ArticleEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                // Always start from page 1 on refresh to avoid data jumping
                1
            }
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                // If no remote key found, we can't append (might be in initial load state)
                if (remoteKeys == null) {
                    return MediatorResult.Success(endOfPaginationReached = false)
                }
                val nextKey = remoteKeys.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                nextKey
            }
        }

        try {
            val response = apiService.getTopHeadlines(
                page = page,
                pageSize = state.config.pageSize,
                apiKey = BuildConfig.NEWS_API_KEY
            )

            // Pre-fetch existing articles before transaction to preserve timestamps/bookmarks
            val existingArticles = mutableMapOf<String, ArticleEntity>()
            for (dto in response.articles) {
                val url = dto.url ?: ""
                if (url.isNotEmpty()) {
                    database.articleDao().getArticleByUrl(url)?.let {
                        existingArticles[url] = it
                    }
                }
            }

            val endOfPaginationReached = response.articles.isEmpty()
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeyDao().clearRemoteKeys()
                    database.articleDao().clearNonBookmarkedHeadlines()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = response.articles.map {
                    RemoteKey(articleUrl = it.url ?: "", prevKey = prevKey, nextKey = nextKey)
                }
                database.remoteKeyDao().insertAll(keys)
                
                // Preserve existing cachedAt and isBookmarked from pre-fetched data
                val articlesToInsert = response.articles.map { dto ->
                    val url = dto.url ?: ""
                    val existing = existingArticles[url]
                    dto.toEntity(isTopHeadline = true).copy(
                        cachedAt = existing?.cachedAt ?: System.currentTimeMillis(),
                        isBookmarked = existing?.isBookmarked ?: false
                    )
                }
                database.articleDao().insertArticles(articlesToInsert)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ArticleEntity>): RemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { article ->
                database.remoteKeyDao().remoteKeysByUrl(article.url)
            }
    }

    private suspend fun getRemoteKeyClosestToPosition(state: PagingState<Int, ArticleEntity>): RemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.url?.let { url ->
                database.remoteKeyDao().remoteKeysByUrl(url)
            }
        }
    }
}
