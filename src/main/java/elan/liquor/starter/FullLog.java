package elan.liquor.starter;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@lombok.Builder
public class FullLog {
    /** 自定义tag */
    private String customTag;
    /** 请求路径 */
    private String path;
    private String parameterMap;
    private String method;
    /** 耗时，毫秒 */
    private Long timeTaken;
    /** 请求时间 */
    private String time;
    /** 响应码 */
    private Integer status;
    private String requestBody;
    private String responseBody;
}
