package elan.liquor.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceProperties;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "management.trace.http", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(HttpTraceProperties.class)
@AutoConfigureBefore(HttpTraceAutoConfiguration.class)
public class HttpTraceConfig {
    public static final String CONFIG_PREFIX = "liquor.http-full-trace";

    @Bean
    @ConditionalOnMissingBean(HttpTraceRepository.class)
    public SimpleTrace traceRepository() {
        return new SimpleTrace();
    }

    @Bean
    @ConditionalOnMissingBean(LogCollector.class)
    public LogCollector logCollector() {
        return (fullLog, request, response) ->
        {
            try {
                log.debug("http-full-trace | " + new ObjectMapper().writeValueAsString(fullLog));
            } catch (JsonProcessingException e) {
                log.error("jackson解析日志条目异常：" + e.getMessage());
            }
        };
    }

    /**
     * 自定义tag
     */
    private static String customTag;
    /**
     * 不过滤的uri
     */
    private static List<Pattern> excludeUri;
    /**
     * 需要过滤的uri
     */
    private static List<Pattern> includeUri;

    @Value("${" + CONFIG_PREFIX + ".customTag:${spring.application.name}}")
    public void setCustomTag(String customTag) {
        log.info("http-full-trace config | customTag = " + customTag);
        HttpTraceConfig.customTag = customTag;
    }

    @Value("${" + CONFIG_PREFIX + ".excludeUri:}")
    public void setExcludeUri(List<String> excludeUri) {
        if (!CollectionUtils.isEmpty(excludeUri)) {
            log.info("http-full-trace config | excludeUri = " + StringUtils.join(excludeUri, ","));
            List<Pattern> match = new ArrayList<>(excludeUri.size());
            excludeUri.forEach(str -> match.add(Pattern.compile(str)));
            HttpTraceConfig.excludeUri = match;
        }
    }

    @Value("${" + CONFIG_PREFIX + ".includeUri:}")
    public void setIncludeUri(List<String> includeUri) {
        if (!CollectionUtils.isEmpty(includeUri)) {
            log.info("http-full-trace config | includeUri = " + StringUtils.join(includeUri, ","));
            List<Pattern> match = new ArrayList<>(includeUri.size());
            includeUri.forEach(str -> match.add(Pattern.compile(str)));
            HttpTraceConfig.includeUri = match;
        }
    }

    public static String getCustomTag() {
        return customTag;
    }

    public static List<Pattern> getExcludeUri() {
        return excludeUri;
    }

    public static List<Pattern> getIncludeUri() {
        return includeUri;
    }
}
