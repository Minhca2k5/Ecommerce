package com.minzetsu.ecommerce.chatbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "chatbot")
public class ChatbotProperties {
    private boolean enabled;
    private String provider = "openai-compatible";
    private String baseUrl;
    private String apiKey;
    private String model;
    private double temperature = 0.1;
    private int maxTokens = 80;
    private int numPredict = 80;
    private long cacheTtlMs = 60000;
    private boolean projectSearchEnabled = true;
    private String projectRoot;
    private int projectMaxFiles = 600;
    private int projectMaxFileBytes = 200_000;
    private int projectMaxSnippets = 6;
    private boolean queryPlannerEnabled = false;
    private String visionModel = "llava";
    private String defaultVoiceLang = "en";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
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

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public int getNumPredict() {
        return numPredict;
    }

    public void setNumPredict(int numPredict) {
        this.numPredict = numPredict;
    }

    public long getCacheTtlMs() {
        return cacheTtlMs;
    }

    public void setCacheTtlMs(long cacheTtlMs) {
        this.cacheTtlMs = cacheTtlMs;
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

    public String getVisionModel() {
        return visionModel;
    }

    public void setVisionModel(String visionModel) {
        this.visionModel = visionModel;
    }

    public String getDefaultVoiceLang() {
        return defaultVoiceLang;
    }

    public void setDefaultVoiceLang(String defaultVoiceLang) {
        this.defaultVoiceLang = defaultVoiceLang;
    }

    public boolean isQueryPlannerEnabled() {
        return queryPlannerEnabled;
    }

    public void setQueryPlannerEnabled(boolean queryPlannerEnabled) {
        this.queryPlannerEnabled = queryPlannerEnabled;
    }
}




