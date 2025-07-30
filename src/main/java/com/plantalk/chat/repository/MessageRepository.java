package com.plantalk.chat.repository;

import com.plantalk.chat.model.entity.Message;
import com.plantalk.chat.model.entity.Plant;
import com.plantalk.chat.model.entity.PlantState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // 특정 식물의 모든 메시지 조회
    List<Message> findByPlant(Plant plant);
    
    // 특정 식물의 모든 메시지 페이징 조회
    Page<Message> findByPlant(Plant plant, Pageable pageable);
    
    // 특정 식물 ID의 모든 메시지 조회
    List<Message> findByPlantPlantId(Long plantId);
    
    // 특정 식물 ID의 모든 메시지 페이징 조회
    Page<Message> findByPlantPlantId(Long plantId, Pageable pageable);
    
    // 특정 상태에 대한 메시지 조회
    List<Message> findByState(PlantState state);
    
    // 특정 상태 ID에 대한 메시지 조회
    List<Message> findByStateStateId(Long stateId);
    
    // 특정 발신자 유형의 메시지 조회
    List<Message> findByPlantAndSenderType(Plant plant, String senderType);
    
    // 특정 식물 ID와 발신자 유형의 메시지 조회
    List<Message> findByPlantPlantIdAndSenderType(Long plantId, String senderType);
    
    // 특정 기간 내의 메시지 조회
    List<Message> findByPlantAndCreatedAtBetween(Plant plant, LocalDateTime start, LocalDateTime end);
    
    // 특정 식물 ID와 기간 내의 메시지 조회
    List<Message> findByPlantPlantIdAndCreatedAtBetween(Long plantId, LocalDateTime start, LocalDateTime end);
    
    // 특정 식물의 최근 메시지 조회
    List<Message> findTop10ByPlantOrderByCreatedAtDesc(Plant plant);
    
    // 특정 식물 ID의 최근 메시지 조회
    List<Message> findTop10ByPlantPlantIdOrderByCreatedAtDesc(Long plantId);
    
    // 특정 식물의 메시지 수 카운트
    long countByPlantPlantId(Long plantId);
    
    // 특정 발신자 유형의 메시지 수 카운트
    long countByPlantPlantIdAndSenderType(Long plantId, String senderType);
}
