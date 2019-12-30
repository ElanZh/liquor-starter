package elan.liquor.starter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 日志采集器抽象
 */
public interface LogCollector {
    void collect(FullLog fullLog, HttpServletRequest request, HttpServletResponse response);
}
