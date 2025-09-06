package sekru.lion.TelegramBotFeedbackForServiceStations.model.request;

public record TrelloCardResponse(
        String id,
        String idList,
        String shortUrl,
        String url,
        String name
) {
}
