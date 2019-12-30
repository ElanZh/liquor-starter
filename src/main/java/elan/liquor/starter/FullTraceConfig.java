package elan.liquor.starter;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = LogProperties.ENV_PREFIX, value = "enabled")
public class FullTraceConfig {

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletTraceFilterConfiguration {
        @Bean
        public HttpFullTrace httpTraceLogFilter(@Autowired LogCollector logCollector, @Autowired LogProperties logProperties, MeterRegistry registry) {
            return new HttpFullTrace(logCollector, logProperties, registry);
        }
    }
}
