package com.plantalk.chat.service;

import com.plantalk.chat.model.entity.Plant;
import com.plantalk.chat.model.entity.PlantState;
import com.plantalk.chat.repository.PlantRepository;
import com.plantalk.chat.repository.PlantStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantStateService {

    private final PlantStateRepository plantStateRepository;
    private final PlantRepository plantRepository;

    /**
     * 모든 식물 상태 조회
     */
    public List<PlantState> findAllPlantStates() {
        return plantStateRepository.findAll();
    }

    /**
     * ID로 식물 상태 조회
     */
    public Optional<PlantState> findPlantStateById(Long stateId) {
        return plantStateRepository.findById(stateId);
    }

    /**
     * 특정 식물의 모든 상태 기록 조회
     */
    public List<PlantState> findPlantStatesByPlant(Plant plant) {
        return plantStateRepository.findByPlant(plant);
    }

    /**
     * 특정 식물의 모든 상태 기록 페이징 조회
     */
    public Page<PlantState> findPlantStatesByPlant(Plant plant, Pageable pageable) {
        return plantStateRepository.findByPlant(plant, pageable);
    }

    /**
     * 식물 ID로 모든 상태 기록 조회
     */
    public List<PlantState> findPlantStatesByPlantId(Long plantId) {
        return plantStateRepository.findByPlantPlantId(plantId);
    }

    /**
     * 특정 식물의 가장 최근 상태 조회
     */
    public Optional<PlantState> findLatestPlantState(Plant plant) {
        return plantStateRepository.findTopByPlantOrderByMeasuredAtDesc(plant);
    }

    /**
     * 특정 식물 ID의 가장 최근 상태 조회
     */
    public Optional<PlantState> findLatestPlantStateByPlantId(Long plantId) {
        return plantStateRepository.findTopByPlantPlantIdOrderByMeasuredAtDesc(plantId);
    }

    /**
     * 특정 기간 내의 상태 기록 조회
     */
    public List<PlantState> findPlantStatesByPlantAndDateRange(Plant plant, LocalDateTime start, LocalDateTime end) {
        return plantStateRepository.findByPlantAndMeasuredAtBetween(plant, start, end);
    }

    /**
     * 식물 상태 생성
     */
    @Transactional
    public PlantState createPlantState(PlantState plantState, Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new IllegalArgumentException("식물을 찾을 수 없습니다: " + plantId));
        
        plantState.setPlant(plant);
        return plantStateRepository.save(plantState);
    }

    /**
     * 식물 상태 업데이트
     */
    @Transactional
    public PlantState updatePlantState(Long stateId, PlantState plantStateDetails) {
        PlantState plantState = plantStateRepository.findById(stateId)
                .orElseThrow(() -> new IllegalArgumentException("식물 상태를 찾을 수 없습니다: " + stateId));
        
        plantState.setLightLevel(plantStateDetails.getLightLevel());
        plantState.setTemperature(plantStateDetails.getTemperature());
        plantState.setMoisture(plantStateDetails.getMoisture());
        plantState.setTouched(plantStateDetails.getTouched());
        
        return plantStateRepository.save(plantState);
    }

    /**
     * 식물 상태 삭제
     */
    @Transactional
    public void deletePlantState(Long stateId) {
        PlantState plantState = plantStateRepository.findById(stateId)
                .orElseThrow(() -> new IllegalArgumentException("식물 상태를 찾을 수 없습니다: " + stateId));
        
        plantStateRepository.delete(plantState);
    }

    /**
     * 특정 식물의 평균 온도 계산
     */
    public Float calculateAverageTemperature(Long plantId) {
        return plantStateRepository.calculateAverageTemperature(plantId);
    }

    /**
     * 특정 식물의 평균 습도 계산
     */
    public Float calculateAverageMoisture(Long plantId) {
        return plantStateRepository.calculateAverageMoisture(plantId);
    }

    /**
     * 센서 데이터 분석 및 상태 평가
     * 식물의 상태를 분석하여 필요한 조치를 결정하는 로직
     */
    public String evaluatePlantCondition(Long plantId) {
        Optional<PlantState> latestStateOpt = findLatestPlantStateByPlantId(plantId);
        
        if (latestStateOpt.isEmpty()) {
            return "식물 상태 데이터가 없습니다.";
        }
        
        PlantState state = latestStateOpt.get();
        StringBuilder evaluation = new StringBuilder();
        
        // 빛 수준 평가
        if (state.getLightLevel() != null) {
            if (state.getLightLevel() < 300) {
                evaluation.append("빛이 부족합니다. 더 밝은 곳으로 옮겨주세요. ");
            } else if (state.getLightLevel() > 800) {
                evaluation.append("빛이 너무 강합니다. 직사광선을 피해주세요. ");
            } else {
                evaluation.append("빛 수준이 적절합니다. ");
            }
        }
        
        // 온도 평가
        if (state.getTemperature() != null) {
            if (state.getTemperature() < 15) {
                evaluation.append("온도가 너무 낮습니다. 따뜻한 곳으로 옮겨주세요. ");
            } else if (state.getTemperature() > 30) {
                evaluation.append("온도가 너무 높습니다. 시원한 곳으로 옮겨주세요. ");
            } else {
                evaluation.append("온도가 적절합니다. ");
            }
        }
        
        // 습도 평가
        if (state.getMoisture() != null) {
            if (state.getMoisture() < 30) {
                evaluation.append("물이 부족합니다. 물을 주세요. ");
            } else if (state.getMoisture() > 80) {
                evaluation.append("물이 너무 많습니다. 물 주기를 줄여주세요. ");
            } else {
                evaluation.append("습도가 적절합니다. ");
            }
        }
        
        // 터치 감지 평가
        if (state.getTouched() != null && state.getTouched()) {
            evaluation.append("최근에 접촉이 감지되었습니다. ");
        }
        
        return evaluation.toString();
    }
}
