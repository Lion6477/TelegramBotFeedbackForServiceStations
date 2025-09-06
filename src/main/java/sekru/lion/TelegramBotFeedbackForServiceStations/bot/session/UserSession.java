package sekru.lion.TelegramBotFeedbackForServiceStations.bot.session;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SessionState state;

    @Column(length = 64)
    private String role;

    @Column(length = 128)
    private String branch;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
        if (this.state == null) {
            this.state = SessionState.AWAITING_ROLE;
        }
    }
}
