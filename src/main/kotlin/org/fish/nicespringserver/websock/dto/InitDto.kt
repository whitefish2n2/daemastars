package org.fish.nicespringserver.websock.dto

import org.fish.nicespringserver.game.Vector3

data class InitDto(val userId: String, val pos: Vector3)