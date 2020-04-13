package elan.liquor.starter;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.MDC;
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
import java.util.*;

import static elan.liquor.starter.DateUtil.DTF;

/**
 * 完整的http信息采集
 */
@Slf4j
public class HttpFullTrace extends OncePerRequestFilter implements Ordered {
    public static final String REQUEST_ID = "requestId";
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
        // 校验路径 以及检查是否处理路径 上传文件不处理
        if ((!isRequestValid(request) && !needCollect(request.getRequestURI())) || IGNORE_CONTENT_TYPE.equals(request.getContentType())) {
            filterChain.doFilter(request, response);
            return;
        }
        // 此处进行包装，一边接下来inputstream可以多次读取
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        Instant startTime = Instant.now();
        String requestId = UUID.randomUUID().toString();
        try {
            MDC.put(REQUEST_ID, requestId);
            filterChain.doFilter(request, response);
            status = response.getStatus();
        } finally {
            ContentCachingRequestWrapper wrapperRequest = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
            ContentCachingResponseWrapper wrapperResponse = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
            // J2EE使用线程池处理请求，此处应清除MDC
            MDC.clear();
            FullLog fullLog = new FullLog();
            fullLog.setCustomTag(FullTraceConfig.getCustomTag());
            fullLog.setRequestId(requestId);
            fullLog.setPath(request.getRequestURI());
            fullLog.setMethod(request.getMethod());
            fullLog.setTimeTaken(Duration.between(startTime, Instant.now()).toMillis());
            fullLog.setTime(LocalDateTime.now().format(DTF));
            fullLog.setParameterMap(mapToString(request.getParameterMap()));
            fullLog.setStatus(status);
            fullLog.setIp(logCollector.getIp(wrapperRequest));
            fullLog.setRequestHeader(getRequestHeader(wrapperRequest));
            fullLog.setRequestBody(getRequestBody(wrapperRequest));
            fullLog.setResponseHeader(getResponseHeader(wrapperResponse));
            fullLog.setResponseBody(getResponseBody(wrapperResponse));
            logCollector.collect(fullLog, wrapperRequest, wrapperResponse);
            updateResponse(response);
        }
    }

    /**
     * 是否需要追踪这个路径的信息
     *
     * @param path 路径
     * @return true=应该处理这个路径
     */
    private boolean needCollect(String path) {
        boolean emptyExclude = CollectionUtils.isEmpty(FullTraceConfig.getExcludeUri());
        boolean emptyInclude = CollectionUtils.isEmpty(FullTraceConfig.getIncludeUri());
        if (emptyExclude && emptyInclude) {
            // 都为空则都处理
            return true;
        }
        if (emptyExclude) {
            // exclude为空，则只看include
            return FullTraceConfig.checkInclude(path);
        } else {
            // exclude不为空则只看exclude
            return !FullTraceConfig.checkExclude(path);
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

    private String getRequestHeader(ContentCachingRequestWrapper request) {
        if (request == null) {
            return null;
        }
        StringBuilder header = new StringBuilder("");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            header.append("|")
                  .append(headerName)
                  .append(":")
                  .append(request.getHeader(headerName));
        }
        if (header.length() > 0) {
            header.deleteCharAt(0);
        }
        return header.toString();
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        if (request == null) {
            return null;
        }
        String requestBody = "";
        try {
            requestBody = IOUtils
                    .toString(request.getContentAsByteArray(), request.getCharacterEncoding())
                    .replaceAll("\\r\\n|\\r|\\n|\\t", "");
        } catch (IOException e) {
            log.error("请求体输出到日志异常：" + e.getMessage());
        }
        return requestBody;
    }

    private String getResponseHeader(ContentCachingResponseWrapper response) {
        if (response == null) {
            return null;
        }
        StringBuilder header = new StringBuilder("");
        Collection<String> headerNames = response.getHeaderNames();
        headerNames.forEach(x -> header.append("|").append(x).append(":").append(response.getHeader(x)));
        if (header.length() > 0) {
            header.deleteCharAt(0);
        }
        return header.toString();
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        if (response == null) {
            return null;
        }
        String responseBody = "";
        try {
            responseBody = IOUtils.toString(response.getContentAsByteArray(), response.getCharacterEncoding());
        } catch (IOException e) {
            log.error("响应体输出到日志异常：" + e.getMessage());
        }
        return responseBody;
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        Objects.requireNonNull(responseWrapper).copyBodyToResponse();
    }

    private String mapToString(Map<String, String[]> parameterMap) {
        if (CollectionUtils.isEmpty(parameterMap)) {
            return null;
        }
        StringBuilder result = new StringBuilder("");
        parameterMap.forEach((k, v) -> result.append("|").append(k).append("=").append(Arrays.toString(v)));
        result.deleteCharAt(0);
        return result.toString();
    }
}
