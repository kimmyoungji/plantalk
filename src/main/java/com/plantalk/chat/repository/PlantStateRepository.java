package com.plantalk.chat.repository;

import com.plantalk.chat.model.entity.Plant;
import com.plantalk.chat.model.entity.PlantState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlantStateRepository extends JpaRepository<PlantState, Long> {
    
    // 특정 식물의 모든 상태 기록 조회
    List<PlantState> findByPlant(Plant plant);
    
    // 특정 식물의 모든 상태 기록 페이징 조회
    Page<PlantState> findByPlant(Plant plant, Pageable pageable);
    
    // 식물 ID로 모든 상태 기록 조회
    List<PlantState> findByPlantPlantId(Long plantId);
    
    // 특정 식물의 가장 최근 상태 조회
    Optional<PlantState> findTopByPlantOrderByMeasuredAtDesc(Plant plant);
    
    // 특정 식물 ID의 가장 최근 상태 조회
    Optional<PlantState> findTopByPlantPlantIdOrderByMeasuredAtDesc(Long plantId);
    
    // 특정 기간 내의 상태 기록 조회
    List<PlantState> findByPlantAndMeasuredAtBetween(Plant plant, LocalDateTime start, LocalDateTime end);
    
    // 특정 조건에 맞는 상태 기록 조회 (예: 온도가 특정 값 이상)
    List<PlantState> findByPlantAndTemperatureGreaterThan(Plant plant, Float temperature);
    
    // 특정 조건에 맞는 상태 기록 조회 (예: 습도가 특정 값 이하)
    List<PlantState> findByPlantAndMoistureLessThan(Plant plant, Integer moisture);
    
    // 특정 조건에 맞는 상태 기록 조회 (예: 터치 감지됨)
    List<PlantState> findByPlantAndTouchedTrue(Plant plant);
    
    // 특정 식물의 평균 온도 계산 (JPQL 사용)
    @Query("SELECT AVG(ps.temperature) FROM PlantState ps WHERE ps.plant.plantId = :plantId")
    Float calculateAverageTemperature(@Param("plantId") Long plantId);
    
    // 특정 식물의 평균 습도 계산 (JPQL 사용)
    @Query("SELECT AVG(ps.moisture) FROM PlantState ps WHERE ps.plant.plantId = :plantId")
    Float calculateAverageMoisture(@Param("plantId") Long plantId);
}
