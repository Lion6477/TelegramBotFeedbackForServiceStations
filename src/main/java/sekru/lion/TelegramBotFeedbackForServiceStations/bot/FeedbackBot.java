package sekru.lion.TelegramBotFeedbackForServiceStations.bot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import sekru.lion.TelegramBotFeedbackForServiceStations.bot.session.SessionState;
import sekru.lion.TelegramBotFeedbackForServiceStations.bot.session.UserSession;
import sekru.lion.TelegramBotFeedbackForServiceStations.bot.session.UserSessionService;
import sekru.lion.TelegramBotFeedbackForServiceStations.model.entity.Feedback;
import sekru.lion.TelegramBotFeedbackForServiceStations.service.FeedbackService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedbackBot extends TelegramLongPollingBot {

    private final FeedbackService feedbackService;
    private final UserSessionService sessionService;

    @Value("${telegram.bot.username}")
    private String botUsername;
    @Value("${telegram.bot.token}")
    private String botToken;

    @PostConstruct
    public void init() {
        try {
            List<BotCommand> commands = new ArrayList<>();
            commands.add(new BotCommand("/start", "Почати роботу з ботом"));
            commands.add(new BotCommand("/help", "Допомога"));
            execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (Exception e) {
            log.warn("Cannot register commands: {}", e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update == null || !update.hasMessage() || !update.getMessage().hasText()) return;

            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText().trim();

            if (text.equalsIgnoreCase("/start")) {
                sessionService.reset(chatId);
                sendMsgWithKeyboard(chatId,
                        "Вітаю! Я збираю анонімні відгуки співробітників.\n" +
                                "Спочатку виберіть вашу посаду:",
                        rolesKeyboard());
                return;
            }

            if (text.equalsIgnoreCase("/help")) {
                sendMsg(chatId,
                        "Я приймаю відгуки анонімно. Натисніть /start, оберіть посаду і введіть філію, " +
                                "після чого просто надсилайте повідомлення з фідбеком.");
                return;
            }

            UserSession session = sessionService.getOrCreate(chatId);

            if (session.getState() == SessionState.AWAITING_ROLE) {
                String role = normalizeRole(text);
                session = sessionService.setRole(chatId, role);
                sendMsg(chatId, "Дякую. Вкажіть, будь ласка, назву вашої філії (відділення):");
                return;
            }

            if (session.getState() == SessionState.AWAITING_BRANCH) {
                session = sessionService.setBranch(chatId, text);
                sendMsg(chatId, "Дякую — тепер надсилайте ваші відгуки у будь-який час. " +
                        "Щоб змінити роль/філію — надішліть /start.");
                return;
            }

            // READY: any text - feedback
            if (session.getState() == SessionState.READY) {
                Feedback fb = feedbackService.saveFeedback(session.getRole(), session.getBranch(), text);
                sendMsg(chatId, "Дякуємо! Ваш відгук збережено. Тональність: " + fb.getSentiment()
                        + " | Критичність: " + fb.getCriticality());
                return;
            }

            sendMsg(chatId, "Невідомий стан. Наберіть /start для початку.");

        } catch (Exception e) {
            log.error("Error processing update: ", e);
        }
    }

    private String normalizeRole(String text) {
        String l = text.toLowerCase();
        if (l.contains("механ") || l.contains("mechan")) return "mechanic";
        if (l.contains("елект") || l.contains("electric")) return "electric";
        if (l.contains("менедж") || l.contains("manager")) return "manager";
        return text;
    }

    private ReplyKeyboardMarkup rolesKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setSelective(true);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow r1 = new KeyboardRow();
        r1.add("Механік");
        r1.add("Електрик");
        rows.add(r1);

        KeyboardRow r2 = new KeyboardRow();
        r2.add("Менеджер");
        rows.add(r2);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void sendMsg(Long chatId, String text) {
        try {
            SendMessage msg = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .build();
            execute(msg);
        } catch (Exception e) {
            log.error("sendMsg error: {}", e.getMessage(), e);
        }
    }

    private void sendMsgWithKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        try {
            SendMessage msg = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .replyMarkup(keyboard)
                    .build();
            execute(msg);
        } catch (Exception e) {
            log.error("sendMsgWithKeyboard error: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
