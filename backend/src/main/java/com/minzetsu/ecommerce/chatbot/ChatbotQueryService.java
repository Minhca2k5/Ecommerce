
package com.minzetsu.ecommerce.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ChatbotQueryService {
    private static final Logger log = LoggerFactory.getLogger(ChatbotQueryService.class);
    private static final Set<String> AGG_FUNCS = Set.of("count", "avg", "sum", "min", "max");
    private static final Set<String> OPS = Set.of("=", "!=", ">", ">=", "<", "<=", "like", "in");
    private static final Pattern SAFE_ALIAS = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private final ChatbotProperties properties;
    private final ChatbotSchemaProvider schemaProvider;
    private final RestTemplate restTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<JoinRule> JOIN_RULES = List.of(
            new JoinRule("products", "categories", "category_id", "id"),
            new JoinRule("products", "reviews", "id", "product_id"),
            new JoinRule("products", "product_images", "id", "product_id"),
            new JoinRule("order_items", "orders", "order_id", "id"),
            new JoinRule("order_items", "products", "product_id", "id"),
            new JoinRule("payments", "orders", "order_id", "id"),
            new JoinRule("orders", "users", "user_id", "id"),
            new JoinRule("orders", "vouchers", "voucher_id", "id"),
            new JoinRule("voucher_uses", "vouchers", "voucher_id", "id"),
            new JoinRule("voucher_uses", "orders", "order_id", "id"),
            new JoinRule("voucher_uses", "users", "user_id", "id"),
            new JoinRule("cart_items", "carts", "cart_id", "id"),
            new JoinRule("cart_items", "products", "product_id", "id"),
            new JoinRule("carts", "users", "user_id", "id"),
            new JoinRule("reviews", "users", "user_id", "id"),
            new JoinRule("wishlists", "users", "user_id", "id"),
            new JoinRule("wishlists", "products", "product_id", "id"),
            new JoinRule("inventory", "products", "product_id", "id"),
            new JoinRule("inventory", "warehouses", "warehouse_id", "id"),
            new JoinRule("notifications", "users", "user_id", "id")
    );

    public ChatbotQueryService(
            ChatbotProperties properties,
            ChatbotSchemaProvider schemaProvider,
            RestTemplate restTemplate,
            JdbcTemplate jdbcTemplate
    ) {
        this.properties = properties;
        this.schemaProvider = schemaProvider;
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    public String answerWithDb(String question) {
        QueryPlan plan = planQuery(question);
        if (plan == null) {
            return null;
        }
        return executePlan(plan);
    }

    private QueryPlan planQuery(String question) {
        try {
            if (!properties.isEnabled() || properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
                return null;
            }
            Map<String, Set<String>> schema = normalizeSchema(schemaProvider.getSchema());
            if (schema.isEmpty()) {
                return null;
            }
            String schemaSummary = schema.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "(" + String.join(",", entry.getValue()) + ")")
                    .collect(Collectors.joining("; "));

            String joinSummary = JOIN_RULES.stream()
                    .map(rule -> rule.leftTable + "." + rule.leftColumn + " = " + rule.rightTable + "." + rule.rightColumn)
                    .collect(Collectors.joining("; "));

            String system = "You are a query planner for an ecommerce database. Convert the user question into a SAFE JSON plan for SELECT only. "
                    + "If the question is not answerable using the schema, return {}. Use table.column in select/filters/groupBy. "
                    + "JSON format: {\"from\":\"table\",\"select\":[{\"column\":\"table.col\",\"alias\":\"alias\"},{\"aggregate\":\"avg\",\"column\":\"table.col\",\"alias\":\"avg_rating\"}],"
                    + "\"joins\":[{\"table\":\"other\",\"type\":\"left\",\"on\":[{\"left\":\"table.col\",\"right\":\"other.col\"}]}],"
                    + "\"filters\":[{\"column\":\"table.col\",\"op\":\">=\",\"value\":10}],"
                    + "\"groupBy\":[\"table.col\"],\"orderBy\":[{\"column\":\"alias_or_table.col\",\"dir\":\"DESC\"}],\"limit\":10}. "
                    + "Allowed join rules: " + joinSummary + ". Schema: " + schemaSummary;

            int basePredict = properties.getNumPredict();
            String raw = callPlanner(system, question, basePredict);
            if (raw == null) {
                return null;
            }
            String json = extractJson(raw);
            if (json == null || json.isBlank()) {
                return null;
            }
            try {
                JsonNode node = objectMapper.readTree(json);
                return QueryPlan.fromJson(node, schema);
            } catch (Exception parseEx) {
                log.warn("Chatbot planner JSON parse failed, retrying once: {}", parseEx.getMessage());
                int retryPredict = basePredict + 64;
                String retryRaw = callPlanner(system + " Return only valid JSON with all brackets closed.", question, retryPredict);
                if (retryRaw == null) {
                    return null;
                }
                String retryJson = extractJson(retryRaw);
                if (retryJson == null || retryJson.isBlank()) {
                    return null;
                }
                JsonNode node = objectMapper.readTree(retryJson);
                return QueryPlan.fromJson(node, schema);
            }
        } catch (Exception ex) {
            log.warn("Chatbot query planning failed: {}", ex.getMessage());
            return null;
        }
    }

    private String callPlanner(String system, String question, int numPredict) {
        Map<String, Object> payload = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", question)
                ),
                "temperature", 0,
                "format", "json",
                "stream", false,
                "options", Map.of("num_predict", numPredict)
        );

        Map<?, ?> response = restTemplate.postForObject(
                properties.getBaseUrl() + "/api/chat",
                payload,
                Map.class
        );
        return extractContent(response);
    }

    private String executePlan(QueryPlan plan) {
        try {
            StringBuilder sql = new StringBuilder("SELECT ");
            sql.append(plan.selectSql()).append(" FROM ").append(plan.fromTable);

            for (Join join : plan.joins) {
                sql.append(" ")
                        .append(join.type)
                        .append(" JOIN ")
                        .append(join.table)
                        .append(" ON ")
                        .append(join.left)
                        .append(" = ")
                        .append(join.right);
            }

            List<Object> params = new ArrayList<>();
            if (!plan.filters.isEmpty()) {
                sql.append(" WHERE ");
                List<String> parts = new ArrayList<>();
                for (Filter f : plan.filters) {
                    if ("IN".equals(f.op)) {
                        if (f.values == null || f.values.isEmpty()) {
                            parts.add("1=0");
                            continue;
                        }
                        String placeholders = f.values.stream().map(v -> "?").collect(Collectors.joining(", "));
                        parts.add(f.column + " IN (" + placeholders + ")");
                        params.addAll(f.values);
                        continue;
                    }
                    if (f.value == null) {
                        if ("=".equals(f.op)) {
                            parts.add(f.column + " IS NULL");
                        } else if ("!=".equals(f.op)) {
                            parts.add(f.column + " IS NOT NULL");
                        }
                        continue;
                    }
                    Object value = f.value;
                    if ("LIKE".equals(f.op) && value instanceof String s && !s.contains("%")) {
                        value = "%" + s + "%";
                    }
                    parts.add(f.column + " " + f.op + " ?");
                    params.add(value);
                }
                sql.append(String.join(" AND ", parts));
            }

            if (!plan.groupBy.isEmpty()) {
                sql.append(" GROUP BY ").append(String.join(", ", plan.groupBy));
            }

            if (!plan.orderBy.isEmpty()) {
                sql.append(" ORDER BY ");
                List<String> parts = new ArrayList<>();
                for (OrderBy ob : plan.orderBy) {
                    parts.add(ob.column + " " + ob.direction);
                }
                sql.append(String.join(", ", parts));
            }

            sql.append(" LIMIT ").append(plan.limit);

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            if (rows.isEmpty()) {
                return "Khong tim thay du lieu phu hop.";
            }
            StringBuilder sb = new StringBuilder();
            int idx = 1;
            for (Map<String, Object> row : rows) {
                sb.append(idx++).append(". ");
                sb.append(formatRow(row, plan));
                sb.append("\n");
                if (idx > plan.limit) {
                    break;
                }
            }
            return sb.toString().trim();
        } catch (Exception ex) {
            log.warn("Chatbot query execution failed: {}", ex.getMessage());
            return null;
        }
    }

    private String formatRow(Map<String, Object> row, QueryPlan plan) {
        List<String> keys = plan.outputColumns;
        String productNameKey = pickKey(keys, "name", "product_name");
        String priceKey = pickKey(keys, "price", "unit_price_snapshot");
        String currencyKey = pickKey(keys, "currency");
        if (productNameKey != null && priceKey != null) {
            Object name = readValue(row, productNameKey);
            Object price = readValue(row, priceKey);
            Object currency = currencyKey == null ? null : readValue(row, currencyKey);
            if (name != null && price != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(name);
                sb.append(" - ").append(price);
                if (currency != null) {
                    sb.append(" ").append(currency);
                }
                return sb.toString();
            }
        }
        if (keys.size() == 1) {
            Object value = readValue(row, keys.get(0));
            return value == null ? "null" : value.toString();
        }
        List<String> parts = new ArrayList<>();
        for (String key : keys) {
            Object value = readValue(row, key);
            parts.add(key + ": " + (value == null ? "null" : value.toString()));
        }
        return String.join(", ", parts);
    }

    private Object readValue(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String pickKey(List<String> keys, String... candidates) {
        for (String candidate : candidates) {
            for (String key : keys) {
                if (key.equalsIgnoreCase(candidate)) {
                    return key;
                }
            }
        }
        return null;
    }

    private Map<String, Set<String>> normalizeSchema(Map<String, Set<String>> schema) {
        Map<String, Set<String>> normalized = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : schema.entrySet()) {
            String table = entry.getKey().toLowerCase(Locale.ROOT);
            Set<String> cols = entry.getValue().stream()
                    .map(col -> col.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            normalized.put(table, cols);
        }
        return normalized;
    }

    private String extractContent(Map<?, ?> response) {
        if (response == null) {
            return null;
        }
        Object message = response.get("message");
        if (message instanceof Map<?, ?> msgMap) {
            Object content = msgMap.get("content");
            return content == null ? null : content.toString();
        }
        Object choices = response.get("choices");
        if (choices instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> choice) {
                Object msg = choice.get("message");
                if (msg instanceof Map<?, ?> msgMap) {
                    Object content = msgMap.get("content");
                    return content == null ? null : content.toString();
                }
            }
        }
        Object direct = response.get("response");
        return direct == null ? null : direct.toString();
    }

    private String extractJson(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed;
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return null;
    }

    private static class QueryPlan {
        private final String fromTable;
        private final List<SelectItem> selectItems;
        private final List<Join> joins;
        private final List<Filter> filters;
        private final List<String> groupBy;
        private final List<OrderBy> orderBy;
        private final int limit;
        private final List<String> outputColumns;

        private QueryPlan(
                String fromTable,
                List<SelectItem> selectItems,
                List<Join> joins,
                List<Filter> filters,
                List<String> groupBy,
                List<OrderBy> orderBy,
                int limit,
                List<String> outputColumns
        ) {
            this.fromTable = fromTable;
            this.selectItems = selectItems;
            this.joins = joins;
            this.filters = filters;
            this.groupBy = groupBy;
            this.orderBy = orderBy;
            this.limit = limit;
            this.outputColumns = outputColumns;
        }

        private String selectSql() {
            List<String> parts = new ArrayList<>();
            for (SelectItem item : selectItems) {
                if (item.outputKey != null && !item.outputKey.equals(item.sql)) {
                    parts.add(item.sql + " AS " + item.outputKey);
                } else {
                    parts.add(item.sql);
                }
            }
            return String.join(", ", parts);
        }

        private static QueryPlan fromJson(JsonNode node, Map<String, Set<String>> schema) {
            if (node == null || !node.isObject() || node.size() == 0) {
                return null;
            }
            String from = readText(node, "from", "table");
            if (from == null) {
                return null;
            }
            from = from.toLowerCase(Locale.ROOT);
            if (!schema.containsKey(from)) {
                return null;
            }

            Set<String> tables = new HashSet<>();
            tables.add(from);

            List<Join> joins = parseJoins(node.get("joins"), tables, schema);
            if (joins == null) {
                return null;
            }

            List<SelectItem> selectItems = parseSelect(node.get("select"), from, tables, schema);
            if (selectItems == null || selectItems.isEmpty()) {
                selectItems = defaultSelect(from, schema.get(from));
            }

            List<Filter> filters = parseFilters(node.get("filters"), tables, schema);
            if (filters == null) {
                filters = List.of();
            }

            List<String> groupBy = parseGroupBy(node.get("groupBy"), tables, schema);
            boolean hasAggregate = selectItems.stream().anyMatch(item -> item.aggregate);
            if ((groupBy == null || groupBy.isEmpty()) && hasAggregate) {
                groupBy = selectItems.stream()
                        .filter(item -> !item.aggregate)
                        .map(item -> item.sql)
                        .collect(Collectors.toList());
            }
            if (groupBy == null) {
                groupBy = List.of();
            }

            List<String> outputColumns = selectItems.stream()
                    .map(item -> item.outputKey)
                    .collect(Collectors.toList());

            List<OrderBy> orderBy = parseOrderBy(node.get("orderBy"), tables, schema, outputColumns);
            if (orderBy == null) {
                orderBy = List.of();
            }

            int limit = node.path("limit").asInt(5);
            if (limit <= 0) {
                limit = 5;
            }
            limit = Math.min(limit, 20);

            return new QueryPlan(from, selectItems, joins, filters, groupBy, orderBy, limit, outputColumns);
        }

        private static String readText(JsonNode node, String... keys) {
            for (String key : keys) {
                if (node.hasNonNull(key)) {
                    String value = node.get(key).asText();
                    if (value != null && !value.isBlank()) {
                        return value.trim();
                    }
                }
            }
            return null;
        }

        private static List<SelectItem> defaultSelect(String from, Set<String> cols) {
            List<SelectItem> defaults = new ArrayList<>();
            if ("products".equals(from)) {
                addDefault(defaults, from, cols, "name", "price", "currency");
            } else if ("categories".equals(from)) {
                addDefault(defaults, from, cols, "name", "slug");
            } else if ("vouchers".equals(from)) {
                addDefault(defaults, from, cols, "code", "discount_value", "discount_type");
            } else {
                addDefault(defaults, from, cols, "id");
            }
            if (defaults.isEmpty() && !cols.isEmpty()) {
                String fallback = cols.iterator().next();
                defaults.add(new SelectItem(from + "." + fallback, fallback, false));
            }
            return defaults;
        }

        private static void addDefault(List<SelectItem> defaults, String from, Set<String> cols, String... names) {
            for (String name : names) {
                if (cols.contains(name)) {
                    defaults.add(new SelectItem(from + "." + name, name, false));
                }
            }
        }

        private static List<SelectItem> parseSelect(JsonNode node, String from, Set<String> tables, Map<String, Set<String>> schema) {
            if (node == null || !node.isArray()) {
                return List.of();
            }
            List<SelectItem> items = new ArrayList<>();
            for (JsonNode item : node) {
                SelectItem parsed = parseSelectItem(item, from, tables, schema);
                if (parsed != null) {
                    items.add(parsed);
                }
            }
            return items;
        }

        private static SelectItem parseSelectItem(JsonNode item, String from, Set<String> tables, Map<String, Set<String>> schema) {
            if (item == null) {
                return null;
            }
            if (item.isTextual()) {
                String column = resolveColumnRef(item.asText(), from, tables, schema);
                if (column == null) {
                    return null;
                }
                String key = column.substring(column.indexOf('.') + 1);
                return new SelectItem(column, key, false);
            }
            if (!item.isObject()) {
                return null;
            }
            if (item.hasNonNull("aggregate")) {
                String func = item.get("aggregate").asText("").toLowerCase(Locale.ROOT);
                if (!AGG_FUNCS.contains(func)) {
                    return null;
                }
                String column = item.hasNonNull("column") ? item.get("column").asText() : "";
                String sql;
                if ("count".equals(func) && (column.isBlank() || "*".equals(column))) {
                    sql = "count(*)";
                } else {
                    String resolved = resolveColumnRef(column, from, tables, schema);
                    if (resolved == null) {
                        return null;
                    }
                    sql = func + "(" + resolved + ")";
                }
                String alias = safeAlias(item.path("alias").asText(null));
                if (alias == null || alias.isBlank()) {
                    alias = func + "_value";
                }
                return new SelectItem(sql, alias, true);
            }
            if (item.hasNonNull("column")) {
                String column = resolveColumnRef(item.get("column").asText(), from, tables, schema);
                if (column == null) {
                    return null;
                }
                String alias = safeAlias(item.path("alias").asText(null));
                String key = alias == null ? column.substring(column.indexOf('.') + 1) : alias;
                return new SelectItem(column, key, false);
            }
            return null;
        }

        private static List<Join> parseJoins(JsonNode node, Set<String> tables, Map<String, Set<String>> schema) {
            if (node == null || node.isNull()) {
                return new ArrayList<>();
            }
            if (!node.isArray()) {
                return null;
            }
            List<Join> joins = new ArrayList<>();
            for (JsonNode joinNode : node) {
                if (!joinNode.isObject()) {
                    continue;
                }
                String table = joinNode.path("table").asText(null);
                if (table == null || table.isBlank()) {
                    continue;
                }
                table = table.toLowerCase(Locale.ROOT);
                if (!schema.containsKey(table) || tables.contains(table)) {
                    continue;
                }
                String type = joinNode.path("type").asText("left").toLowerCase(Locale.ROOT);
                type = "inner".equals(type) ? "INNER" : "LEFT";

                Join join = resolveJoin(joinNode.get("on"), table, tables, schema);
                if (join == null) {
                    continue;
                }
                joins.add(new Join(table, type, join.left, join.right));
                tables.add(table);
            }
            return joins;
        }

        private static Join resolveJoin(JsonNode onNode, String joinTable, Set<String> tables, Map<String, Set<String>> schema) {
            if (onNode == null || !onNode.isArray()) {
                return null;
            }
            for (JsonNode on : onNode) {
                String left = on.path("left").asText(null);
                String right = on.path("right").asText(null);
                if (left == null || right == null) {
                    continue;
                }
                String leftResolved = resolveColumnRef(left, null, tables, schema);
                String rightResolved = resolveColumnRef(right, null, tables, schema);
                if (leftResolved == null || rightResolved == null) {
                    continue;
                }
                String leftTable = leftResolved.substring(0, leftResolved.indexOf('.'));
                String rightTable = rightResolved.substring(0, rightResolved.indexOf('.'));
                if (!tables.contains(leftTable) && !tables.contains(rightTable)) {
                    continue;
                }
                if (!joinTable.equals(leftTable) && !joinTable.equals(rightTable)) {
                    continue;
                }
                if (!isAllowedJoin(leftResolved, rightResolved)) {
                    continue;
                }
                return new Join(joinTable, "LEFT", leftResolved, rightResolved);
            }
            return null;
        }

        private static boolean isAllowedJoin(String left, String right) {
            String leftTable = left.substring(0, left.indexOf('.'));
            String leftCol = left.substring(left.indexOf('.') + 1);
            String rightTable = right.substring(0, right.indexOf('.'));
            String rightCol = right.substring(right.indexOf('.') + 1);
            for (JoinRule rule : JOIN_RULES) {
                if (rule.matches(leftTable, leftCol, rightTable, rightCol)) {
                    return true;
                }
            }
            return false;
        }

        private static List<Filter> parseFilters(JsonNode node, Set<String> tables, Map<String, Set<String>> schema) {
            if (node == null || !node.isArray()) {
                return List.of();
            }
            List<Filter> filters = new ArrayList<>();
            for (JsonNode f : node) {
                if (!f.isObject()) {
                    continue;
                }
                String column = resolveColumnRef(f.path("column").asText(null), null, tables, schema);
                if (column == null) {
                    continue;
                }
                String op = f.path("op").asText("=").toLowerCase(Locale.ROOT);
                if (!OPS.contains(op)) {
                    continue;
                }
                if ("in".equals(op)) {
                    List<Object> values = new ArrayList<>();
                    if (f.has("value")) {
                        JsonNode valueNode = f.get("value");
                        if (valueNode.isArray()) {
                            for (JsonNode v : valueNode) {
                                values.add(readValueNode(v));
                            }
                        } else {
                            values.add(readValueNode(valueNode));
                        }
                    }
                    filters.add(new Filter(column, "IN", values, null));
                    continue;
                }
                Object value = f.has("value") ? readValueNode(f.get("value")) : null;
                String normalizedOp = op.equals("like") ? "LIKE" : op.toUpperCase(Locale.ROOT);
                filters.add(new Filter(column, normalizedOp, null, value));
            }
            return filters;
        }

        private static List<String> parseGroupBy(JsonNode node, Set<String> tables, Map<String, Set<String>> schema) {
            if (node == null || !node.isArray()) {
                return List.of();
            }
            List<String> columns = new ArrayList<>();
            for (JsonNode c : node) {
                String column = resolveColumnRef(c.asText(null), null, tables, schema);
                if (column != null) {
                    columns.add(column);
                }
            }
            return columns;
        }

        private static List<OrderBy> parseOrderBy(JsonNode node, Set<String> tables, Map<String, Set<String>> schema, List<String> outputColumns) {
            if (node == null || !node.isArray()) {
                return List.of();
            }
            List<OrderBy> orderBy = new ArrayList<>();
            for (JsonNode o : node) {
                if (!o.isObject()) {
                    continue;
                }
                String column = o.path("column").asText(null);
                if (column == null || column.isBlank()) {
                    continue;
                }
                String dir = o.path("dir").asText("asc").toUpperCase(Locale.ROOT);
                if (!"ASC".equals(dir) && !"DESC".equals(dir)) {
                    dir = "ASC";
                }
                String resolved;
                if (outputColumns.stream().anyMatch(col -> col.equalsIgnoreCase(column))) {
                    resolved = column;
                } else {
                    resolved = resolveColumnRef(column, null, tables, schema);
                }
                if (resolved != null) {
                    orderBy.add(new OrderBy(resolved, dir));
                }
            }
            return orderBy;
        }

        private static String resolveColumnRef(String ref, String from, Set<String> tables, Map<String, Set<String>> schema) {
            if (ref == null || ref.isBlank()) {
                return null;
            }
            String trimmed = ref.trim();
            if (trimmed.contains(".")) {
                String[] parts = trimmed.split("\\.", 2);
                String table = parts[0].toLowerCase(Locale.ROOT);
                String column = parts[1].toLowerCase(Locale.ROOT);
                if (!schema.containsKey(table)) {
                    return null;
                }
                if (!schema.get(table).contains(column)) {
                    return null;
                }
                return table + "." + column;
            }
            String column = trimmed.toLowerCase(Locale.ROOT);
            if (from != null && schema.containsKey(from) && schema.get(from).contains(column)) {
                return from + "." + column;
            }
            String matchedTable = null;
            for (String table : tables) {
                if (schema.containsKey(table) && schema.get(table).contains(column)) {
                    if (matchedTable != null) {
                        return null;
                    }
                    matchedTable = table;
                }
            }
            return matchedTable == null ? null : matchedTable + "." + column;
        }

        private static Object readValueNode(JsonNode node) {
            if (node == null || node.isNull()) {
                return null;
            }
            if (node.isNumber()) {
                return node.numberValue();
            }
            if (node.isBoolean()) {
                return node.asBoolean();
            }
            return node.asText();
        }

        private static String safeAlias(String alias) {
            if (alias == null) {
                return null;
            }
            String trimmed = alias.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            return SAFE_ALIAS.matcher(trimmed).matches() ? trimmed : null;
        }
    }

    private record JoinRule(String leftTable, String rightTable, String leftColumn, String rightColumn) {
        private boolean matches(String leftTable, String leftColumn, String rightTable, String rightColumn) {
            if (this.leftTable.equals(leftTable) && this.rightTable.equals(rightTable)
                    && this.leftColumn.equals(leftColumn) && this.rightColumn.equals(rightColumn)) {
                return true;
            }
            return this.leftTable.equals(rightTable) && this.rightTable.equals(leftTable)
                    && this.leftColumn.equals(rightColumn) && this.rightColumn.equals(leftColumn);
        }
    }

    private static class SelectItem {
        private final String sql;
        private final String outputKey;
        private final boolean aggregate;

        private SelectItem(String sql, String outputKey, boolean aggregate) {
            this.sql = sql;
            this.outputKey = outputKey;
            this.aggregate = aggregate;
        }
    }

    private static class Join {
        private final String table;
        private final String type;
        private final String left;
        private final String right;

        private Join(String table, String type, String left, String right) {
            this.table = table;
            this.type = type;
            this.left = left;
            this.right = right;
        }
    }

    private static class Filter {
        private final String column;
        private final String op;
        private final List<Object> values;
        private final Object value;

        private Filter(String column, String op, List<Object> values, Object value) {
            this.column = column;
            this.op = op;
            this.values = values;
            this.value = value;
        }
    }

    private record OrderBy(String column, String direction) {
    }
}
