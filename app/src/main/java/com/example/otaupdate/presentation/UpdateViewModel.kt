package com.example.otaupdate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otaupdate.domain.UpdateInfo
import com.example.otaupdate.domain.UpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateManager: UpdateManager
) : ViewModel() {

    var updateInfo: UpdateInfo? = null

    fun check() {
        viewModelScope.launch {
            updateInfo = updateManager.checkForUpdate()
        }
    }

    fun update() {
        updateInfo?.let {
            updateManager.downloadAndInstall(it.apkUrl)
        }
    }
}