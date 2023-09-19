package com.bouyahya.models

import io.ktor.websocket.*

data class Session(val current: DefaultWebSocketSession, val userId: Long)