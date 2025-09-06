package sekru.lion.TelegramBotFeedbackForServiceStations.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.BatchUpdateDocumentRequest;
import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.Request;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import sekru.lion.TelegramBotFeedbackForServiceStations.model.entity.Feedback;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class GoogleDocsService {

    private static final String APPLICATION_NAME = "Telegram Feedback Bot";
    private final Docs docsService;
    private final String documentId;

    public GoogleDocsService(@Value("${google.docs.document-id}") String documentId) throws IOException {
        this.documentId = documentId;

        try (InputStream in = new ClassPathResource("sa-key.json").getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(in)
                    .createScoped(Collections.singleton(DocsScopes.DOCUMENTS));

            this.docsService = new Docs.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
    }

    public void appendFeedback(Feedback feedback) {
        try {
            String text = String.format(
                    "📝 [%s | %s]\n%s\n\n",
                    feedback.getRole(),
                    feedback.getBranch(),
                    feedback.getMessage()
            );

            Request insertText = new Request()
                    .setInsertText(new InsertTextRequest()
                            .setText(text)
                            .setEndOfSegmentLocation(new com.google.api.services.docs.v1.model.EndOfSegmentLocation())
                    );

            BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest()
                    .setRequests(List.of(insertText));

            docsService.documents().batchUpdate(documentId, body).execute();
            log.info("Feedback appended to Google Doc: {}", feedback.getId());
        } catch (Exception e) {
            log.error("Error appending feedback to Google Doc", e);
        }
    }
}
