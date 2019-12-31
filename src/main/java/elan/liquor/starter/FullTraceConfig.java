package elan.liquor.starter;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = HttpTraceConfig.CONFIG_PREFIX, value = "enabled")
public class FullTraceConfig {

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletTraceFilterConfiguration {
        @Bean
        public HttpFullTrace httpTraceLogFilter(@Autowired LogCollector logCollector, MeterRegistry registry) {
            return new HttpFullTrace(logCollector, registry);
        }
    }
}
