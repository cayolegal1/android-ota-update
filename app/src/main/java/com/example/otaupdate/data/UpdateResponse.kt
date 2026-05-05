package com.example.otaupdate.data

import kotlinx.serialization.Serializable

@Serializable
data class UpdateResponse(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val forceUpdate: Boolean,
    val checksum: String? = null
)