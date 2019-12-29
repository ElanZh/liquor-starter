package elan.liquor.starter;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication
public class FullTraceConfig {

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletTraceFilterConfiguration {

        @Bean
        public HttpFullTrace httpTraceLogFilter(@Autowired LogCollector logCollector, MeterRegistry registry) {
            return new HttpFullTrace(logCollector, registry);
        }

    }
}
