package com.plantalk.chat.dto;

import com.plantalk.chat.model.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

public class MessageDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotNull(message = "식물 ID는 필수 입력값입니다.")
        private Long plantId;
        
        private Long stateId;
        
        @NotBlank(message = "발신자 유형은 필수 입력값입니다.")
        @Pattern(regexp = "user|plant", message = "발신자 유형은 'user' 또는 'plant'여야 합니다.")
        private String senderType;
        
        @NotBlank(message = "메시지 내용은 필수 입력값입니다.")
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long messageId;
        private Long plantId;
        private Long stateId;
        private String plantName;
        private String senderType;
        private String content;
        private LocalDateTime createdAt;

        public static Response fromEntity(Message message) {
            return Response.builder()
                    .messageId(message.getMessageId())
                    .plantId(message.getPlant().getPlantId())
                    .stateId(message.getState() != null ? message.getState().getStateId() : null)
                    .plantName(message.getPlant().getName())
                    .senderType(message.getSenderType())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .build();
        }
    }
}
