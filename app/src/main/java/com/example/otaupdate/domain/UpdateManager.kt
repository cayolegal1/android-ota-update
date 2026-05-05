package com.example.otaupdate.domain

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.otaupdate.data.UpdateApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    private val api: UpdateApi,
    @ApplicationContext private val context: Context
) {

    suspend fun checkForUpdate(): UpdateInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getLatestVersion()

                val packageInfo = context.packageManager
                    .getPackageInfo(context.packageName, 0)

                val currentVersion = if (
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P
                ) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode
                }

                if (response.versionCode > currentVersion) {
                    UpdateInfo(
                        versionCode = response.versionCode,
                        apkUrl = response.apkUrl,
                        forceUpdate = response.forceUpdate
                    )
                } else null

            } catch (e: Exception) {
                null
            }
        }
    }

    fun downloadAndInstall(apkUrl: String) {
        val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            setTitle("Actualizando app")
            setDescription("Descargando actualización...")
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "update.apk"
            )
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }

    fun installApk() {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "update.apk"
        )

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(intent)
    }
}

data class UpdateInfo(
    val versionCode: Int,
    val apkUrl: String,
    val forceUpdate: Boolean
)