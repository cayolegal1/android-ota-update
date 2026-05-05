package com.example.otaupdate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.otaupdate.domain.UpdateManager
import com.example.otaupdate.domain.UpdateInfo
import com.example.otaupdate.ui.theme.OTAUpdateTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var updateManager: UpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OTAUpdateTheme {
                UpdateScreen(updateManager)
            }
        }
    }
}

@Composable
fun UpdateScreen(updateManager: UpdateManager) {

    val scope = rememberCoroutineScope()

    val progress by updateManager.downloadProgress.collectAsState()

    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Idle") }

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("OTA UPDATE TEST", style = MaterialTheme.typography.headlineMedium)

            Text("Status: $status")

            if (loading) {
                CircularProgressIndicator()
            }

            if (progress > 0 && progress < 100) {
                LinearProgressIndicator(progress = { progress / 100f })
                Text("Descargando: $progress%")
            }

            Button(onClick = {
                scope.launch {
                    loading = true
                    status = "Checking update..."

                    updateInfo = updateManager.checkForUpdate()

                    status = if (updateInfo != null) {
                        "Update available: v${updateInfo!!.versionCode}"
                    } else {
                        "App is up to date"
                    }

                    loading = false
                }
            }) {
                Text("Check for update")
            }

            Button(
                onClick = {
                    val info = updateInfo ?: return@Button

                    scope.launch {
                        loading = true
                        status = "Downloading APK..."

                        updateManager.downloadAndInstall(info.apkUrl)

                        loading = false
                    }
                },
                enabled = updateInfo != null
            ) {
                Text("Download update")
            }

            Button(
                onClick = {
                    updateManager.installApk()
                },
                enabled = updateInfo != null
            ) {
                Text("Install APK")
            }
        }
    }
}