package com.plantalk.chat.controller;

import com.plantalk.chat.model.entity.Message;
import com.plantalk.chat.model.entity.Plant;
import com.plantalk.chat.model.entity.PlantState;
import com.plantalk.chat.service.MessageService;
import com.plantalk.chat.service.PlantService;
import com.plantalk.chat.service.PlantStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PlantService plantService;
    private final MessageService messageService;
    private final PlantStateService plantStateService;

    /**
     * 메인 페이지 - 식물 목록 표시
     */
    @GetMapping("/")
    public String index(Model model) {
        List<Plant> plants = plantService.findAllPlants();
        model.addAttribute("plants", plants);
        return "index";
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
        Optional<PlantState> latestStateOpt = plantStateService.findLatestStateByPlantId(plantId);
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
}
