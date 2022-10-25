package com.gonnen.livetime.ui.ext.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.whenStarted

@Composable
fun rememberAnimationTimeMillis(key: Any? = true): State<Long> {
    val currentTimeMillis = remember { mutableStateOf(0L) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(key) {
        val startTimeMillis = withFrameMillis { it }
        lifecycleOwner.whenStarted {

            while (true) {
                withFrameMillis { frameTimeMillis ->
                    currentTimeMillis.value = frameTimeMillis - startTimeMillis
                }
            }
        }
    }

    return currentTimeMillis
}