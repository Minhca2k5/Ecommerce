package com.minzetsu.ecommerce.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Duration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        objectMapper.addMixIn(PageImpl.class, PageImplMixin.class);
        objectMapper.addMixIn(PageRequest.class, PageRequestMixin.class);
        objectMapper.addMixIn(Sort.class, SortMixin.class);
        objectMapper.addMixIn(Sort.Order.class, SortOrderMixin.class);

        SimpleModule pageModule = new SimpleModule();
        pageModule.addDeserializer(Sort.class, new SortDeserializer());
        pageModule.addDeserializer(PageRequest.class, new PageRequestDeserializer());
        objectMapper.registerModule(pageModule);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("home", defaultConfig.entryTtl(Duration.ofSeconds(60)));
        cacheConfigs.put("productDetail", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("categoryDetail", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("categoryTree", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("bannerPublic", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        cacheConfigs.put("voucherPublicV2", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        cacheConfigs.put("analyticsAdmin", defaultConfig.entryTtl(Duration.ofSeconds(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                logger.warn("Cache GET error on cache={} key={}: {}", cache != null ? cache.getName() : "unknown", key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                logger.warn("Cache PUT error on cache={} key={}: {}", cache != null ? cache.getName() : "unknown", key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                logger.warn("Cache EVICT error on cache={} key={}: {}", cache != null ? cache.getName() : "unknown", key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                logger.warn("Cache CLEAR error on cache={}: {}", cache != null ? cache.getName() : "unknown", exception.getMessage());
            }
        };
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class PageImplMixin<T> {
        @JsonCreator
        PageImplMixin(
                @JsonProperty("content") List<T> content,
                @JsonProperty("pageable") Pageable pageable,
                @JsonProperty("totalElements") long totalElements
        ) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class PageRequestMixin {
        @JsonCreator
        PageRequestMixin(
                @JsonProperty("pageNumber") int pageNumber,
                @JsonProperty("pageSize") int pageSize,
                @JsonProperty("sort") Sort sort
        ) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class SortMixin {
        @JsonCreator
        SortMixin(@JsonProperty("orders") List<Sort.Order> orders) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class SortOrderMixin {
        @JsonCreator
        SortOrderMixin(
                @JsonProperty("direction") Sort.Direction direction,
                @JsonProperty("property") String property,
                @JsonProperty("ignoreCase") boolean ignoreCase,
                @JsonProperty("nullHandling") Sort.NullHandling nullHandling
        ) {
        }
    }

    static class SortDeserializer extends JsonDeserializer<Sort> {
        @Override
        public Sort deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            JsonNode ordersNode = node != null ? node.get("orders") : null;
            if (ordersNode == null || !ordersNode.isArray() || ordersNode.isEmpty()) {
                return Sort.unsorted();
            }

            List<Sort.Order> orders = new ArrayList<>();
            for (JsonNode orderNode : ordersNode) {
                if (orderNode == null || orderNode.isNull()) {
                    continue;
                }
                String property = orderNode.path("property").asText(null);
                if (property == null || property.isBlank()) {
                    continue;
                }
                Sort.Direction direction = Sort.Direction.fromString(orderNode.path("direction").asText("ASC"));
                Sort.Order order = new Sort.Order(direction, property);
                if (orderNode.path("ignoreCase").asBoolean(false)) {
                    order = order.ignoreCase();
                }
                String nullHandlingValue = orderNode.path("nullHandling").asText(null);
                if (nullHandlingValue != null && !nullHandlingValue.isBlank()) {
                    try {
                        Sort.NullHandling nullHandling = Sort.NullHandling.valueOf(nullHandlingValue);
                        order = order.with(nullHandling);
                    } catch (IllegalArgumentException ignored) {
                        // ignore invalid null handling values
                    }
                }
                orders.add(order);
            }

            return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        }
    }

    static class PageRequestDeserializer extends JsonDeserializer<PageRequest> {
        @Override
        public PageRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            int pageNumber = node.path("pageNumber").asInt(0);
            int pageSize = node.path("pageSize").asInt(20);
            JsonNode sortNode = node.get("sort");
            Sort sort = sortNode != null && !sortNode.isNull()
                    ? p.getCodec().treeToValue(sortNode, Sort.class)
                    : Sort.unsorted();
            return PageRequest.of(pageNumber, pageSize, sort);
        }
    }
}



