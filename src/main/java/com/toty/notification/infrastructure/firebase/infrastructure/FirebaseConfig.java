package com.toty.notification.infrastructure.firebase.infrastructure;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {
    @Value("${firebase.service-account-json}")
    private String firebaseJsonBase64;

    @Value("${firebase.project-id}")
    private String projectId;

    @PostConstruct
    public void init() throws Exception {

        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        byte[] decoded = Base64.getDecoder().decode(firebaseJsonBase64);
        InputStream is = new ByteArrayInputStream(decoded);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(is))
                .setProjectId(projectId)
                .build();

        FirebaseApp.initializeApp(options);
    }
}


