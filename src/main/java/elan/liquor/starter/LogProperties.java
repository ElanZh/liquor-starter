package elan.liquor.starter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = LogProperties.ENV_PREFIX)
@lombok.Data
public class LogProperties {
    public static final String ENV_PREFIX = "liquor.http-full-trace";

    /** 服务名称 */
    @Value("${spring.application.name}")
    private String customTag;
    /** 不过滤的uri */
    private List<String> excludeUri;
    /** 需要过滤的uri */
    private List<String> includeUri;
}
