package com.bouyahya.plugins

import com.bouyahya.models.Session
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import java.util.*
import kotlin.collections.LinkedHashSet

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(1000)
        timeout = Duration.ofSeconds(1000)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        val sessions = Collections.synchronizedSet<Session?>(LinkedHashSet())
        webSocket("/chat") {
            println("Connected!")
            val currentSession = Session(this)
            sessions += currentSession
            try {
                send("You are connected!")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    println(receivedText)
                    sessions.forEach {
                        it.current.send(receivedText)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println(e.localizedMessage)
            } finally {
                println("Disconnected!")
            }
        }
    }
}