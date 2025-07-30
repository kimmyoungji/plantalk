package com.plantalk.chat.controller.api;

import com.plantalk.chat.dto.ResponseDTO;
import com.plantalk.chat.dto.UserDTO;
import com.plantalk.chat.model.entity.User;
import com.plantalk.chat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    /**
     * 로그인 (이메일로 사용자 조회)
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<UserDTO.Response>> login(@Valid @RequestBody UserDTO.LoginRequest request) {
        Optional<User> userOpt = userService.findUserByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.fail("사용자를 찾을 수 없습니다: " + request.getEmail()));
        }
        
        User user = userOpt.get();
        UserDTO.Response response = UserDTO.Response.fromEntity(user);
        
        return ResponseEntity.ok(ResponseDTO.success("로그인 성공", response));
    }

    /**
     * 회원 가입
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<UserDTO.Response>> register(@Valid @RequestBody UserDTO.Request request) {
        try {
            // 이메일 중복 체크
            if (userService.isEmailExists(request.getEmail())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ResponseDTO.fail("이미 사용 중인 이메일입니다: " + request.getEmail()));
            }
            
            // 사용자명 중복 체크
            if (userService.isUsernameExists(request.getUsername())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ResponseDTO.fail("이미 사용 중인 사용자명입니다: " + request.getUsername()));
            }
            
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            
            User savedUser = userService.createUser(user);
            UserDTO.Response response = UserDTO.Response.fromEntity(savedUser);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ResponseDTO.success("회원 가입 성공", response));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDTO.fail("회원 가입 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 이메일 중복 체크
     */
    @GetMapping("/check-email")
    public ResponseEntity<ResponseDTO<Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email);
        
        if (exists) {
            return ResponseEntity.ok(ResponseDTO.fail("이미 사용 중인 이메일입니다."));
        } else {
            return ResponseEntity.ok(ResponseDTO.success("사용 가능한 이메일입니다.", false));
        }
    }

    /**
     * 사용자명 중복 체크
     */
    @GetMapping("/check-username")
    public ResponseEntity<ResponseDTO<Boolean>> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.isUsernameExists(username);
        
        if (exists) {
            return ResponseEntity.ok(ResponseDTO.fail("이미 사용 중인 사용자명입니다."));
        } else {
            return ResponseEntity.ok(ResponseDTO.success("사용 가능한 사용자명입니다.", false));
        }
    }
}
