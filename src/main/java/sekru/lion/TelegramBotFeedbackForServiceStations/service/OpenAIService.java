package sekru.lion.TelegramBotFeedbackForServiceStations.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


/**
 * Using default springAI lib as an openai client
 */
@Service
public class OpenAIService {

    private final ChatClient chatClient;

    public OpenAIService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Return raw text response (String.content()).
     * Using system-message for suggestion response format.
     */
    public String analyzeFeedbackRaw(String feedbackText) {
        return this.chatClient.prompt()
                .system("You are an assistant that analyzes employee feedback. " +
                        "For the given feedback determine: sentiment (negative|neutral|positive), " +
                        "criticality (1-5), and a short suggestion (solution). " +
                        "Return only a valid JSON object with fields: sentiment, criticality, solution.")
                .user("Feedback: " + feedbackText)
                .call()
                .content(); //result as string
    }
}
