package com.plantalk.chat.service;

import com.plantalk.chat.model.entity.Plant;
import com.plantalk.chat.model.entity.User;
import com.plantalk.chat.repository.PlantRepository;
import com.plantalk.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantService {

    private final PlantRepository plantRepository;
    private final UserRepository userRepository;

    /**
     * 모든 식물 조회
     */
    public List<Plant> findAllPlants() {
        return plantRepository.findAll();
    }

    /**
     * ID로 식물 조회
     */
    public Optional<Plant> findPlantById(Long plantId) {
        return plantRepository.findById(plantId);
    }

    /**
     * 사용자의 모든 식물 조회
     */
    public List<Plant> findPlantsByUser(User user) {
        return plantRepository.findByUser(user);
    }

    /**
     * 사용자 ID로 모든 식물 조회
     */
    public List<Plant> findPlantsByUserId(Long userId) {
        return plantRepository.findByUserUserId(userId);
    }

    /**
     * 사용자와 식물 이름으로 식물 조회
     */
    public Optional<Plant> findPlantByUserAndName(User user, String name) {
        return plantRepository.findByUserAndName(user, name);
    }

    /**
     * 사용자 ID와 식물 이름으로 식물 조회
     */
    public Optional<Plant> findPlantByUserIdAndName(Long userId, String name) {
        return plantRepository.findByUserUserIdAndName(userId, name);
    }

    /**
     * 식물 생성
     */
    @Transactional
    public Plant createPlant(Plant plant, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        // 같은 사용자의 같은 이름의 식물이 이미 존재하는지 확인
        if (plantRepository.findByUserAndName(user, plant.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 같은 이름의 식물이 존재합니다: " + plant.getName());
        }
        
        plant.setUser(user);
        return plantRepository.save(plant);
    }

    /**
     * 식물 정보 업데이트
     */
    @Transactional
    public Plant updatePlant(Long plantId, Plant plantDetails) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new IllegalArgumentException("식물을 찾을 수 없습니다: " + plantId));
        
        // 이름이 변경되었고, 같은 사용자의 같은 이름의 식물이 이미 존재하는지 확인
        if (!plant.getName().equals(plantDetails.getName()) && 
                plantRepository.findByUserAndName(plant.getUser(), plantDetails.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 같은 이름의 식물이 존재합니다: " + plantDetails.getName());
        }
        
        plant.setName(plantDetails.getName());
        plant.setSpecies(plantDetails.getSpecies());
        
        return plantRepository.save(plant);
    }

    /**
     * 식물 삭제
     */
    @Transactional
    public void deletePlant(Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new IllegalArgumentException("식물을 찾을 수 없습니다: " + plantId));
        
        plantRepository.delete(plant);
    }

    /**
     * 사용자가 가진 식물 수 카운트
     */
    public long countPlantsByUserId(Long userId) {
        return plantRepository.countByUserUserId(userId);
    }
}
