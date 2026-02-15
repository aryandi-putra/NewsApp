package com.hunter.newsapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hunter.newsapp.BuildConfig
import com.hunter.newsapp.data.local.dao.ArticleDao
import com.hunter.newsapp.data.mapper.toDomain
import com.hunter.newsapp.data.mapper.toEntity
import com.hunter.newsapp.data.remote.NewsApiService
import com.hunter.newsapp.domain.model.Article

class SearchPagingSource(
    private val apiService: NewsApiService,
    private val articleDao: ArticleDao,
    private val query: String
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val position = params.key ?: 1
        return try {
            val response = apiService.searchNews(
                query = query,
                page = position,
                pageSize = params.loadSize,
                apiKey = BuildConfig.NEWS_API_KEY
            )
            val articles = response.articles.map { dto ->
                val article = dto.toEntity().toDomain()
                // Check if already bookmarked in DB
                val isBookmarked = articleDao.isBookmarked(article.url) ?: false
                article.copy(isBookmarked = isBookmarked)
            }

            LoadResult.Page(
                data = articles,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (articles.isEmpty()) null else position + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
