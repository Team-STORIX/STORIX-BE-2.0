package com.storix.infrastructure.external.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.storix.common.property.FirebaseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "firebase", name = "credentials-base64")
public class FirebaseAppConfig {

    private static final String FIREBASE_APP_NAME = "storix-fcm";

    private final FirebaseProperties firebaseProperties;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // 1. 동일 이름의 FirebaseApp 이 이미 초기화되어 있으면 재사용
        for (FirebaseApp existing : FirebaseApp.getApps()) {
            if (FIREBASE_APP_NAME.equals(existing.getName())) {
                return existing;
            }
        }

        // 2. credentials base64 디코딩 후 FirebaseOptions 빌드
        byte[] decoded = Base64.getDecoder().decode(firebaseProperties.getCredentialsBase64());
        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(firebaseProperties.getProjectId())
                .build();

        // 3. 신규 FirebaseApp 초기화
        FirebaseApp app = FirebaseApp.initializeApp(options, FIREBASE_APP_NAME);
        log.info(">>>> [FCM] FirebaseApp 초기화 완료 (projectId={})", firebaseProperties.getProjectId());
        return app;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
