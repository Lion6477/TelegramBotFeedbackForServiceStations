package sekru.lion.TelegramBotFeedbackForServiceStations.bot.session;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
}
