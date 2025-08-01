package com.plantalk.chat.controller;

import com.plantalk.chat.model.entity.Message;
import com.plantalk.chat.model.entity.Plant;
import com.plantalk.chat.model.entity.PlantState;
import com.plantalk.chat.model.entity.User;
import com.plantalk.chat.service.MessageService;
import com.plantalk.chat.service.PlantService;
import com.plantalk.chat.service.PlantStateService;
import com.plantalk.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
public class MainController {

    private final PlantService plantService;
    private final MessageService messageService;
    private final PlantStateService plantStateService;
    private final UserService userService;

    /**
     * 메인 페이지 - 인증 상태에 따라 리다이렉트
     */
    @GetMapping("/")
    public String index() {
        // 인증 상태에 따라 리다이렉트 처리 (index.html에서 처리하므로 바로 반환)
        return "index";
    }
    
    /**
     * 식물 목록 페이지
     */
    @GetMapping("/plant-list")
    public String plantList(Model model) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // 이메일을 사용자명으로 사용
        
        // 사용자 정보 조회
        Optional<User> userOptional = userService.findUserByEmail(email);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 사용자의 식물 목록 조회
            List<Plant> plants = plantService.findPlantsByUserId(user.getUserId());
            
            model.addAttribute("plants", plants);
            model.addAttribute("user", user);
        }
        
        return "plant-list";
    }

    /**
     * 채팅 페이지 - 특정 식물과의 채팅
     */
    @GetMapping("/chat/{plantId}")
    public String chat(@PathVariable Long plantId, Model model) {
        // 식물 정보 조회
        Optional<Plant> plantOpt = plantService.findPlantById(plantId);
        if (plantOpt.isEmpty()) {
            return "redirect:/";
        }
        
        Plant plant = plantOpt.get();
        model.addAttribute("plant", plant);
        
        // 모든 식물 목록 (드롭다운용)
        List<Plant> plants = plantService.findAllPlants();
        model.addAttribute("plants", plants);
        
        // 해당 식물의 메시지 목록
        List<Message> messages = messageService.findMessagesByPlantId(plantId);
        model.addAttribute("messages", messages);
        
        // 해당 식물의 최신 상태 정보
        Optional<PlantState> latestStateOpt = plantStateService.findLatestPlantStateByPlantId(plantId);
        latestStateOpt.ifPresent(state -> model.addAttribute("latestState", state));
        
        return "chat";
    }

    /**
     * 채팅 페이지 - 식물 선택 없이 접근
     */
    @GetMapping("/chat")
    public String chatWithoutPlant(Model model) {
        // 모든 식물 목록 (드롭다운용)
        List<Plant> plants = plantService.findAllPlants();
        model.addAttribute("plants", plants);
        
        return "chat";
    }
    
    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    /**
     * 회원가입 페이지
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    /**
     * 식물 등록 페이지
     */
    @GetMapping("/plant-register")
    public String registerPlant(Model model) {
        // 디버깅을 위한 로그 추가
        System.out.println("===== 식물 등록 페이지 접근 =====");
        
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // 이메일을 사용자명으로 사용
        
        System.out.println("Authentication Name: " + email);
        System.out.println("Authentication Principal: " + authentication.getPrincipal());
        System.out.println("Authentication Details: " + authentication.getDetails());
        
        // 사용자 정보 조회
        Optional<User> userOptional = userService.findUserByEmail(email);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("User found: " + user.getUsername() + ", ID: " + user.getUserId());
            model.addAttribute("userId", user.getUserId());
        } else {
            System.out.println("User not found for email: " + email);
            // 사용자가 없는 경우 임시 값 설정 (테스트용)
            model.addAttribute("userId", 1L);
        }
        
        return "plant-register";
    }
    
    /**
     * 식물 수정 페이지
     */
    @GetMapping("/plant/edit/{plantId}")
    public String editPlant(@PathVariable Long plantId, Model model) {
        // 식물 정보 조회
        Optional<Plant> plantOpt = plantService.findPlantById(plantId);
        if (plantOpt.isEmpty()) {
            return "redirect:/";
        }
        
        Plant plant = plantOpt.get();
        model.addAttribute("plant", plant);
        
        // 해당 식물의 최신 상태 정보
        Optional<PlantState> latestStateOpt = plantStateService.findLatestPlantStateByPlantId(plantId);
        latestStateOpt.ifPresent(state -> model.addAttribute("latestState", state));
        
        // 해당 식물의 상태 기록 (최근 10개)
        List<PlantState> stateHistory = plantStateService.findPlantStatesByPlantId(plantId)
                .stream()
                .limit(10)
                .collect(Collectors.toList());
        model.addAttribute("stateHistory", stateHistory);
        
        return "plant-edit";
    }
}
