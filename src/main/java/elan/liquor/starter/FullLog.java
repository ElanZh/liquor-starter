package elan.liquor.starter;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@lombok.Builder
public class FullLog {
    private String path;
    private String parameterMap;
    private String method;
    /** 耗时，毫秒 */
    private Long timeTaken;
    /** 请求时间 */
    private String time;
    private Integer status;
    private String requestBody;
    private String responseBody;
}
