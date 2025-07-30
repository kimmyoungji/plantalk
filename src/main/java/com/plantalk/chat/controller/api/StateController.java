package com.plantalk.chat.controller.api;

import com.plantalk.chat.dto.PlantStateDTO;
import com.plantalk.chat.dto.ResponseDTO;
import com.plantalk.chat.model.entity.Plant;
import com.plantalk.chat.model.entity.PlantState;
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
@RequestMapping("/api/state")
@RequiredArgsConstructor
public class StateController {

    private final PlantStateService plantStateService;
    private final PlantService plantService;

    /**
     * 식물 상태 생성
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<PlantStateDTO.Response>> createPlantState(@Valid @RequestBody PlantStateDTO.Request request) {
        try {
            // 식물 존재 여부 확인
            Optional<Plant> plantOpt = plantService.findPlantById(request.getPlantId());
            if (plantOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseDTO.fail("식물을 찾을 수 없습니다: " + request.getPlantId()));
            }
            
            PlantState plantState = new PlantState();
            plantState.setLightLevel(request.getLightLevel());
            plantState.setTemperature(request.getTemperature());
            plantState.setMoisture(request.getMoisture());
            plantState.setTouched(request.getTouched());
            
            PlantState savedPlantState = plantStateService.createPlantState(plantState, request.getPlantId());
            PlantStateDTO.Response response = PlantStateDTO.Response.fromEntity(savedPlantState);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ResponseDTO.success("식물 상태 생성 성공", response));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDTO.fail("식물 상태 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 식물 상태 조회
     */
    @GetMapping("/{stateId}")
    public ResponseEntity<ResponseDTO<PlantStateDTO.Response>> getPlantState(@PathVariable Long stateId) {
        Optional<PlantState> plantStateOpt = plantStateService.findPlantStateById(stateId);
        
        if (plantStateOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.fail("식물 상태를 찾을 수 없습니다: " + stateId));
        }
        
        PlantState plantState = plantStateOpt.get();
        PlantStateDTO.Response response = PlantStateDTO.Response.fromEntity(plantState);
        
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    /**
     * 특정 식물의 모든 상태 조회
     */
    @GetMapping("/plant/{plantId}")
    public ResponseEntity<ResponseDTO<List<PlantStateDTO.Response>>> getPlantStatesByPlant(
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
        
        Plant plant = plantOpt.get();
        Page<PlantState> plantStatesPage = plantStateService.findPlantStatesByPlant(
                plant, 
                PageRequest.of(page, size, Sort.by("measuredAt").descending())
        );
        
        List<PlantStateDTO.Response> responses = plantStatesPage.getContent().stream()
                .map(PlantStateDTO.Response::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseDTO.success(responses));
    }

    /**
     * 특정 식물의 가장 최근 상태 조회
     */
    @GetMapping("/plant/{plantId}/latest")
    public ResponseEntity<ResponseDTO<PlantStateDTO.Response>> getLatestPlantState(@PathVariable Long plantId) {
        // 식물 존재 여부 확인
        Optional<Plant> plantOpt = plantService.findPlantById(plantId);
        if (plantOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.fail("식물을 찾을 수 없습니다: " + plantId));
        }
        
        Optional<PlantState> latestStateOpt = plantStateService.findLatestPlantStateByPlantId(plantId);
        
        if (latestStateOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.fail("식물 상태 데이터가 없습니다."));
        }
        
        PlantState latestState = latestStateOpt.get();
        PlantStateDTO.Response response = PlantStateDTO.Response.fromEntity(latestState);
        
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    /**
     * 특정 기간 내의 식물 상태 조회
     */
    @GetMapping("/plant/{plantId}/period")
    public ResponseEntity<ResponseDTO<List<PlantStateDTO.Response>>> getPlantStatesByPeriod(
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
        List<PlantState> plantStates = plantStateService.findPlantStatesByPlantAndDateRange(plant, start, end);
        
        List<PlantStateDTO.Response> responses = plantStates.stream()
                .map(PlantStateDTO.Response::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseDTO.success(responses));
    }

    /**
     * 식물 상태 평가
     */
    @GetMapping("/plant/{plantId}/evaluate")
    public ResponseEntity<ResponseDTO<PlantStateDTO.EvaluationResponse>> evaluatePlantCondition(@PathVariable Long plantId) {
        // 식물 존재 여부 확인
        Optional<Plant> plantOpt = plantService.findPlantById(plantId);
        if (plantOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.fail("식물을 찾을 수 없습니다: " + plantId));
        }
        
        Plant plant = plantOpt.get();
        String evaluation = plantStateService.evaluatePlantCondition(plantId);
        
        Optional<PlantState> latestStateOpt = plantStateService.findLatestPlantStateByPlantId(plantId);
        PlantStateDTO.Response latestStateResponse = latestStateOpt.isPresent() 
                ? PlantStateDTO.Response.fromEntity(latestStateOpt.get()) 
                : null;
        
        PlantStateDTO.EvaluationResponse response = PlantStateDTO.EvaluationResponse.builder()
                .plantId(plantId)
                .plantName(plant.getName())
                .evaluation(evaluation)
                .latestState(latestStateResponse)
                .build();
        
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    /**
     * 식물 상태 업데이트
     */
    @PutMapping("/{stateId}")
    public ResponseEntity<ResponseDTO<PlantStateDTO.Response>> updatePlantState(
            @PathVariable Long stateId,
            @Valid @RequestBody PlantStateDTO.Request request) {
        try {
            // 식물 상태 존재 여부 확인
            Optional<PlantState> plantStateOpt = plantStateService.findPlantStateById(stateId);
            if (plantStateOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseDTO.fail("식물 상태를 찾을 수 없습니다: " + stateId));
            }
            
            PlantState existingPlantState = plantStateOpt.get();
            
            // 요청한 식물 ID와 상태의 식물 ID가 일치하는지 확인
            if (!existingPlantState.getPlant().getPlantId().equals(request.getPlantId())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ResponseDTO.fail("식물 ID가 일치하지 않습니다."));
            }
            
            PlantState plantState = new PlantState();
            plantState.setLightLevel(request.getLightLevel());
            plantState.setTemperature(request.getTemperature());
            plantState.setMoisture(request.getMoisture());
            plantState.setTouched(request.getTouched());
            
            PlantState updatedPlantState = plantStateService.updatePlantState(stateId, plantState);
            PlantStateDTO.Response response = PlantStateDTO.Response.fromEntity(updatedPlantState);
            
            return ResponseEntity.ok(ResponseDTO.success("식물 상태 업데이트 성공", response));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDTO.fail("식물 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 식물 상태 삭제
     */
    @DeleteMapping("/{stateId}")
    public ResponseEntity<ResponseDTO<Void>> deletePlantState(@PathVariable Long stateId) {
        try {
            // 식물 상태 존재 여부 확인
            Optional<PlantState> plantStateOpt = plantStateService.findPlantStateById(stateId);
            if (plantStateOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseDTO.fail("식물 상태를 찾을 수 없습니다: " + stateId));
            }
            
            plantStateService.deletePlantState(stateId);
            
            return ResponseEntity.ok(ResponseDTO.success("식물 상태 삭제 성공", null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDTO.fail("식물 상태 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
