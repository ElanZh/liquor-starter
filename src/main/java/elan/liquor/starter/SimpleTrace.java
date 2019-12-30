package elan.liquor.starter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class SimpleTrace implements HttpTraceRepository {

    @Override
    public List<HttpTrace> findAll() {
        return Collections.emptyList();
    }

    @Override
    public void add(HttpTrace trace) {
        if (log.isDebugEnabled()) {
            HttpTrace.Request request = trace.getRequest();
            HttpTrace.Response response = trace.getResponse();
            String time = LocalDateTime.ofInstant(trace.getTimestamp(), DateUtil.SHANGHAI).format(DateUtil.DTF);
            log.debug("http-trace-log| method: {}, path: {}, remoteAddress: {}, query: {}, rawQuery: {}, requestHeaders: {}, responseHeaders: {}, timeTaken: {}ms, time: {}."
                    , request.getMethod()
                    , request.getUri().getPath()
                    , request.getRemoteAddress()
                    , request.getUri().getQuery()
                    , request.getUri().getRawQuery()
                    , mapToString(request.getHeaders())
                    , mapToString(response.getHeaders())
                    , trace.getTimeTaken()
                    , time
            );
        }
    }

    private String mapToString(Map<String, List<String>> headers) {
        if (CollectionUtils.isEmpty(headers)) {
            return "";
        }
        StringBuilder result = new StringBuilder("");
        headers.forEach((k, v) -> result.append("|").append(k).append("=").append(StringUtils.join(v, ",")));
        result.deleteCharAt(0);
        return result.toString();
    }
}
