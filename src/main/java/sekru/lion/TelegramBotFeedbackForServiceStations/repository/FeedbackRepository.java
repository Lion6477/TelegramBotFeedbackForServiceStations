package sekru.lion.TelegramBotFeedbackForServiceStations.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import sekru.lion.TelegramBotFeedbackForServiceStations.model.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
