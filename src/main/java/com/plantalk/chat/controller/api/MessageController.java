package com.plantalk.chat.controller.api;

import com.plantalk.chat.dto.MessageDTO;
import com.plantalk.chat.dto.ResponseDTO;
import com.plantalk.chat.model.entity.Message;
import com.plantalk.chat.model.entity.Plant;
import com.plantalk.chat.model.entity.PlantState;
import com.plantalk.chat.service.MessageService;
import com.plantalk.chat.service.PlantService;
import com.plantalk.chat.service.PlantStateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final PlantService plantService;
    private final PlantStateService plantStateService;

    /**
     * 메시지 생성
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<MessageDTO.Response>> createMessage(@Valid @RequestBody MessageDTO.Request request) {
        try {
            // 식물 존재 여부 확인
            Optional<Plant> plantOpt = plantService.findPlantById(request.getPlantId());
            if (plantOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseDTO.fail("식물을 찾을 수 없습니다: " + request.getPlantId()));
            }
            
            Message message = new Message();
            message.setSenderType(request.getSenderType());
            message.setContent(request.getContent());
            
            Message savedMessage;
            if (request.getStateId() != null) {
                // 식물 상태 존재 여부 확인
                Optional<PlantState> stateOpt = plantStateService.findPlantStateById(request.getStateId());
                if (stateOpt.isEmpty()) {
                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(ResponseDTO.fail("식물 상태를 찾을 수 없습니다: " + request.getStateId()));
                }
                
                savedMessage = messageService.createMessage(message, request.getPlantId(), request.getStateId());
            } else {
                savedMessage = messageService.createMessage(message, request.getPlantId());
            }
            
            MessageDTO.Response response = MessageDTO.Response.fromEntity(savedMessage);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ResponseDTO.success("메시지 생성 성공", response));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDTO.fail("메시지 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 메시지 조회
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<ResponseDTO<MessageDTO.Response>> getMessage(@PathVariable Long messageId) {
        Optional<Message> messageOpt = messageService.findMessageById(messageId);
        
        if (messageOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.fail("메시지를 찾을 수 없습니다: " + messageId));
        }
        
        Message message = messageOpt.get();
        MessageDTO.Response response = MessageDTO.Response.fromEntity(message);
        
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    /**
     * 특정 식물의 모든 메시지 조회
     */
    @GetMapping("/plant/{plantId}")
    public ResponseEntity<ResponseDTO<List<MessageDTO.Response>>> getMessagesByPlant(
            @PathVariable Long plantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 식물 존재 여부 확인
        Optional<Plant> plantOpt = plantService.findPlantById(plantId);
        if (plantOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.fail("식물을 찾을 수 없습니다: " + plantId));
        }
        
        Page<Message> messagesPage = messageService.findMessagesByPlantId(
                plantId, 
                PageRequest.of(page, size, Sort.by("createdAt").ascending())
        );
        
        List<MessageDTO.Response> responses = messagesPage.getContent().stream()
                .map(MessageDTO.Response::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseDTO.success(responses));
    }

    /**
     * 특정 식물의 최근 메시지 조회
     */
    @GetMapping("/plant/{plantId}/recent")
    public ResponseEntity<ResponseDTO<List<MessageDTO.Response>>> getRecentMessagesByPlant(@PathVariable Long plantId) {
        // 식물 존재 여부 확인
        Optional<Plant> plantOpt = plantService.findPlantById(plantId);
        if (plantOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.fail("식물을 찾을 수 없습니다: " + plantId));
        }
        
        List<Message> recentMessages = messageService.findRecentMessagesByPlantId(plantId);
        
        List<MessageDTO.Response> responses = recentMessages.stream()
                .map(MessageDTO.Response::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseDTO.success(responses));
    }

    /**
     * 특정 발신자 유형의 메시지 조회
     */
    @GetMapping("/plant/{plantId}/sender/{senderType}")
    public ResponseEntity<ResponseDTO<List<MessageDTO.Response>>> getMessagesByPlantAndSenderType(
            @PathVariable Long plantId,
            @PathVariable String senderType) {
        
        // 식물 존재 여부 확인
        Optional<Plant> plantOpt = plantService.findPlantById(plantId);
        if (plantOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.fail("식물을 찾을 수 없습니다: " + plantId));
        }
        
        // 발신자 유형 검증
        if (!senderType.equals("user") && !senderType.equals("plant")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseDTO.fail("발신자 유형은 'user' 또는 'plant'여야 합니다."));
        }
        
        List<Message> messages = messageService.findMessagesByPlantIdAndSenderType(plantId, senderType);
        
        List<MessageDTO.Response> responses = messages.stream()
                .map(MessageDTO.Response::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseDTO.success(responses));
    }

    /**
     * 특정 기간 내의 메시지 조회
     */
    @GetMapping("/plant/{plantId}/period")
    public ResponseEntity<ResponseDTO<List<MessageDTO.Response>>> getMessagesByPeriod(
            @PathVariable Long plantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        // 식물 존재 여부 확인
        Optional<Plant> plantOpt = plantService.findPlantById(plantId);
        if (plantOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.fail("식물을 찾을 수 없습니다: " + plantId));
        }
        
        Plant plant = plantOpt.get();
        List<Message> messages = messageService.findMessagesByPlantAndDateRange(plant, start, end);
        
        List<MessageDTO.Response> responses = messages.stream()
                .map(MessageDTO.Response::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseDTO.success(responses));
    }

    /**
     * 식물 상태 기반 자동 메시지 생성
     */
    @PostMapping("/plant/{plantId}/state/{stateId}/generate")
    public ResponseEntity<ResponseDTO<MessageDTO.Response>> generatePlantMessage(
            @PathVariable Long plantId,
            @PathVariable Long stateId) {
        try {
            // 식물 존재 여부 확인
            Optional<Plant> plantOpt = plantService.findPlantById(plantId);
            if (plantOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseDTO.fail("식물을 찾을 수 없습니다: " + plantId));
            }
            
            // 식물 상태 존재 여부 확인
            Optional<PlantState> stateOpt = plantStateService.findPlantStateById(stateId);
            if (stateOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseDTO.fail("식물 상태를 찾을 수 없습니다: " + stateId));
            }
            
            Message generatedMessage = messageService.generatePlantMessage(plantId, stateId);
            MessageDTO.Response response = MessageDTO.Response.fromEntity(generatedMessage);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ResponseDTO.success("식물 메시지 생성 성공", response));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDTO.fail("식물 메시지 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 메시지 업데이트
     */
    @PutMapping("/{messageId}")
    public ResponseEntity<ResponseDTO<MessageDTO.Response>> updateMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody MessageDTO.Request request) {
        try {
            // 메시지 존재 여부 확인
            Optional<Message> messageOpt = messageService.findMessageById(messageId);
            if (messageOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseDTO.fail("메시지를 찾을 수 없습니다: " + messageId));
            }
            
            Message existingMessage = messageOpt.get();
            
            // 요청한 식물 ID와 메시지의 식물 ID가 일치하는지 확인
            if (!existingMessage.getPlant().getPlantId().equals(request.getPlantId())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ResponseDTO.fail("식물 ID가 일치하지 않습니다."));
            }
            
            Message message = new Message();
            message.setContent(request.getContent());
            
            Message updatedMessage = messageService.updateMessage(messageId, message);
            MessageDTO.Response response = MessageDTO.Response.fromEntity(updatedMessage);
            
            return ResponseEntity.ok(ResponseDTO.success("메시지 업데이트 성공", response));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDTO.fail("메시지 업데이트 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 메시지 삭제
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ResponseDTO<Void>> deleteMessage(@PathVariable Long messageId) {
        try {
            // 메시지 존재 여부 확인
            Optional<Message> messageOpt = messageService.findMessageById(messageId);
            if (messageOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseDTO.fail("메시지를 찾을 수 없습니다: " + messageId));
            }
            
            messageService.deleteMessage(messageId);
            
            return ResponseEntity.ok(ResponseDTO.success("메시지 삭제 성공", null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDTO.fail("메시지 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
