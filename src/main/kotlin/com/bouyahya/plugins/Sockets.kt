package com.bouyahya.plugins

import com.bouyahya.models.Session
import com.bouyahya.models.User
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        val users = Collections.synchronizedSet<User?>(LinkedHashSet())
        webSocket("/chat/{userId}/{username}") {
            println("Connected!")
            val currentSession = Session(current = this, userId = call.parameters["userId"]!!.toLong())
            sessions.add(currentSession)
            val currentUser = User(id = call.parameters["userId"]!!.toLong(), username = call.parameters["username"]!!)
            users.add(currentUser)
            try {
                send("You are connected!")
                sessions.forEach {
                    it.current.send(Json.encodeToString(users.map { user -> user!! }))
                }
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
                val disconnectedUser = users.first { it.id == call.parameters["userId"]!!.toLong() }
                val disconnectedSession = sessions.first { it.userId == call.parameters["userId"]!!.toLong() }
                sessions.remove(disconnectedSession)
                users.remove(disconnectedUser)
                sessions.forEach {
                    it.current.send(Json.encodeToString(users.map { user -> user!! }))
                }
            }
        }
    }
}