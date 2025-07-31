package com.plantalk.chat.controller;

import com.plantalk.chat.dto.MessageDTO;
import com.plantalk.chat.model.entity.Message;
import com.plantalk.chat.model.entity.Plant;
import com.plantalk.chat.model.entity.PlantState;
import com.plantalk.chat.service.MessageService;
import com.plantalk.chat.service.PlantService;
import com.plantalk.chat.service.PlantStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final MessageService messageService;
    private final PlantService plantService;
    private final PlantStateService plantStateService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 메시지 전송 처리
     * 클라이언트에서 /app/chat.sendMessage/{plantId} 경로로 메시지를 보내면 처리
     */
    @MessageMapping("/chat.sendMessage/{plantId}")
    @SendTo("/topic/public/{plantId}")
    public MessageDTO.Response sendMessage(
            @DestinationVariable Long plantId,
            @Payload MessageDTO.Request messageRequest) {
        
        log.info("WebSocket 메시지 수신: plantId={}, content={}", plantId, messageRequest.getContent());
        
        // 식물 존재 여부 확인
        Optional<Plant> plantOpt = plantService.findPlantById(plantId);
        if (plantOpt.isEmpty()) {
            log.error("식물을 찾을 수 없음: plantId={}", plantId);
            return null;
        }
        
        Message message = new Message();
        message.setSenderType(messageRequest.getSenderType());
        message.setContent(messageRequest.getContent());
        message.setPlant(plantOpt.get());

        // 메시지 저장
        Message savedMessage = messageService.createMessage(
            message,
            messageRequest.getPlantId(),
            messageRequest.getStateId()
        );
        
        // 사용자 메시지인 경우, 비동기로 식물 응답 생성
        if ("user".equals(messageRequest.getSenderType())) {
            // 비동기로 식물 응답 생성 처리
            CompletableFuture.runAsync(() -> {
                generatePlantResponse(plantId);
            });
        }
        
        // 사용자 메시지 즉시 응답
        return MessageDTO.Response.fromEntity(savedMessage);
    }
    
    /**
     * 식물 응답 메시지 자동 생성 및 WebSocket을 통한 전송
     */
    private void generatePlantResponse(Long plantId) {
        try {
            // 최신 상태 정보 조회
            Optional<PlantState> latestStateOpt = plantStateService.findLatestPlantStateByPlantId(plantId);
            if (latestStateOpt.isEmpty()) {
                log.error("식물 상태 정보를 찾을 수 없음: plantId={}", plantId);
                return;
            }
            
            // 식물 정보 조회
            Optional<Plant> plantOpt = plantService.findPlantById(plantId);
            if (plantOpt.isEmpty()) {
                log.error("식물을 찾을 수 없음: plantId={}", plantId);
                return;
            }
            
            // 자동 메시지 생성 - ChatGPT API 호출
            Message plantMessage = messageService.generatePlantMessage(plantOpt.get().getPlantId(), latestStateOpt.get().getStateId());
            
            // 생성된 식물 응답을 DTO로 변환
            MessageDTO.Response response = MessageDTO.Response.fromEntity(plantMessage);
            
            // WebSocket을 통해 클라이언트에게 식물 응답 전송
            log.info("식물 응답 전송: plantId={}, content={}", plantId, plantMessage.getContent());
            messagingTemplate.convertAndSend("/topic/public/" + plantId, response);
            
        } catch (Exception e) {
            log.error("식물 응답 생성 및 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 사용자 입장 처리
     * 클라이언트에서 /app/chat.addUser/{plantId} 경로로 메시지를 보내면 처리
     */
    @MessageMapping("/chat.addUser/{plantId}")
    @SendTo("/topic/public/{plantId}")
    public MessageDTO.Response addUser(
            @DestinationVariable Long plantId,
            @Payload MessageDTO.Request messageRequest,
            SimpMessageHeaderAccessor headerAccessor) {
        
        // WebSocket 세션에 사용자 정보 추가
        headerAccessor.getSessionAttributes().put("plantId", plantId);
        headerAccessor.getSessionAttributes().put("username", messageRequest.getContent());
        
        log.info("사용자 입장: plantId={}, username={}", plantId, messageRequest.getContent());
        
        // 시스템 메시지 생성 (사용자 입장)
        MessageDTO.Response response = new MessageDTO.Response();
        response.setPlantId(plantId);
        response.setSenderType("system");
        response.setContent(messageRequest.getContent() + "님이 입장했습니다.");
        
        return response;
    }
}
