package com.axibase.tsd.api.util;

import com.axibase.tsd.api.model.TimeUnit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Util {
    public static final String MIN_QUERYABLE_DATE = "1000-01-01T00:00:00.000Z";
    public static final String MAX_QUERYABLE_DATE = "9999-12-31T23:59:59.999Z";
    public static final String MIN_STORABLE_DATE = "1970-01-01T00:00:00.000Z";
    public static final String MAX_STORABLE_DATE = "2106-02-07T06:59:59.999Z";
    public static final Long MILLIS_IN_DAY = 1000 * 60 * 60 * 24L;
    public static final String DEFAULT_TIMEZONE_NAME = "UTC";
    private static ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    public static Date getCurrentDate() {
        return new Date();
    }


    public static String formatDate(Date date, String pattern, TimeZone timeZone) {
        SimpleDateFormat format;
        format = new SimpleDateFormat(pattern);
        format.setTimeZone(timeZone);
        return format.format(date);
    }

    public static Date getNextDay() {
        return new Date(System.currentTimeMillis() + MILLIS_IN_DAY);
    }

    public static String ISOFormat(Date date) {
        return ISOFormat(date, true, DEFAULT_TIMEZONE_NAME);
    }

    public static String ISOFormat(long t) {
        return ISOFormat(new Date(t));
    }

    public static String ISOFormat(Date date, boolean withMillis, String timeZoneName) {
        String pattern = (withMillis) ? "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" : "yyyy-MM-dd'T'HH:mm:ssXXX";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneName));
        return dateFormat.format(date);
    }

    public static Date parseDate(String date) {
        Date d = null;
        try {
            d = ISO8601Utils.parse(date, new ParsePosition(0));
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Fail to parse date: %s", date));
        }
        return d;
    }

    public static String prettyPrint(Object o) {
        try {
            return objectWriter.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return o.toString();
        }
    }

    public static String addOneMS(String date) {
        return ISOFormat(parseDate(date).getTime() + 1);
    }

    public static Long getMillis(String date){
        return parseDate(date).getTime();
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
}
