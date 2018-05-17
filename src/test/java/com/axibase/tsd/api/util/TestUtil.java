package com.axibase.tsd.api.util;

import com.axibase.tsd.api.model.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import static com.axibase.tsd.api.util.TestUtil.TimeTranslation.UNIVERSAL_TO_LOCAL;
import static com.axibase.tsd.api.util.Util.*;

public class TestUtil {
    public static final Long MILLIS_IN_DAY = 1000 * 60 * 60 * 24L;

    public enum TimeTranslation {
        LOCAL_TO_UNIVERSAL, UNIVERSAL_TO_LOCAL
    }

    public static String formatDate(Date date, String pattern, TimeZone timeZone) {
        SimpleDateFormat format;
        format = new SimpleDateFormat(pattern);
        format.setTimeZone(timeZone);
        return format.format(date);
    }

    public static String formatDate(Date date, String pattern) {
        return formatDate(date, pattern, getServerTimeZone());
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    public static Date getPreviousDay() { return new Date(System.currentTimeMillis() - MILLIS_IN_DAY); }

    public static Date getNextDay() {
        return new Date(System.currentTimeMillis() + MILLIS_IN_DAY);
    }

    public static String timeTranslate(String date, TimeZone timeZone, TimeTranslation mode) {
        Date parsed = parseDate(date);
        long time = parsed.getTime();
        long offset = timeZone.getOffset(time);

        if (mode == UNIVERSAL_TO_LOCAL) {
            time += offset;
        } else {
            time -= offset;
        }

        return ISOFormat(time);
    }

    public static String timeTranslateDefault(String date, TimeTranslation mode) {
        TimeZone timeZone = getServerTimeZone();
        return timeTranslate(date, timeZone, mode);
    }

    public static String addTimeUnitsInTimezone(
            String dateTime,
            ZoneId zoneId,
            TimeUnit timeUnit,
            int amount) {
        ZonedDateTime dateUtc = ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
        ZonedDateTime localDate = dateUtc.withZoneSameInstant(zoneId);
        switch (timeUnit) {
            case NANOSECOND: {
                localDate = localDate.plusNanos(amount);
                break;
            }
            case MILLISECOND: {
                localDate = localDate.plusNanos(amount * 1000);
                break;
            }
            case SECOND: {
                localDate = localDate.plusSeconds(amount);
                break;
            }
            case MINUTE: {
                localDate = localDate.plusMinutes(amount);
                break;
            }
            case HOUR: {
                localDate = localDate.plusHours(amount);
                break;
            }
            case DAY: {
                localDate = localDate.plusDays(amount);
                break;
            }
            case WEEK: {
                localDate = localDate.plusWeeks(amount);
                break;
            }
            case MONTH: {
                localDate = localDate.plusMonths(amount);
                break;
            }
            case QUARTER: {
                localDate = localDate.plusMonths(3 * amount);
                break;
            }
            case YEAR: {
                localDate = localDate.plusYears(amount);
                break;
            }
        }

        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
        return localDate.withZoneSameInstant(ZoneId.of("Etc/UTC")).format(isoFormatter);
    }

    public static String formatAsLocalTime(String isoDate) {
        TimeZone serverTimeZone = Util.getServerTimeZone();
        Date parsedDate = parseDate(isoDate);
        SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        localDateFormat.setTimeZone(serverTimeZone);
        return localDateFormat.format(parsedDate);
    }

    public static long truncateTime(long time, TimeZone trucnationTimeZone, TemporalUnit truncationUnit) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time),  trucnationTimeZone.toZoneId())
                .truncatedTo(truncationUnit)
                .toInstant()
                .toEpochMilli();
    }

    public static long plusTime(long time, long amount,
                                    TimeZone plusTimeZone, TemporalUnit plusUnit) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time),  plusTimeZone.toZoneId())
                .plus(amount, plusUnit)
                .toInstant()
                .toEpochMilli();
    }

    public static <T> List<List<T>> twoDArrayToList(T[][] twoDArray) {
        List<List<T>> list = new ArrayList<List<T>>();
        for (T[] array : twoDArray) {
            list.add(Arrays.asList(array));
        }
        return list;
    }

    public static StringBuilder appendChar(StringBuilder sb, char c, int count) {
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb;
    }

    public static String extractJSONObjectFieldFromJSONArrayByIndex(int index, String field, JSONArray array) throws JSONException {
        if (array == null) {
            return "JSONArray is null";
        }
        return (((JSONObject) array.get(index)).get(field)).toString();
    }

    public static byte[] getGzipBytes(String inputString) {
        byte[] rawInput = inputString.getBytes();
        byte[] gzipBytes;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(rawInput.length);
             GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
            gzos.write(rawInput);
            gzos.close();
            gzipBytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Gzip compression of string variable failed");
        }

        return gzipBytes;
    }

    public static String quoteEscape(String s) {
        StringBuilder resultBuilder = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\'') {
                resultBuilder.append('\'');
            }
            resultBuilder.append(c);
        }
        return resultBuilder.toString();
    }


    public static <T> Set<T> hashSet(T... objects) {
        return new HashSet<>(Arrays.asList(objects));
    }
}
