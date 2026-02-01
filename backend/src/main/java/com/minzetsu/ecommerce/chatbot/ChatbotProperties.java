package com.minzetsu.ecommerce.chatbot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "chatbot")
public class ChatbotProperties {
    private boolean enabled;
    private String baseUrl;
    private String apiKey;
    private String model;
    private boolean projectSearchEnabled = true;
    private String projectRoot;
    private int projectMaxFiles = 600;
    private int projectMaxFileBytes = 200_000;
    private int projectMaxSnippets = 6;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isProjectSearchEnabled() {
        return projectSearchEnabled;
    }

    public void setProjectSearchEnabled(boolean projectSearchEnabled) {
        this.projectSearchEnabled = projectSearchEnabled;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public void setProjectRoot(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    public int getProjectMaxFiles() {
        return projectMaxFiles;
    }

    public void setProjectMaxFiles(int projectMaxFiles) {
        this.projectMaxFiles = projectMaxFiles;
    }

    public int getProjectMaxFileBytes() {
        return projectMaxFileBytes;
    }

    public void setProjectMaxFileBytes(int projectMaxFileBytes) {
        this.projectMaxFileBytes = projectMaxFileBytes;
    }

    public int getProjectMaxSnippets() {
        return projectMaxSnippets;
    }

    public void setProjectMaxSnippets(int projectMaxSnippets) {
        this.projectMaxSnippets = projectMaxSnippets;
    }
}
