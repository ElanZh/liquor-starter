package elan.liquor.starter;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS").withZone(SHANGHAI);
}
