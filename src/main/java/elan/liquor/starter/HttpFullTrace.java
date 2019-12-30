package elan.liquor.starter;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static elan.liquor.starter.DateUtil.DTF;

/**
 * 完整的http信息采集
 */
@Slf4j
public class HttpFullTrace extends OncePerRequestFilter implements Ordered {
    // 上传文件不采集
    private static final String IGNORE_CONTENT_TYPE = "multipart/form-data";
    private final LogCollector logCollector;
    private final MeterRegistry registry;

    public HttpFullTrace(LogCollector logCollector, MeterRegistry registry) {
        this.logCollector = logCollector;
        this.registry = registry;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    @Override
    protected void doFilterInternal(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, javax.servlet.FilterChain filterChain) throws ServletException, IOException {
        if (!isRequestValid(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        Instant startTime = Instant.now();
        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();
        } finally {
            String path = request.getRequestURI();
            if (!Objects.equals(IGNORE_CONTENT_TYPE, request.getContentType())) {
                FullLog fullLog = new FullLog();
                fullLog.setPath(path);
                fullLog.setMethod(request.getMethod());
                fullLog.setTimeTaken(Duration.between(startTime, Instant.now()).toMillis());
                fullLog.setTime(LocalDateTime.now().format(DTF));
                fullLog.setParameterMap(mapToString(request.getParameterMap()));
                fullLog.setStatus(status);
                fullLog.setRequestBody(getRequestBody(request));
                fullLog.setResponseBody(getResponseBody(response));
                logCollector.collect(fullLog, request, response);
            }
            updateResponse(response);
        }
    }

    /**
     * 校验url
     */
    private boolean isRequestValid(HttpServletRequest request) {
        try {
            new URI(request.getRequestURL().toString());
            return true;
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    private String getRequestBody(HttpServletRequest request) {
        String requestBody = "";
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            try {
                requestBody = IOUtils.toString(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding());
            } catch (IOException e) {
                log.error("请求体输出到日志异常：" + e.getMessage());
            }
        }
        return requestBody;
    }

    private String getResponseBody(HttpServletResponse response) {
        String responseBody = "";
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            try {
                responseBody = IOUtils.toString(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding());
            } catch (IOException e) {
                log.error("响应体输出到日志异常：" + e.getMessage());
            }
        }
        return responseBody;
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        Objects.requireNonNull(responseWrapper).copyBodyToResponse();
    }

    private String mapToString(Map<String, String[]> parameterMap) {
        if (CollectionUtils.isEmpty(parameterMap)) {
            return "";
        }
        StringBuilder result = new StringBuilder("");
        parameterMap.forEach((k, v) -> result.append("|").append(k).append("=").append(Arrays.toString(v)));
        result.deleteCharAt(0);
        return result.toString();
    }
}
