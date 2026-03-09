package org.fish.nicespringserver.websock

import org.fish.nicespringserver.game.PlayerManager
import org.fish.nicespringserver.game.Vector3
import org.fish.nicespringserver.websock.dto.ChatDto
import org.fish.nicespringserver.websock.dto.HitDto
import org.fish.nicespringserver.websock.dto.InitDto
import org.fish.nicespringserver.websock.dto.JoinDto
import org.fish.nicespringserver.websock.dto.LeaveDto
import org.fish.nicespringserver.websock.dto.MessageDto
import org.fish.nicespringserver.websock.dto.MoveDto
import org.fish.nicespringserver.websock.dto.ShotDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Component
class WebSocketHandler(
    private val objectMapper: ObjectMapper,
    private val playerManager: PlayerManager
) : TextWebSocketHandler() {
    //region [만지지 마세용]
    private fun MessageDto.toJson(): String {
        return objectMapper.writeValueAsString(this)
    }
    //endregion
    private val logger = LoggerFactory.getLogger(javaClass)
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    // 세션 ID와 발급된 Player ID를 매핑해두는 맵 추가
    private val sessionToPlayerId = ConcurrentHashMap<String, String>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        println("Received message: $payload")
        try {
            val messageDto = objectMapper.readValue(payload, MessageDto::class.java)

            when (messageDto.type) {
                MessageType.JOIN -> {
                    val dto = objectMapper.readValue<JoinDto>(messageDto.dto)
                    val newPlayer = playerManager.addPlayer(dto.playerName)

                    sessionToPlayerId[session.id] = newPlayer.id

                    val spawnPos:Vector3 = Vector3(0f,0f,0f);//!!
                    val initDto: InitDto = InitDto("",spawnPos)//!!
                    val initResponse = MessageDto(MessageType.INIT, objectMapper.writeValueAsString(initDto))
                    newPlayer.position = spawnPos
                    session.sendMessage(TextMessage(initResponse.toJson()))

                    val joinResponse = MessageDto(MessageType.JOIN, objectMapper.writeValueAsString(newPlayer))
                    broadcastWithOutSender(objectMapper.writeValueAsString(joinResponse),session.id)


                    playerManager.getAllPlayers().forEach { existingPlayer ->
                        if (existingPlayer.id != newPlayer.id) {
                            val msg = MessageDto(MessageType.JOIN, objectMapper.writeValueAsString(existingPlayer))
                            session.sendMessage(TextMessage(objectMapper.writeValueAsString(msg)))
                        }
                    }
                }

                MessageType.MOVE -> {
                    val dto = objectMapper.readValue<MoveDto>(messageDto.dto)

                    val targetPlayer = playerManager.getPlayer(dto.playerId)

                    if (targetPlayer != null) {

                        //대상 플레이어의 위치를 업데이트해요.(playerManager.updatePlayerTransform 함수 활용)


                        val moveDto = MoveDto(dto.playerId, dto.pos, dto.rot)
                        val moveResponse = MessageDto(MessageType.MOVE, objectMapper.writeValueAsString(moveDto))
                        broadcastWithOutSender(objectMapper.writeValueAsString(moveResponse),session.id)
                    }
                }

                MessageType.PING ->{
                    //보낸 플레이어에게 PONG 을 반환해요
                }
                MessageType.CHAT -> {
                    // 클라이언트가 친 명령어를 불러와요
                    val chatDto = objectMapper.readValue<ChatDto>(messageDto.dto)

                    // 누가 채팅을 쳤는지 콘솔에 출력해요
                    val player = playerManager.getPlayer(chatDto.playerId)
                    println(player?.let { it.name + " 가 채팅: " + chatDto.message })
                    /*
                    if(player != null){
                        println(player.name + " 가 채팅: " + chatDto.message)
                    }
                    else{
                        println()
                     */

                    // 클라이언트의 채팅을 다시 다른 플레이어들이 볼 수 있게 포장해요
                    val chatResponse = MessageDto(MessageType.CHAT, messageDto.dto)

                    // 다른 클라이언트들에게 메시지를 전달해요
                    broadcast(objectMapper.writeValueAsString(chatResponse))
                }
                MessageType.SHOT -> {
                    val dto: ShotDto = objectMapper.readValue<ShotDto>(messageDto.dto)
                    /*
                    data class ShotDto(
                        val playerId: String,
                        val direction: Vector3
                    )
                    */
                    ///ShotDto를 다른 유저들에게 전달해요
                }

                MessageType.HIT -> {
                    val dto = objectMapper.readValue<HitDto>(messageDto.dto)
                    val shooter = playerManager.getPlayer(dto.shooterId)
                    val victim = playerManager.getPlayer(dto.victimId)

                    if (shooter != null && victim != null) {

                    }
                }
                else -> {}
            }

        } catch (e: Exception) {
            logger.error(e.message)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session.id)

        val playerId = sessionToPlayerId.remove(session.id)

        if (playerId != null) {
            val targetPlayer = playerManager.getPlayer(playerId)

            if (targetPlayer != null) {
                ///플레이어가 나간 사실을 알려요(playerManager.removePlayer 사용, LeaveDto를 broadcast)
            }
        }
    }

    ///현재 연결되어있는 모든 세션에 payload를 전송
    private fun broadcast(payload: String) {
        val textMessage = TextMessage(payload)
        sessions.values.forEach { targetSession ->
            if (targetSession.isOpen) {
                synchronized(targetSession) {
                    targetSession.sendMessage(textMessage)
                }
            }
        }
    }
    ///현재 연결되어있는 세션 아이디가 senderId인 사람을 제외한 모든 세션에 payload를 전송
    private fun broadcastWithOutSender(payload: String, senderId: String) {
        val textMessage = TextMessage(payload)
        sessions.values.forEach { targetSession ->
            if (targetSession.isOpen && targetSession.id != senderId) {
                synchronized(targetSession) {
                    targetSession.sendMessage(textMessage)
                }
            }
        }
    }
}