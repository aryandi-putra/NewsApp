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
                val remoteKeys = getRemoteKeyClosestToPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val response = apiService.getTopHeadlines(
                page = page,
                pageSize = state.config.pageSize,
                apiKey = BuildConfig.NEWS_API_KEY
            )

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
                database.articleDao().insertArticles(response.articles.map { it.toEntity(isTopHeadline = true) })
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
