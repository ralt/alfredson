package io.ralt.alfredson.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.ralt.alfredson.AlfredsonApp

@Composable
fun rememberApp(): AlfredsonApp {
    val ctx = LocalContext.current
    return ctx.applicationContext as AlfredsonApp
}

fun Context.alfredsonApp(): AlfredsonApp = applicationContext as AlfredsonApp
