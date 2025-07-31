package com.plantalk.chat.service;

import com.plantalk.chat.model.entity.User;
import com.plantalk.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 모든 사용자 조회
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * ID로 사용자 조회
     */
    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * 이메일로 사용자 조회
     */
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    // Spring Security가 인증을 처리하므로 authenticateUser 메서드 제거

    /**
     * 사용자명으로 사용자 조회
     */
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 사용자 생성
     */
    @Transactional
    public User createUser(User user) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + user.getEmail());
        }
        
        // 사용자명 중복 체크
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다: " + user.getUsername());
        }
        
        // Spring Security의 PasswordEncoder를 사용하여 비밀번호 인코딩
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        return userRepository.save(user);
    }

    /**
     * 사용자 정보 업데이트
     */
    @Transactional
    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        // 이메일이 변경되었고, 새 이메일이 이미 사용 중인 경우
        if (!user.getEmail().equals(userDetails.getEmail()) && 
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + userDetails.getEmail());
        }
        
        // 사용자명이 변경되었고, 새 사용자명이 이미 사용 중인 경우
        if (!user.getUsername().equals(userDetails.getUsername()) && 
                userRepository.existsByUsername(userDetails.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다: " + userDetails.getUsername());
        }
        
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        
        return userRepository.save(user);
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        userRepository.delete(user);
    }

    /**
     * 이메일 존재 여부 확인
     */
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 사용자명 존재 여부 확인
     */
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
