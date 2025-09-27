package com.example.evcs_mobileapp.intial

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {

    // System splash gate (minimal boot)
    private val _bootReady = mutableStateOf(false)
    val bootReady: State<Boolean> = _bootReady

    // Custom splash visibility & progress
    private val _progress = MutableStateFlow(0) // 0..100
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _showCustomSplash = MutableStateFlow(true)
    val showCustomSplash: StateFlow<Boolean> = _showCustomSplash.asStateFlow()

    private val prefs: SharedPreferences = app.getSharedPreferences("evcs_prefs", Context.MODE_PRIVATE)
    private val _isFirstLogin = MutableStateFlow(prefs.getBoolean("isFirstLogin", true))
    val isFirstLogin: StateFlow<Boolean> = _isFirstLogin.asStateFlow()

    init {
        startInitialization()
    }

    private fun startInitialization() = viewModelScope.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                initSecureStore()
                quickSessionCheck()
            }
        }
        _bootReady.value = true

        val tasks: List<suspend () -> Unit> = listOf(
            { loadRemoteConfig().also { bump(25) } },
            { warmDatabase().also { bump(50) } },
            { prefetchHome().also { bump(75) } },
            { hydrateUserProfile().also { bump(100) } },
        )
        tasks.forEach { it() }

        _showCustomSplash.value = false // enter the real app
        // Optionally, check first login here if needed
    }

    private fun bump(target: Int) { _progress.value = target }

    // --- Example task stubs (replace with your real work) ---
    private suspend fun initSecureStore() { /* read keystore, keys */ }
    private suspend fun quickSessionCheck() { /* check token in prefs */ }

    private suspend fun loadRemoteConfig() {
        withContext(Dispatchers.IO) { /* fetch */ }
    }
    private suspend fun warmDatabase() {
        withContext(Dispatchers.IO) { /* Room open, pre-load */ }
    }
    private suspend fun prefetchHome() {
        withContext(Dispatchers.IO) { /* API calls */ }
    }
    private suspend fun hydrateUserProfile() {
        withContext(Dispatchers.IO) { /* user */ }
    }

    fun completeWalkthrough() {
        prefs.edit().putBoolean("isFirstLogin", false).apply()
        _isFirstLogin.value = false
    }
}