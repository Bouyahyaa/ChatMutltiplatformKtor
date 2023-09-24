package com.bouyahya

object Constants {
    fun String.toFormattedUrlGet(): String {
        return this.replace(
            "%3A", ":"
        ).replace("%2F", "/")
    }
}