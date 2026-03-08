package org.fish.nicespringserver.game

import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class PlayerManager {
    private val players = ConcurrentHashMap<String, Player>()

    fun addPlayer(name: String): Player {
        val newId = UUID.randomUUID().toString()
        val newPlayer = Player(name, newId)
        players[newId] = newPlayer
        return newPlayer
    }

    fun removePlayer(playerId: String) {
        players.remove(playerId)
    }

    fun getPlayer(playerId: String): Player? {
        return players[playerId]
    }

    fun getAllPlayers(): List<Player> {
        return players.values.toList()
    }

    fun updatePlayerTransform(playerId: String, position: Vector3, rotation: Vector3): Player? {
        val player = players[playerId] ?: return null

        synchronized(player) {
            player.position.apply { x = position.x; y = position.y; z = position.z }
            player.rotation.apply { x = rotation.x; y = rotation.y; z = rotation.z }
        }


        return player
    }
}