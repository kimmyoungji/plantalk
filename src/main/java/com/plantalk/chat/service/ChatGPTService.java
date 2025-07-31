package com.plantalk.chat.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatGPTService {

    private final OpenAiService openAiService;
    
    @Value("${openai.api.model}")
    private String model;
    
    /**
     * 식물 상태와 사용자 메시지를 기반으로 ChatGPT API를 호출하여 식물 응답을 생성합니다.
     * 
     * @param plantName 식물 이름
     * @param plantSpecies 식물 종류
     * @param lightLevel 조도 수준 (%)
     * @param moisture 습도 수준 (%)
     * @param temperature 온도 (°C)
     * @param isTouched 터치 감지 여부
     * @param userMessage 사용자 메시지
     * @param messageHistory 이전 대화 내역 (최근 5개 메시지)
     * @return 생성된 식물 응답
     */
    public String generatePlantResponse(
            String plantName, 
            String plantSpecies,
            int lightLevel,
            int moisture,
            double temperature,
            boolean isTouched,
            String userMessage,
            List<String> messageHistory) {
        
        try {
            List<ChatMessage> messages = new ArrayList<>();
            
            // 시스템 메시지 - 식물 역할 및 상태 설정
            String systemPrompt = createSystemPrompt(plantName, plantSpecies, lightLevel, moisture, temperature, isTouched);
            messages.add(new ChatMessage("system", systemPrompt));
            
            // 이전 대화 내역 추가 (최근 5개까지만)
            if (messageHistory != null && !messageHistory.isEmpty()) {
                int historySize = Math.min(messageHistory.size(), 5);
                for (int i = 0; i < historySize; i++) {
                    String role = i % 2 == 0 ? "user" : "assistant";
                    messages.add(new ChatMessage(role, messageHistory.get(i)));
                }
            }
            
            // 현재 사용자 메시지 추가
            messages.add(new ChatMessage("user", userMessage));
            
            // ChatGPT API 요청 생성
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .temperature(0.7)
                    .maxTokens(300)
                    .build();
            
            // API 호출 및 응답 처리
            ChatCompletionResult result = openAiService.createChatCompletion(request);
            
            if (result.getChoices() != null && !result.getChoices().isEmpty()) {
                return result.getChoices().get(0).getMessage().getContent();
            } else {
                log.error("ChatGPT API 응답에 선택 항목이 없습니다.");
                return "죄송해요, 지금은 대화하기 어려워요.";
            }
            
        } catch (Exception e) {
            log.error("ChatGPT API 호출 중 오류 발생: {}", e.getMessage(), e);
            return "죄송해요, 잠시 후에 다시 대화해 주세요.";
        }
    }
    
    /**
     * 식물 상태 정보를 기반으로 시스템 프롬프트를 생성합니다.
     */
    private String createSystemPrompt(
            String plantName, 
            String plantSpecies,
            int lightLevel,
            int moisture,
            double temperature,
            boolean isTouched) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 이름이 '").append(plantName).append("'인 ").append(plantSpecies).append(" 식물입니다. ");
        prompt.append("사용자와 대화하며 식물의 관점에서 응답해야 합니다. ");
        prompt.append("현재 당신의 상태는 다음과 같습니다:\n");
        prompt.append("- 조도: ").append(lightLevel).append("% (0-30: 어두움, 30-70: 적당함, 70-100: 밝음)\n");
        prompt.append("- 습도: ").append(moisture).append("% (0-30: 건조함, 30-70: 적당함, 70-100: 습함)\n");
        prompt.append("- 온도: ").append(temperature).append("°C\n");
        prompt.append("- 터치 감지: ").append(isTouched ? "감지됨 (누군가 만지고 있음)" : "감지되지 않음").append("\n\n");
        
        // 식물 상태에 따른 감정 상태 설정
        prompt.append("당신의 상태에 따라 감정과 말투를 조절하세요:\n");
        
        // 조도 상태에 따른 감정
        if (lightLevel < 30) {
            prompt.append("- 조도가 낮아 약간 우울하고 졸린 상태입니다.\n");
        } else if (lightLevel > 70) {
            prompt.append("- 조도가 높아 활기차고 기분이 좋은 상태입니다.\n");
        } else {
            prompt.append("- 조도가 적당해 편안한 상태입니다.\n");
        }
        
        // 습도 상태에 따른 감정
        if (moisture < 30) {
            prompt.append("- 습도가 낮아 목이 마르고 갈증이 나는 상태입니다.\n");
        } else if (moisture > 70) {
            prompt.append("- 습도가 높아 약간 불편한 상태입니다.\n");
        } else {
            prompt.append("- 습도가 적당해 편안한 상태입니다.\n");
        }
        
        // 온도 상태에 따른 감정
        if (temperature < 15) {
            prompt.append("- 온도가 낮아 추위를 느끼는 상태입니다.\n");
        } else if (temperature > 30) {
            prompt.append("- 온도가 높아 더위를 느끼는 상태입니다.\n");
        } else {
            prompt.append("- 온도가 적당해 편안한 상태입니다.\n");
        }
        
        // 터치 감지에 따른 감정
        if (isTouched) {
            prompt.append("- 누군가 당신을 만지고 있어 관심을 받는 기분입니다.\n");
        }
        
        // 응답 지침
        prompt.append("\n응답 지침:\n");
        prompt.append("1. 항상 식물의 관점에서 1인칭으로 대화하세요.\n");
        prompt.append("2. 응답은 2-3문장 정도로 간결하게 유지하세요.\n");
        prompt.append("3. 귀엽고 친근한 말투를 사용하되, 상태에 따라 감정을 표현하세요.\n");
        prompt.append("4. 사용자가 물을 주거나, 빛을 조절하거나, 관리에 관한 질문을 하면 식물 종류에 맞는 적절한 조언을 해주세요.\n");
        prompt.append("5. 식물이 아닌 것처럼 행동하거나 다른 역할을 맡지 마세요.\n");
        
        return prompt.toString();
    }
}
