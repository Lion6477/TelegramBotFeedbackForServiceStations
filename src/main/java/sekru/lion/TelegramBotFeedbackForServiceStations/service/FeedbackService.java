package sekru.lion.TelegramBotFeedbackForServiceStations.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sekru.lion.TelegramBotFeedbackForServiceStations.model.entity.Feedback;
import sekru.lion.TelegramBotFeedbackForServiceStations.model.request.FeedbackAnalysis;
import sekru.lion.TelegramBotFeedbackForServiceStations.repository.FeedbackRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {
    private final FeedbackRepository repository;
    private final OpenAIService openAIService;
    private final GoogleDocsService googleDocsService;
    private final ObjectMapper objectMapper;
    private final TrelloService trelloService;

    public Feedback saveFeedback(String role, String branch, String message) {
        String analysisRaw = openAIService.analyzeFeedbackRaw(message);

        FeedbackAnalysis parsed = tryParseJsonFromModel(analysisRaw);

        String sentiment;
        int criticality;
        String solution;

        if (parsed != null) {
            sentiment = safeString(parsed.sentiment(), "neutral");
            criticality = clampCriticality(parsed.criticality()); //never trust ai :)
            solution = safeString(parsed.solution(), "No suggestion");
        } else {
            // fallback: manual definition
            sentiment = manualDefinitionSentiment(message);
            criticality = manualDefinitionCriticality(message);
            solution = "No suggestion (auto-generated)";
        }

        Feedback feedback = Feedback.builder()
                .role(role)
                .branch(branch)
                .message(message)
                .sentiment(sentiment)
                .criticality(criticality)
                .solution(solution)
                .build();

        repository.save(feedback);

        try {
            googleDocsService.appendFeedback(feedback);
        } catch (Exception e) {
            log.warn("GoogleDocs append failed: {}", e.getMessage());
        }

        // TRELLO: create card if crit>=4
        if (criticality >= 4) {
            trelloService.createCardFromFeedback(feedback);
        }

        return feedback;
    }

    /**
     * Parse model response into FeedbackAnalysis.Class
     */
    private FeedbackAnalysis tryParseJsonFromModel(String raw) {
        if (raw == null) return null;

        int first = raw.indexOf('{');
        int last = raw.lastIndexOf('}');
        String candidate = raw;
        if (first >= 0 && last > first) {
            candidate = raw.substring(first, last + 1);
        }

        try {
            return objectMapper.readValue(candidate, FeedbackAnalysis.class);
        } catch (JsonProcessingException e) {
            log.warn("JSON parse failed for candidate: {} ; error: {}", candidate, e.getMessage());
            return null;
        }
    }

    private String manualDefinitionSentiment(String text) {
        String lower = text.toLowerCase();
        if (containsAny(lower, "поган", "жах", "не працює", "негатив", "затримк", "проблем")) return "negative";
        if (containsAny(lower, "дякую", "гарно", "добре", "задоволений", "плюс")) return "positive";
        return "neutral";
    }

    private int manualDefinitionCriticality(String text) {
        String lower = text.toLowerCase();
        if (containsAny(lower, "пожеж", "небезп", "безпек", "відключенн", "термінов")) return 5;
        if (containsAny(lower, "затримк", "неотрим", "збої", "серйоз")) return 4;
        if (containsAny(lower, "поган", "скарг", "проблем")) return 3;
        if (containsAny(lower, "бажан", "пропозиц")) return 2;
        return 1;
    }

    private boolean containsAny(String s, String... subs) {
        for (String sub : subs) {
            if (s.contains(sub)) {
                return true;
            }
        }
        return false;
    }

    private int clampCriticality(int v) {
        if (v < 1) return 1;
        if (v > 5) return 5;
        return v;
    }

    private String safeString(String s, String def) {
        if (s == null || s.isBlank()) {
            return def;
        } else return s.trim();
    }
}
