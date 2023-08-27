package im.langchainjava.utils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtils {

    private static long ONE_DAY_SEC = 24L * 3600L;
    private static long ONE_DAY_MILLI = 24L * 3600L * 1000L;

    public static long getTime(String date, int timezoneOffsetSec){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        ZonedDateTime dt = LocalDate.parse(date, formatter).atStartOfDay(ZoneOffset.ofTotalSeconds(timezoneOffsetSec));
        return dt.toEpochSecond();
    }

    public static int getDayDiff(String date, int timezoneOffsetSec){
        long today = LocalDate.now().atStartOfDay(ZoneOffset.ofTotalSeconds(timezoneOffsetSec)).toEpochSecond();
        long secDiff = getTime(date, timezoneOffsetSec) - today;
        return (int)(secDiff / ONE_DAY_SEC);
    }

    public static Date getOffsetedDate(int offset){
        return new Date(System.currentTimeMillis() + offset * ONE_DAY_MILLI);
    }

}
