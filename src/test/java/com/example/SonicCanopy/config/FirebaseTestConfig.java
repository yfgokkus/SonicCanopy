package com.example.SonicCanopy.config;

import com.google.cloud.storage.Bucket;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class FirebaseTestConfig {
    @Bean
    public Bucket storageBucket() {
        return Mockito.mock(Bucket.class);
    }
}
