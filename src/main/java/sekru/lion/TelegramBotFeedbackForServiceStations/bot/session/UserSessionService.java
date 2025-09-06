package sekru.lion.TelegramBotFeedbackForServiceStations.bot.session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository repository;

    @Transactional(readOnly = true)
    public UserSession getOrCreate(Long chatId) {
        return repository.findById(chatId)
                .orElseGet(() -> UserSession.builder()
                        .chatId(chatId)
                        .state(SessionState.AWAITING_ROLE)
                        .build());
    }

    @Transactional
    public UserSession reset(Long chatId) {
        UserSession s = repository.findById(chatId).orElse(
                UserSession.builder().chatId(chatId).build()
        );
        s.setState(SessionState.AWAITING_ROLE);
        s.setRole(null);
        s.setBranch(null);
        return repository.save(s);
    }

    @Transactional
    public UserSession setRole(Long chatId, String role) {
        UserSession s = getOrCreate(chatId);
        s.setRole(role);
        s.setState(SessionState.AWAITING_BRANCH);
        return repository.save(s);
    }

    @Transactional
    public UserSession setBranch(Long chatId, String branch) {
        UserSession s = getOrCreate(chatId);
        s.setBranch(branch);
        s.setState(SessionState.READY);
        return repository.save(s);
    }

    @Transactional
    public UserSession save(UserSession s) {
        return repository.save(s);
    }
}
