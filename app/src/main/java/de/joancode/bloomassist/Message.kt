package de.joancode.bloomassist

import android.net.Uri

data class Message(
    val text: String,
    val isUser: Boolean,
    val time: String,
    val imageUri: Uri? = null,
    val plantName: String? = null
)