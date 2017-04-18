package com.axibase.tsd.api.util;

import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.version.*;
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
import java.util.*;
import java.util.Date;
import java.util.TimeZone;

import static com.axibase.tsd.api.util.TestUtil.TimeTranslation.UNIVERSAL_TO_LOCAL;

public class TestUtil {
    public static final Long MILLIS_IN_DAY = 1000 * 60 * 60 * 24L;
    public static final String UNIVERSAL_TIMEZONE_NAME = "UTC";
    public static final Long LAST_INSERT_WRITE_PERIOD = 15000L;
    private static final TestNameGenerator NAME_GENERATOR = new TestNameGenerator();
    private static ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    public static Date getCurrentDate() {
        return new Date();
    }

    public static Date getPreviousDay() {
        return new Date(System.currentTimeMillis() - MILLIS_IN_DAY);

    }

    public static String formatDate(Date date, String pattern) {
        try {
            return formatDate(date, pattern, getServerTimeZone());
        } catch (JSONException e) {
            throw new IllegalStateException("Unknow timezone");
        }
    }

    public static String formatDate(Date date, String pattern, TimeZone timeZone) {
        SimpleDateFormat format;
        format = new SimpleDateFormat(pattern);
        format.setTimeZone(timeZone);
        return format.format(date);
    }

    private static TimeZone getServerTimeZone() throws JSONException {
        Version version = VersionMethod.queryVersion().readEntity(Version.class);
        return TimeZone.getTimeZone(version.getDate().getTimeZone().getName());
    }

    public static Date getNextDay() {
        return new Date(System.currentTimeMillis() + MILLIS_IN_DAY);
    }

    public static String ISOFormat(Date date) {
        return ISOFormat(date, true, UNIVERSAL_TIMEZONE_NAME);
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
            throw new RuntimeException(e);
        }
        return d;
    }

    private static String timeTranslate(String date, TimeZone timeZone, TimeTranslation mode) {
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

    private static String timeTranslateDefault(String date, TimeTranslation mode) {
        TimeZone timeZone;
        try {
            timeZone = getServerTimeZone();
        } catch (JSONException e) {
            throw new IllegalStateException("Unknown timezone");
        }
        return timeTranslate(date, timeZone, mode);
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

    public static Long getMillis(String date) throws ParseException {
        return parseDate(date).getTime();
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

    public static class TestNames {
        public static String metric() {
            return NAME_GENERATOR.getMetricName();
        }

        public static String entity() {
            return NAME_GENERATOR.getEntityName();
        }

        public static String entityGroup() {
            return NAME_GENERATOR.getTestName(TestNameGenerator.Keys.ENTITY_GROUP);
        }

        public static String property() {
            return NAME_GENERATOR.getTestName(TestNameGenerator.Keys.PROPERTY);
        }

        public static String message() {
            return NAME_GENERATOR.getTestName(TestNameGenerator.Keys.MESSAGE);
        }

        public static String propertyType() {
            return NAME_GENERATOR.getTestName(TestNameGenerator.Keys.PROPERTY_TYPE);
        }
    }

    public static enum TimeTranslation {
        LOCAL_TO_UNIVERSAL, UNIVERSAL_TO_LOCAL
    }
}
