package com.toty.notification.infrastructure.firebase.infrastructure;

import com.google.firebase.messaging.*;
import com.toty.notification.infrastructure.firebase.dto.SendMulticastResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class MulticastResultConfig {
    public SendMulticastResult sendMulticastResult(
            Collection<String> tokens,
            String title,
            String body,
            Map<String, String> data,
            int batchSize,
            boolean dryRun
    ) {

        if (tokens == null || tokens.isEmpty()) {
            return new SendMulticastResult(
                    0,
                    0,
                    0,
                    Collections.emptyList(),  // invalidTokens
                    Collections.emptyMap()    // tokenErrors
            );
        }

        List<List<String>> batches = splitIntoBatches(new ArrayList<>(tokens), Math.max(1, batchSize));

        int totalSuccess = 0;
        int totalFailure = 0;
        Map<String, String> tokenErrors = new LinkedHashMap<>();
        List<String> invalidTokens = new ArrayList<>();

        for (List<String> batch : batches) {
            MulticastMessage.Builder builder = MulticastMessage.builder()
                    .addAllTokens(batch)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                data.forEach(builder::putData);
            }

            try {
                BatchResponse response = FirebaseMessaging.getInstance()
                        .sendEachForMulticast(builder.build(), dryRun);

                totalSuccess += response.getSuccessCount();
                totalFailure += response.getFailureCount();

                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    SendResponse r = responses.get(i);
                    String token = batch.get(i);

                    if (!r.isSuccessful()) {
                        String msg = (r.getException() != null)
                                ? r.getException().getMessage()
                                : "Unknown error";
                        tokenErrors.put(token, msg);

                        if (isInvalidToken(r)) invalidTokens.add(token);
                    }
                }
            } catch (FirebaseMessagingException e) {
                for (String token : batch) {
                    tokenErrors.put(token, e.getMessage());
                }
                totalFailure += batch.size();
                log.error("FCM batch send failed: {}", e.getMessage(), e);
            }
        }

        SendMulticastResult result = new SendMulticastResult(
                tokens.size(),
                totalSuccess,
                totalFailure,
                invalidTokens,
                tokenErrors
        );

        log.info("[FCM] requested={}, success={}, failure={}, invalidTokens={}",
                result.getRequestedCount(),
                result.getSuccessCount(),
                result.getFailureCount(),
                result.getInvalidTokens().size()
        );

        return result;
    }


    private static List<List<String>> splitIntoBatches(List<String> tokens, int batchSize) {
        List<List<String>> out = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i += batchSize) {
            out.add(tokens.subList(i, Math.min(i + batchSize, tokens.size())));
        }
        return out;
    }

    private static boolean isInvalidToken(SendResponse r) {
        if (r == null || r.isSuccessful() || r.getException() == null) return false;

        MessagingErrorCode code = r.getException().getMessagingErrorCode();
        if (code == null) return false;

        return switch (code) {
            case UNREGISTERED,
                 INVALID_ARGUMENT,
                 SENDER_ID_MISMATCH -> true;
            default -> false;
        };
    }
}
