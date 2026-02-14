package com.hunter.newsapp.data.remote

import com.hunter.newsapp.data.remote.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String = "technology",
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 5,
        @Query("apiKey") apiKey: String
    ): NewsResponse

    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 5,
        @Query("apiKey") apiKey: String
    ): NewsResponse
}
