package org.fish.nicespringserver.websock.dto

import org.fish.nicespringserver.websock.MessageType

data class MessageDto(val type: MessageType, val dto: String);