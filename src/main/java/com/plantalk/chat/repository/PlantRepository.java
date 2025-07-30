package com.plantalk.chat.repository;

import com.plantalk.chat.model.entity.Plant;
import com.plantalk.chat.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {
    
    // 사용자의 모든 식물 찾기
    List<Plant> findByUser(User user);
    
    // 사용자 ID로 모든 식물 찾기
    List<Plant> findByUserUserId(Long userId);
    
    // 사용자와 식물 이름으로 식물 찾기
    Optional<Plant> findByUserAndName(User user, String name);
    
    // 사용자 ID와 식물 이름으로 식물 찾기
    Optional<Plant> findByUserUserIdAndName(Long userId, String name);
    
    // 사용자가 가진 식물 수 카운트
    long countByUserUserId(Long userId);
    
}
