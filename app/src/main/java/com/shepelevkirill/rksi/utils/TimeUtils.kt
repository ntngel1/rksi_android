package com.shepelevkirill.rksi.utils

import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

fun LocalTime.getString(): String {
    val formatter = DateTimeFormatter.ofPattern("H:mm")
    return this.format(formatter)
}