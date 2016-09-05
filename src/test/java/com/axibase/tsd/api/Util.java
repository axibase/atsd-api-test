package com.axibase.tsd.api;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.version.Version;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.json.JSONException;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Dmitry Korchagin.
 */
public class Util {

    public static final Long MILLIS_IN_DAY = 1000 * 60 * 60 * 24L;
    public static final String DEFAULT_TIMEZONE_NAME = "UTC";

    static {
        Registry.Metric.register(BaseMethod.RULE_METRIC_NAME);
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    public static Date getPreviousDay() {
        return new Date(System.currentTimeMillis() - MILLIS_IN_DAY);

    }


    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat format;
        try {
            format = new SimpleDateFormat(pattern);
            format.setTimeZone(Util.getServerTimeZone());
        } catch (JSONException e) {
            throw new IllegalStateException("Failed to format date!");
        }
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
            throw new RuntimeException(e);
        }
        return d;
    }

    public static String generateStringFromChar(char c, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    public static String addOneMS(String date) {
        return ISOFormat(parseDate(date).getTime() + 1);
    }

    public static Long getMillis(String date) throws ParseException {
        return parseDate(date).getTime();
    }

    public static String transformDateToServerTimeZone(String date, int offsetMinutes) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(parseDate(date));
        instance.add(Calendar.MINUTE, -offsetMinutes);
        return ISOFormat(instance.getTime());
    }
}
