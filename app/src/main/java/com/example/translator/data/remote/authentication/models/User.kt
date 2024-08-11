package com.example.translator.data.remote.authentication.models

import android.net.Uri

data class User(
    val id: String,
    var name: String?,
    val photoUrl: Uri?
)
