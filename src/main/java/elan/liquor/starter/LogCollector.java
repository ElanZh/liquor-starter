package elan.liquor.starter;

/**
 * 日志采集器抽象
 */
public interface LogCollector {
    void collect (FullLog fullLog);
}
