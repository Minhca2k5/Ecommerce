package com.minzetsu.ecommerce.chatbot.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChatbotSchemaProvider {
    private static final Pattern CREATE_TABLE = Pattern.compile("createTable\\s+tableName=\\\"([^\\\"]+)\\\"");
    private static final Pattern COLUMN = Pattern.compile("column\\s+name=\\\"([^\\\"]+)\\\"");
    private final Map<String, Set<String>> tableColumns = new HashMap<>();

    public ChatbotSchemaProvider() {
        loadFromLiquibase();
    }

    public Map<String, Set<String>> getSchema() {
        Map<String, Set<String>> copy = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : tableColumns.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    private void loadFromLiquibase() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:db/changelog/*.xml");
            for (Resource resource : resources) {
                parseLiquibaseFile(resource);
            }
        } catch (Exception ignored) {
            // Best-effort schema loading; fallback handled by query planner.
        }
    }

    private void parseLiquibaseFile(Resource resource) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            String currentTable = null;
            while ((line = reader.readLine()) != null) {
                Matcher createMatcher = CREATE_TABLE.matcher(line);
                if (createMatcher.find()) {
                    currentTable = createMatcher.group(1);
                    tableColumns.computeIfAbsent(currentTable, k -> new HashSet<>());
                }
                Matcher columnMatcher = COLUMN.matcher(line);
                if (currentTable != null && columnMatcher.find()) {
                    String column = columnMatcher.group(1);
                    if (isSensitive(column)) {
                        continue;
                    }
                    tableColumns.get(currentTable).add(column);
                }
                if (line.contains("</createTable>")) {
                    currentTable = null;
                }
            }
        } catch (Exception ignored) {
            // Best-effort parsing.
        }
    }

    private boolean isSensitive(String column) {
        String lower = column.toLowerCase(Locale.ROOT);
        return lower.contains("password")
                || lower.contains("secret")
                || lower.contains("token")
                || lower.contains("salt")
                || lower.contains("apikey");
    }
}




