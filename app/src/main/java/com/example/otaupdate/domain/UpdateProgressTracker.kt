package com.example.otaupdate.domain

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadProgressTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private val _progress = MutableStateFlow(0)
    val progress = _progress.asStateFlow()

    fun track(downloadId: Long, onDownloadEnd: () -> Unit) {

        Thread {
            var isDownloading = true

            while (isDownloading) {

                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor: Cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {

                    val bytesDownloaded =
                        cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
                            )
                        )

                    val bytesTotal =
                        cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                DownloadManager.COLUMN_TOTAL_SIZE_BYTES
                            )
                        )

                    if (bytesTotal > 0) {
                        _progress.value = (bytesDownloaded * 100L / bytesTotal).toInt()
                    }

                    val status =
                        cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                DownloadManager.COLUMN_STATUS
                            )
                        )

                    if (
                        status == DownloadManager.STATUS_SUCCESSFUL ||
                        status == DownloadManager.STATUS_FAILED
                    ) {
                        isDownloading = false
                        onDownloadEnd()
                    }
                }

                cursor.close()
                Thread.sleep(300)
            }
        }.start()
    }
}