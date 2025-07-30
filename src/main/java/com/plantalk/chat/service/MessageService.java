package com.plantalk.chat.service;

import com.plantalk.chat.model.entity.Message;
import com.plantalk.chat.model.entity.Plant;
import com.plantalk.chat.model.entity.PlantState;
import com.plantalk.chat.repository.MessageRepository;
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
public class MessageService {

    private final MessageRepository messageRepository;
    private final PlantRepository plantRepository;
    private final PlantStateRepository plantStateRepository;

    /**
     * 모든 메시지 조회
     */
    public List<Message> findAllMessages() {
        return messageRepository.findAll();
    }

    /**
     * ID로 메시지 조회
     */
    public Optional<Message> findMessageById(Long messageId) {
        return messageRepository.findById(messageId);
    }

    /**
     * 특정 식물의 모든 메시지 조회
     */
    public List<Message> findMessagesByPlant(Plant plant) {
        return messageRepository.findByPlant(plant);
    }

    /**
     * 특정 식물의 모든 메시지 페이징 조회
     */
    public Page<Message> findMessagesByPlant(Plant plant, Pageable pageable) {
        return messageRepository.findByPlant(plant, pageable);
    }

    /**
     * 특정 식물 ID의 모든 메시지 조회
     */
    public List<Message> findMessagesByPlantId(Long plantId) {
        return messageRepository.findByPlantPlantId(plantId);
    }

    /**
     * 특정 식물 ID의 모든 메시지 페이징 조회
     */
    public Page<Message> findMessagesByPlantId(Long plantId, Pageable pageable) {
        return messageRepository.findByPlantPlantId(plantId, pageable);
    }

    /**
     * 특정 상태에 대한 메시지 조회
     */
    public List<Message> findMessagesByState(PlantState state) {
        return messageRepository.findByState(state);
    }

    /**
     * 특정 상태 ID에 대한 메시지 조회
     */
    public List<Message> findMessagesByStateId(Long stateId) {
        return messageRepository.findByStateStateId(stateId);
    }

    /**
     * 특정 발신자 유형의 메시지 조회
     */
    public List<Message> findMessagesByPlantAndSenderType(Plant plant, String senderType) {
        return messageRepository.findByPlantAndSenderType(plant, senderType);
    }

    /**
     * 특정 식물 ID와 발신자 유형의 메시지 조회
     */
    public List<Message> findMessagesByPlantIdAndSenderType(Long plantId, String senderType) {
        return messageRepository.findByPlantPlantIdAndSenderType(plantId, senderType);
    }

    /**
     * 특정 기간 내의 메시지 조회
     */
    public List<Message> findMessagesByPlantAndDateRange(Plant plant, LocalDateTime start, LocalDateTime end) {
        return messageRepository.findByPlantAndCreatedAtBetween(plant, start, end);
    }

    /**
     * 특정 식물의 최근 메시지 조회
     */
    public List<Message> findRecentMessagesByPlant(Plant plant) {
        return messageRepository.findTop10ByPlantOrderByCreatedAtDesc(plant);
    }

    /**
     * 특정 식물 ID의 최근 메시지 조회
     */
    public List<Message> findRecentMessagesByPlantId(Long plantId) {
        return messageRepository.findTop10ByPlantPlantIdOrderByCreatedAtDesc(plantId);
    }

    /**
     * 메시지 생성 (식물 ID만 사용)
     */
    @Transactional
    public Message createMessage(Message message, Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new IllegalArgumentException("식물을 찾을 수 없습니다: " + plantId));
        
        message.setPlant(plant);
        return messageRepository.save(message);
    }

    /**
     * 메시지 생성 (식물 ID와 상태 ID 사용)
     */
    @Transactional
    public Message createMessage(Message message, Long plantId, Long stateId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new IllegalArgumentException("식물을 찾을 수 없습니다: " + plantId));
        
        PlantState state = null;
        if (stateId != null) {
            state = plantStateRepository.findById(stateId)
                    .orElseThrow(() -> new IllegalArgumentException("식물 상태를 찾을 수 없습니다: " + stateId));
        }
        
        message.setPlant(plant);
        message.setState(state);
        return messageRepository.save(message);
    }

    /**
     * 메시지 업데이트
     */
    @Transactional
    public Message updateMessage(Long messageId, Message messageDetails) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다: " + messageId));
        
        message.setContent(messageDetails.getContent());
        
        return messageRepository.save(message);
    }

    /**
     * 메시지 삭제
     */
    @Transactional
    public void deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다: " + messageId));
        
        messageRepository.delete(message);
    }

    /**
     * 특정 식물의 메시지 수 카운트
     */
    public long countMessagesByPlantId(Long plantId) {
        return messageRepository.countByPlantPlantId(plantId);
    }

    /**
     * 특정 발신자 유형의 메시지 수 카운트
     */
    public long countMessagesByPlantIdAndSenderType(Long plantId, String senderType) {
        return messageRepository.countByPlantPlantIdAndSenderType(plantId, senderType);
    }

    /**
     * 식물 상태 기반 자동 메시지 생성
     */
    @Transactional
    public Message generatePlantMessage(Long plantId, Long stateId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new IllegalArgumentException("식물을 찾을 수 없습니다: " + plantId));
        
        PlantState state = plantStateRepository.findById(stateId)
                .orElseThrow(() -> new IllegalArgumentException("식물 상태를 찾을 수 없습니다: " + stateId));
        
        // 식물 상태 기반 메시지 생성 로직
        String content = generateMessageContent(state);
        
        Message message = new Message();
        message.setPlant(plant);
        message.setState(state);
        message.setSenderType("plant");
        message.setContent(content);
        
        return messageRepository.save(message);
    }
    
    /**
     * 식물 상태 기반 메시지 내용 생성
     */
    private String generateMessageContent(PlantState state) {
        StringBuilder content = new StringBuilder();
        
        // 빛 수준 기반 메시지
        if (state.getLightLevel() != null) {
            if (state.getLightLevel() < 300) {
                content.append("빛이 부족해요. 좀 더 밝은 곳으로 옮겨주세요. ");
            } else if (state.getLightLevel() > 800) {
                content.append("빛이 너무 강해요. 직사광선을 피해주세요. ");
            } else {
                content.append("빛 수준이 적당해요. ");
            }
        }
        
        // 온도 기반 메시지
        if (state.getTemperature() != null) {
            if (state.getTemperature() < 15) {
                content.append("추워요! 따뜻한 곳으로 옮겨주세요. ");
            } else if (state.getTemperature() > 30) {
                content.append("너무 더워요! 시원한 곳으로 옮겨주세요. ");
            } else {
                content.append("온도가 적당해요. ");
            }
        }
        
        // 습도 기반 메시지
        if (state.getMoisture() != null) {
            if (state.getMoisture() < 30) {
                content.append("물이 필요해요! 물을 주세요. ");
            } else if (state.getMoisture() > 80) {
                content.append("물이 너무 많아요! 물 주기를 줄여주세요. ");
            } else {
                content.append("수분이 적당해요. ");
            }
        }
        
        // 터치 감지 기반 메시지
        if (state.getTouched() != null && state.getTouched()) {
            content.append("방금 저를 만졌네요! 반가워요. ");
        }
        
        // 메시지가 비어있으면 기본 메시지 추가
        if (content.length() == 0) {
            content.append("안녕하세요! 오늘도 잘 지내고 있어요.");
        }
        
        return content.toString();
    }
}
