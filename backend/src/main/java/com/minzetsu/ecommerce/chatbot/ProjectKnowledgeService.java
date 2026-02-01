package com.minzetsu.ecommerce.chatbot;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class ProjectKnowledgeService {
    private static final Set<String> ALLOWED_EXT = Set.of(
            ".java", ".kt", ".md", ".txt", ".yml", ".yaml", ".json", ".properties",
            ".ts", ".tsx", ".js", ".jsx", ".sql"
    );
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "a", "an", "and", "or", "to", "of", "in", "on", "is", "are", "for",
            "with", "this", "that", "it", "as", "at", "by", "from", "be", "was", "were",
            "i", "you", "we", "they", "he", "she", "them", "us", "our", "your"
    );

    private final ChatbotProperties properties;

    public ProjectKnowledgeService(ChatbotProperties properties) {
        this.properties = properties;
    }

    public String buildContext(String query) {
        if (!properties.isProjectSearchEnabled()) {
            return "";
        }
        Set<String> tokens = tokenize(query);
        if (tokens.isEmpty()) {
            return "";
        }
        Path root = resolveRoot();
        if (root == null || !Files.exists(root)) {
            return "";
        }
        List<Snippet> snippets = search(root, tokens);
        if (snippets.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("Project context:\n");
        for (Snippet s : snippets) {
            sb.append("- ")
              .append(s.path)
              .append(":")
              .append(s.line)
              .append(" ")
              .append(s.text)
              .append("\n");
        }
        return sb.toString().trim();
    }

    private Path resolveRoot() {
        String configured = properties.getProjectRoot();
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured);
        }
        Path userDir = Paths.get(System.getProperty("user.dir"));
        Path parent = userDir.getParent();
        return parent != null ? parent : userDir;
    }

    private List<Snippet> search(Path root, Set<String> tokens) {
        List<Snippet> matches = new ArrayList<>();
        int maxFiles = Math.max(50, properties.getProjectMaxFiles());
        int maxSnippets = Math.max(1, properties.getProjectMaxSnippets());
        int maxBytes = Math.max(50_000, properties.getProjectMaxFileBytes());
        int[] visited = {0};

        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> isAllowedFile(p))
                    .filter(p -> !isIgnoredPath(p))
                    .limit(maxFiles)
                    .forEach(path -> {
                        if (visited[0] >= maxFiles || matches.size() >= maxSnippets) {
                            return;
                        }
                        visited[0]++;
                        try {
                            if (Files.size(path) > maxBytes) {
                                return;
                            }
                            Snippet snippet = findSnippet(root, path, tokens);
                            if (snippet != null) {
                                matches.add(snippet);
                            }
                        } catch (IOException ex) {
                            // ignore
                        }
                    });
        } catch (IOException ex) {
            return List.of();
        }

        matches.sort(Comparator.comparingInt((Snippet s) -> s.score).reversed());
        if (matches.size() > maxSnippets) {
            return matches.subList(0, maxSnippets);
        }
        return matches;
    }

    private Snippet findSnippet(Path root, Path path, Set<String> tokens) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String lower = line.toLowerCase();
                int score = 0;
                for (String token : tokens) {
                    if (lower.contains(token)) {
                        score++;
                    }
                }
                if (score > 0) {
                    String trimmed = line.trim();
                    if (trimmed.length() > 220) {
                        trimmed = trimmed.substring(0, 220) + "...";
                    }
                    String relative = root.relativize(path).toString().replace("\\", "/");
                    return new Snippet(relative, lineNo, trimmed, score);
                }
            }
        }
        return null;
    }

    private boolean isAllowedFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        for (String ext : ALLOWED_EXT) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private boolean isIgnoredPath(Path path) {
        String p = path.toString().replace("\\", "/").toLowerCase();
        return p.contains("/node_modules/")
                || p.contains("/dist/")
                || p.contains("/build/")
                || p.contains("/target/")
                || p.contains("/.git/")
                || p.contains("/.idea/")
                || p.contains("/.vscode/")
                || p.contains("/.gradle/")
                || p.contains("/.mvn/")
                || p.contains("/logs/");
    }

    private Set<String> tokenize(String query) {
        String lower = query == null ? "" : query.toLowerCase();
        String[] parts = lower.split("[^a-z0-9_\\-]+");
        Set<String> tokens = new HashSet<>();
        for (String p : parts) {
            if (p.length() < 3) {
                continue;
            }
            if (STOP_WORDS.contains(p)) {
                continue;
            }
            tokens.add(p);
            if (tokens.size() >= 8) {
                break;
            }
        }
        return tokens;
    }

    private static class Snippet {
        private final String path;
        private final int line;
        private final String text;
        private final int score;

        private Snippet(String path, int line, String text, int score) {
            this.path = path;
            this.line = line;
            this.text = text;
            this.score = score;
        }
    }
}
