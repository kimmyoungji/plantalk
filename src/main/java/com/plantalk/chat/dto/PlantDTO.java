package com.plantalk.chat.dto;

import com.plantalk.chat.model.entity.Plant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class PlantDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "식물 이름은 필수 입력값입니다.")
        @Size(min = 1, max = 50, message = "식물 이름은 1자 이상 50자 이하여야 합니다.")
        private String name;
        
        private String species;
        
        @NotNull(message = "사용자 ID는 필수 입력값입니다.")
        private Long userId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long plantId;
        private Long userId;
        private String name;
        private String species;
        private LocalDateTime createdAt;

        public static Response fromEntity(Plant plant) {
            return Response.builder()
                    .plantId(plant.getPlantId())
                    .userId(plant.getUser().getUserId())
                    .name(plant.getName())
                    .species(plant.getSpecies())
                    .createdAt(plant.getCreatedAt())
                    .build();
        }
    }
}
