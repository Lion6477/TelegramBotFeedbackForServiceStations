package sekru.lion.TelegramBotFeedbackForServiceStations.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FeedbackAnalysis(
        @JsonProperty("sentiment") String sentiment,
        @JsonProperty("criticality") int criticality,
        @JsonProperty("solution") String solution
) {}
