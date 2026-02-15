package com.hunter.newsapp.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.hunter.newsapp.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Query("SELECT * FROM articles WHERE isTopHeadline = 1 ORDER BY cachedAt ASC")
    fun getTopHeadlines(): PagingSource<Int, ArticleEntity>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY cachedAt DESC")
    fun getBookmarkedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY cachedAt DESC")
    fun getBookmarkedArticlesPaging(): PagingSource<Int, ArticleEntity>

    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE url = :url")
    suspend fun updateBookmarkStatus(url: String, isBookmarked: Boolean)

    @Query("SELECT isBookmarked FROM articles WHERE url = :url")
    suspend fun isBookmarked(url: String): Boolean?

    @Query("DELETE FROM articles WHERE isTopHeadline = 1 AND isBookmarked = 0")
    suspend fun clearNonBookmarkedHeadlines()

    @Query("SELECT * FROM articles WHERE url = :url")
    suspend fun getArticleByUrl(url: String): ArticleEntity?
}
