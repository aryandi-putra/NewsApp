package com.hunter.newsapp.data.mapper

import com.hunter.newsapp.data.local.entity.ArticleEntity
import com.hunter.newsapp.data.remote.model.ArticleDto
import com.hunter.newsapp.domain.model.Article

fun ArticleDto.toEntity(isTopHeadline: Boolean = false): ArticleEntity {
    return ArticleEntity(
        url = url ?: "",
        title = title ?: "",
        description = description,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = content,
        author = author,
        sourceName = source?.name,
        isTopHeadline = isTopHeadline
    )
}

fun ArticleEntity.toDomain(): Article {
    return Article(
        url = url,
        title = title,
        description = description,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = content,
        author = author,
        sourceName = sourceName,
        isBookmarked = isBookmarked
    )
}

fun Article.toEntity(isTopHeadline: Boolean = false): ArticleEntity {
    return ArticleEntity(
        url = url,
        title = title,
        description = description,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = content,
        author = author,
        sourceName = sourceName,
        isBookmarked = isBookmarked,
        isTopHeadline = isTopHeadline
    )
}
