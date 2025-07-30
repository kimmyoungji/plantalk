package com.plantalk.chat.controller.api;

import com.plantalk.chat.dto.PlantDTO;
import com.plantalk.chat.dto.ResponseDTO;
import com.plantalk.chat.model.entity.Plant;
import com.plantalk.chat.service.PlantService;
import com.plantalk.chat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plant")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;
    private final UserService userService;

    /**
     * 식물 생성
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<PlantDTO.Response>> createPlant(@Valid @RequestBody PlantDTO.Request request) {
        try {
            // 사용자 존재 여부 확인
            if (!userService.findUserById(request.getUserId()).isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseDTO.fail("사용자를 찾을 수 없습니다: " + request.getUserId()));
            }
            
            // 같은 사용자의 같은 이름의 식물이 이미 존재하는지 확인
            if (plantService.findPlantByUserIdAndName(request.getUserId(), request.getName()).isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ResponseDTO.fail("이미 같은 이름의 식물이 존재합니다: " + request.getName()));
            }
            
            Plant plant = new Plant();
            plant.setName(request.getName());
            plant.setSpecies(request.getSpecies());
            
            Plant savedPlant = plantService.createPlant(plant, request.getUserId());
            PlantDTO.Response response = PlantDTO.Response.fromEntity(savedPlant);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ResponseDTO.success("식물 생성 성공", response));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDTO.fail("식물 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 식물 조회
     */
    @GetMapping("/{plantId}")
    public ResponseEntity<ResponseDTO<PlantDTO.Response>> getPlant(@PathVariable Long plantId) {
        Optional<Plant> plantOpt = plantService.findPlantById(plantId);
        
        if (plantOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.fail("식물을 찾을 수 없습니다: " + plantId));
        }
        
        Plant plant = plantOpt.get();
        PlantDTO.Response response = PlantDTO.Response.fromEntity(plant);
        
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    /**
     * 사용자의 모든 식물 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseDTO<List<PlantDTO.Response>>> getPlantsByUser(@PathVariable Long userId) {
        // 사용자 존재 여부 확인
        if (!userService.findUserById(userId).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.fail("사용자를 찾을 수 없습니다: " + userId));
        }
        
        List<Plant> plants = plantService.findPlantsByUserId(userId);
        List<PlantDTO.Response> responses = plants.stream()
                .map(PlantDTO.Response::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseDTO.success(responses));
    }

    /**
     * 식물 정보 업데이트
     */
    @PutMapping("/{plantId}")
    public ResponseEntity<ResponseDTO<PlantDTO.Response>> updatePlant(
            @PathVariable Long plantId,
            @Valid @RequestBody PlantDTO.Request request) {
        try {
            // 식물 존재 여부 확인
            Optional<Plant> plantOpt = plantService.findPlantById(plantId);
            if (plantOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseDTO.fail("식물을 찾을 수 없습니다: " + plantId));
            }
            
            Plant existingPlant = plantOpt.get();
            
            // 요청한 사용자와 식물의 소유자가 일치하는지 확인
            if (!existingPlant.getUser().getUserId().equals(request.getUserId())) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ResponseDTO.fail("해당 식물을 수정할 권한이 없습니다."));
            }
            
            // 이름이 변경되었고, 같은 사용자의 같은 이름의 식물이 이미 존재하는지 확인
            if (!existingPlant.getName().equals(request.getName()) && 
                    plantService.findPlantByUserIdAndName(request.getUserId(), request.getName()).isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ResponseDTO.fail("이미 같은 이름의 식물이 존재합니다: " + request.getName()));
            }
            
            Plant plant = new Plant();
            plant.setName(request.getName());
            plant.setSpecies(request.getSpecies());
            
            Plant updatedPlant = plantService.updatePlant(plantId, plant);
            PlantDTO.Response response = PlantDTO.Response.fromEntity(updatedPlant);
            
            return ResponseEntity.ok(ResponseDTO.success("식물 정보 업데이트 성공", response));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDTO.fail("식물 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 식물 삭제
     */
    @DeleteMapping("/{plantId}")
    public ResponseEntity<ResponseDTO<Void>> deletePlant(@PathVariable Long plantId, @RequestParam Long userId) {
        try {
            // 식물 존재 여부 확인
            Optional<Plant> plantOpt = plantService.findPlantById(plantId);
            if (plantOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseDTO.fail("식물을 찾을 수 없습니다: " + plantId));
            }
            
            Plant plant = plantOpt.get();
            
            // 요청한 사용자와 식물의 소유자가 일치하는지 확인
            if (!plant.getUser().getUserId().equals(userId)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ResponseDTO.fail("해당 식물을 삭제할 권한이 없습니다."));
            }
            
            plantService.deletePlant(plantId);
            
            return ResponseEntity.ok(ResponseDTO.success("식물 삭제 성공", null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDTO.fail("식물 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
