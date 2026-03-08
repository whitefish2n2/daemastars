package org.fish.nicespringserver.websock.dto

import org.fish.nicespringserver.game.Vector3

data class MoveDto (
    val playerId:String,
    val pos : Vector3,
    val rot : Vector3,
)