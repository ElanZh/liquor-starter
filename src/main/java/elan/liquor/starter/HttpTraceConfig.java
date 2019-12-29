package elan.liquor.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "management.trace.http", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(HttpTraceProperties.class)
@AutoConfigureBefore(HttpTraceAutoConfiguration.class)
public class HttpTraceConfig {

    @Bean
    @ConditionalOnMissingBean(HttpTraceRepository.class)
    public SimpleTrace traceRepository() {
        return new SimpleTrace();
    }

    @Bean
    @ConditionalOnMissingBean(LogCollector.class)
    public LogCollector logCollector() {
        return fullLog ->
        {
            try {
                log.debug("http-full-trace | " + new ObjectMapper().writeValueAsString(fullLog));
            } catch (JsonProcessingException e) {
                log.error("jackson解析日志条目异常：" + e.getMessage());
            }
        };
    }
}
