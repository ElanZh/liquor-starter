package elan.liquor.starter;

import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class HttpTraceLog implements HttpTraceRepository {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HttpTraceLog.class);
    @Override
    public List<HttpTrace> findAll() {
        return Collections.emptyList();
    }

    @Override
    public void add(HttpTrace trace) {
        if (log.isDebugEnabled()) {
            String path = trace.getRequest().getUri().getPath();
            String queryPara = trace.getRequest().getUri().getQuery();
            String queryParaRaw = trace.getRequest().getUri().getRawQuery();
            String method = trace.getRequest().getMethod();
            long timeTaken = trace.getTimeTaken();
            String time = LocalDateTime.ofInstant(trace.getTimestamp(), DateUtil.SHANGHAI).format(DateUtil.DTF);
            log.debug("http-trace-log| method: {}, path: {}, query: {}, rawQuery: {}, timeTaken: {}ms, time: {} ."
                    ,method , path, queryPara, queryParaRaw, timeTaken, time);
        }
    }
}
