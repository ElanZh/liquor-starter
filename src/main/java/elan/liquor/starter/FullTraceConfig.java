package elan.liquor.starter;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = FullTraceConfig.CONFIG_PREFIX, value = "enabled")
@ConfigurationProperties(prefix = FullTraceConfig.CONFIG_PREFIX)
public class FullTraceConfig {
    public static final String CONFIG_PREFIX = "liquor.http-full-trace";

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletTraceFilterConfiguration {
        @Bean
        public HttpFullTrace httpTraceLogFilter(@Autowired LogCollector logCollector, MeterRegistry registry) {
            return new HttpFullTrace(logCollector, registry);
        }
    }

    @Bean
    @ConditionalOnMissingBean(LogCollector.class)
    public LogCollector logCollector() {
        return (fullLog, request, response) ->
                log.debug("default http-full-trace implement: " +
                        "\n************************" +
                        "\ncustomTag=" + fullLog.getCustomTag() +
                        "\nrequestId=" + fullLog.getRequestId() +
                        "\npath=" + fullLog.getPath() +
                        "\nparameterMap=" + fullLog.getParameterMap() +
                        "\nmethod=" + fullLog.getMethod() +
                        "\ntimeTaken=" + fullLog.getTimeTaken() +
                        "\ntime=" + fullLog.getTime() +
                        "\nstatus=" + fullLog.getStatus() +
                        "\nIP=" + fullLog.getIp() +
                        "\nrequestHeader=" + fullLog.getRequestHeader() +
                        "\nrequestBody=" + fullLog.getRequestBody() +
                        "\nresponseHeader=" + fullLog.getResponseHeader() +
                        "\nresponseBody=" + fullLog.getResponseBody() +
                        "\n************************"
                );
    }

    /**
     * 自定义tag
     */
    private static String customTag;
    /**
     * 不过滤的uri
     */
    private static List<String> excludeUri;
    /**
     * 需要过滤的uri
     */
    private static List<String> includeUri;

    @Value("${" + CONFIG_PREFIX + ".customTag:${spring.application.name}}")
    public void setCustomTag(String customTag) {
        log.info("http-full-trace-config | customTag = " + customTag);
        FullTraceConfig.customTag = customTag;
    }

    public void setExcludeUri(List<String> excludeUri) {
        if (!CollectionUtils.isEmpty(excludeUri)) {
            log.info("http-full-trace-config | excludeUri = " + StringUtils.join(excludeUri, ","));
            FullTraceConfig.excludeUri = excludeUri;
        }
    }

    public void setIncludeUri(List<String> includeUri) {
        if (!CollectionUtils.isEmpty(includeUri)) {
            log.info("http-full-trace-config | includeUri = " + StringUtils.join(includeUri, ","));
            FullTraceConfig.includeUri = includeUri;
        }
    }

    public static String getCustomTag() {
        return customTag;
    }

    public static List<String> getExcludeUri() {
        return excludeUri;
    }

    public static List<String> getIncludeUri() {
        return includeUri;
    }

    private static final AntPathMatcher MATCHES = new AntPathMatcher();

    /**
     * 传入路径是否配置了不包含（不会判断配置是否为空）
     *
     * @param path 路径
     * @return true=配置了不包含
     */
    public static boolean checkExclude(final String path) {
        for (String pattern : FullTraceConfig.excludeUri) {
            if (MATCHES.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 传入路径是否配置了包含（不会判断配置是否为空）
     *
     * @param path 路径
     * @return true=配置了包含
     */
    public static boolean checkInclude(final String path) {
        for (String pattern : FullTraceConfig.includeUri) {
            if (MATCHES.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
