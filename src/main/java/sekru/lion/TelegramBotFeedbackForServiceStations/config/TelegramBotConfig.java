package sekru.lion.TelegramBotFeedbackForServiceStations.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

//Sometimes tg library can't compile bot without this config
@Configuration
public class TelegramBotConfig {

    private final ApplicationContext ctx;

    public TelegramBotConfig(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @PostConstruct
    public void registerBots() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            TelegramLongPollingBot bot = ctx.getBean("feedbackBot", TelegramLongPollingBot.class);
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            throw new IllegalStateException("Failed to register Telegram bot", e);
        }
    }
}
