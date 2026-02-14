package com.hunter.newsapp.domain.model

data class Article(
    val url: String,
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?,
    val author: String?,
    val sourceName: String?,
    val isBookmarked: Boolean = false
)
