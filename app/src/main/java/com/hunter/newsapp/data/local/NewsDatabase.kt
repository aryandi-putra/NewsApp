package com.hunter.newsapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hunter.newsapp.data.local.dao.ArticleDao
import com.hunter.newsapp.data.local.dao.RemoteKeyDao
import com.hunter.newsapp.data.local.entity.ArticleEntity
import com.hunter.newsapp.data.local.entity.RemoteKey

@Database(entities = [ArticleEntity::class, RemoteKey::class], version = 1, exportSchema = false)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
