package com.hunter.newsapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKey(
    @PrimaryKey val articleUrl: String,
    val prevKey: Int?,
    val nextKey: Int?
)
