package elan.liquor.starter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogProperties {
    public static final String CONFIG_PREFIX = "liquor.http-full-trace";

    /** 服务名称 */
    private static String applicationName;
    private static String customTag;
    /** 不过滤的uri */
    private static List<String> excludeUri;
    /** 需要过滤的uri */
    private static List<String> includeUri;

    @Value("${spring.application.name:}")
    public void setApplicationName(String applicationName) {
        LogProperties.applicationName = applicationName;
    }

    @Value("${"+CONFIG_PREFIX+".customTag}")
    public void setCustomTag(String customTag) {
        if (StringUtils.isBlank(customTag)) {
            customTag = applicationName;
        }
        LogProperties.customTag = customTag;
    }

    @Value("${"+CONFIG_PREFIX+".excludeUri}")
    public void setExcludeUri(List<String> excludeUri) {
        LogProperties.excludeUri = excludeUri;
    }

    @Value("${"+CONFIG_PREFIX+".includeUri}")
    public void setIncludeUri(List<String> includeUri) {
        LogProperties.includeUri = includeUri;
    }

    public static String getApplicationName() {
        return applicationName;
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
}
