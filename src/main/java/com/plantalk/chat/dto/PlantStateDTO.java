package com.plantalk.chat.dto;

import com.plantalk.chat.model.entity.PlantState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class PlantStateDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotNull(message = "식물 ID는 필수 입력값입니다.")
        private Long plantId;
        
        private Integer lightLevel;
        private Float temperature;
        private Integer moisture;
        private Boolean touched;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long stateId;
        private Long plantId;
        private Integer lightLevel;
        private Float temperature;
        private Integer moisture;
        private Boolean touched;
        private LocalDateTime measuredAt;

        public static Response fromEntity(PlantState plantState) {
            return Response.builder()
                    .stateId(plantState.getStateId())
                    .plantId(plantState.getPlant().getPlantId())
                    .lightLevel(plantState.getLightLevel())
                    .temperature(plantState.getTemperature())
                    .moisture(plantState.getMoisture())
                    .touched(plantState.getTouched())
                    .measuredAt(plantState.getMeasuredAt())
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluationResponse {
        private Long plantId;
        private String plantName;
        private String evaluation;
        private PlantStateDTO.Response latestState;
    }
}
