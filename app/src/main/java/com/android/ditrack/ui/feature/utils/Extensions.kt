package com.android.ditrack.ui.feature.utils

import android.content.Context
import android.widget.Toast

fun String.showMessageWithToast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}