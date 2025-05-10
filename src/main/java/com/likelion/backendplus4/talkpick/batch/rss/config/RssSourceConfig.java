package com.likelion.backendplus4.talkpick.batch.rss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "rss")
@Data
public class RssSourceConfig {

    private List<RssSource> sources = new ArrayList<>();

    @Data
    public static class RssSource {
        private String name;
        private String url;
        private String type;
        private boolean enabled = true;
    }
}