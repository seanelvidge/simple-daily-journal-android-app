package com.example.journalapp

import android.text.method.LinkMovementMethod
import android.widget.ScrollView
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.image.coil.CoilImagesPlugin

@Composable
fun MarkdownView(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(CoilImagesPlugin.create(context))
            .build()
    }
    val textColor = MaterialTheme.colorScheme.onSurface

    AndroidView(
        modifier = modifier,
        factory = {
            val textView = TextView(it)
            textView.isClickable = true
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.textSize = 16f
            val scroll = ScrollView(it)
            scroll.addView(textView)
            scroll
        },
        update = { view ->
            val textView = view.getChildAt(0) as TextView
            textView.setTextColor(textColor.toArgb())
            markwon.setMarkdown(textView, markdown)
        }
    )
}
