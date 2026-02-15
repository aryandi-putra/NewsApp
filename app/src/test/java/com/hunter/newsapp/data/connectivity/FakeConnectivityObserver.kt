package com.hunter.newsapp.data.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeConnectivityObserver : ConnectivityObserver {
    private val _isOnline = MutableStateFlow(true)
    override fun observe(): Flow<Boolean> = _isOnline.asStateFlow()
    
    fun setOnline(isOnline: Boolean) {
        _isOnline.value = isOnline
    }
}
