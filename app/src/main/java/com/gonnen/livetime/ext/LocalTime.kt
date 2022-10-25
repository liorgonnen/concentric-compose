package com.gonnen.livetime.ext

import java.time.LocalTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

val LocalTime.secondsInMillis get() = second.seconds.inWholeMilliseconds

val LocalTime.minutesInMillis get() = minute.minutes.inWholeMilliseconds