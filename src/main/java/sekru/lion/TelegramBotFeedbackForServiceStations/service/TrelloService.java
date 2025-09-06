package sekru.lion.TelegramBotFeedbackForServiceStations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import sekru.lion.TelegramBotFeedbackForServiceStations.model.entity.Feedback;
import sekru.lion.TelegramBotFeedbackForServiceStations.model.request.TrelloCardResponse;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrelloService {

    @Value("${trello.api.base-url:https://api.trello.com/1}")
    private String baseUrl;

    @Value("${trello.api.key}")
    private String apiKey;

    @Value("${trello.api.token}")
    private String apiToken;

    @Value("${trello.list.id}")
    private String listId;

    @Value("${trello.labels.critical-ids:}")
    private String criticalLabelIds;

    private final RestTemplate rest = new RestTemplate();

    public TrelloCardResponse createCardFromFeedback(Feedback fb) {
        String title = "[CRIT " + fb.getCriticality() + "] Role:" + fb.getRole() + " Branch: " + fb.getBranch();
        String desc = buildDescription(fb);
        OffsetDateTime due = OffsetDateTime.now().plusDays(2);
        return createCard(title, desc, due);
    }

    private TrelloCardResponse createCard(String name, String desc, OffsetDateTime due) {
        String url = baseUrl + "/cards";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("name", name);
        form.add("desc", desc);
        form.add("idList", listId);
        form.add("pos", "top");
        form.add("key", apiKey);
        form.add("token", apiToken);

        if (due != null) {
            form.add("due", due.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }

        if (criticalLabelIds != null && !criticalLabelIds.isBlank()) {
            form.add("idLabels", criticalLabelIds);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            log.info("Requesting Trello Card Request");
            return rest.postForObject(url, new HttpEntity<>(form, headers), TrelloCardResponse.class);
        } catch (RestClientException ex) {
            log.error("Trello createCard error: {}", ex.getMessage(), ex);
            return null;
        }
    }

    private String buildDescription(Feedback fb) {
        StringJoiner j = new StringJoiner("\n");
        j.add("Role: " + nullSafe(fb.getRole()));
        j.add("Branch: " + nullSafe(fb.getBranch()));
        j.add("Sentiment: " + nullSafe(fb.getSentiment()));
        j.add("Criticality: " + fb.getCriticality());
        j.add("");
        j.add("Message:");
        j.add(nullSafe(fb.getMessage()));
        j.add("");
        j.add("Suggested solution:");
        j.add(nullSafe(fb.getSolution()));
        return j.toString();
    }

    private String nullSafe(String v) {
        return v == null ? "-" : v;
    }
}
