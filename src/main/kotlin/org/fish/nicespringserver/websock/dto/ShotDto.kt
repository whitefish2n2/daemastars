package org.fish.nicespringserver.websock.dto

import org.fish.nicespringserver.game.Vector3

data class ShotDto(
    val playerId: String,
    val direction: Vector3
)