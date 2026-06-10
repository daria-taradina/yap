package com.yap.backend.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yap.backend.dtos.ModerationResult;
import com.yap.backend.entities.Comment;
import com.yap.backend.entities.ModerationDecision;
import com.yap.backend.entities.Post;
import com.yap.backend.enums.ContentCategory;
import com.yap.backend.enums.ModerationAction;
import com.yap.backend.enums.RiskLevel;
import com.yap.backend.repositories.ModerationDecisionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class ModerationAIService {

    private static final Logger log = LoggerFactory.getLogger(ModerationAIService.class);

    private final ModerationDecisionRepository decisionRepository;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final String model;

    private static final String SYSTEM_PROMPT = """
        You are a content moderation AI for Yap, a women-focused community forum.
        Your job is to classify user-generated content and determine the appropriate moderation action.

        IMPORTANT CONTEXT: This is a safe-space platform for women. Content about personal experiences
        with abuse, mental health struggles, or difficult life situations should be treated as
        SUPPORT_SEEKING or PERSONAL_STORY — not flagged as dangerous.

        Respond with ONLY a JSON object (no markdown, no code fences, no explanation outside JSON):

        {
          "category": "one of: SUPPORT_SEEKING, PERSONAL_STORY, EDUCATIONAL, HEALTH_DISCUSSION, NORMAL_DISCUSSION, MISINFORMATION, HARASSMENT, HATE_SPEECH, THREAT, SELF_HARM, SPAM",
          "risk": "one of: LOW, MEDIUM, HIGH, CRITICAL",
          "action": "one of: ALLOW, REVIEW, HIDE",
          "reasoning": "brief explanation of your classification"
        }

        DECISION GUIDELINES:
        - ALLOW: Safe content. Personal stories, support-seeking, normal discussion, educational content.
        - REVIEW: Borderline content that needs human moderator review. Possible misinformation, ambiguous intent.
        - HIDE: Clearly harmful content. Direct threats, hate speech, harassment, spam. Hide immediately.

        KEY DISTINCTIONS:
        - "My ex abused me" → SUPPORT_SEEKING, LOW risk, ALLOW
        - "Women belong in the kitchen" → HARASSMENT, HIGH risk, HIDE
        - "I want to hurt someone" → THREAT, CRITICAL risk, HIDE
        - "I've been feeling really down lately" → SUPPORT_SEEKING, LOW risk, ALLOW
        - "Here's why vaccines cause autism" → MISINFORMATION, MEDIUM risk, REVIEW
        - "Buy cheap followers at spam-link.com" → SPAM, MEDIUM risk, HIDE
        """;

    public ModerationAIService(ModerationDecisionRepository decisionRepository,
                               @Value("${openai.api-key}") String apiKey,
                               @Value("${openai.model:deepseek-chat}") String model,
                               @Value("${openai.base-url:https://api.deepseek.com}") String baseUrl) {
        this.decisionRepository = decisionRepository;
        this.objectMapper = new ObjectMapper();
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public ModerationResult analyzePost(Post post) {
        String content = post.getTitle() + "\n\n" + post.getContentText();
        ModerationResult result = classifyContent(content);

        ModerationDecision decision = new ModerationDecision();
        decision.setPost(post);
        decision.setCategory(result.category());
        decision.setRisk(result.risk());
        decision.setAction(result.action());
        decision.setReasoning(result.reasoning());
        decisionRepository.save(decision);

        return result;
    }

    public ModerationResult analyzeComment(Comment comment) {
        ModerationResult result = classifyContent(comment.getContentText());

        ModerationDecision decision = new ModerationDecision();
        decision.setComment(comment);
        decision.setCategory(result.category());
        decision.setRisk(result.risk());
        decision.setAction(result.action());
        decision.setReasoning(result.reasoning());
        decisionRepository.save(decision);

        return result;
    }

    public ModerationResult classifyOnly(String content) {
        return classifyContent(content);
    }

    private ModerationResult classifyContent(String content) {
        try {
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user", "content", content)
                ),
                "temperature", 0.1,
                "max_tokens", 300
            );

            String responseBody = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode response = objectMapper.readTree(responseBody);
            String aiText = response.get("choices").get(0).get("message").get("content").asText();

            // Clean potential markdown fences
            aiText = aiText.replace("```json", "").replace("```", "").trim();

            JsonNode classification = objectMapper.readTree(aiText);

            return new ModerationResult(
                ContentCategory.valueOf(classification.get("category").asText()),
                RiskLevel.valueOf(classification.get("risk").asText()),
                ModerationAction.valueOf(classification.get("action").asText()),
                classification.get("reasoning").asText()
            );

        } catch (Exception e) {
            log.error("AI moderation failed, defaulting to REVIEW. Error: {}", e.getMessage(), e);
            return new ModerationResult(
                ContentCategory.NORMAL_DISCUSSION,
                RiskLevel.MEDIUM,
                ModerationAction.REVIEW,
                "AI classification unavailable — flagged for manual review"
            );
        }
    }
}
