package com.axibase.tsd.api;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Dmitry Korchagin.
 */
public class Util {

    private static final Long MILLIS_IN_DAY = 1000 * 60 * 60 * 24L;
    public static final Long REQUEST_INTERVAL = 500L;
    public static final Long EXPECTED_PROCESSING_TIME = 2000L;
    public static final String MIN_QUERYABLE_DATE = "1000-01-01T00:00:00.000Z";
    public static final String MAX_QUERYABLE_DATE = "9999-12-31T23:59:59.999Z";
    public static final String MIN_STORABLE_DATE = "1970-01-01T00:00:00.000Z";
    public static final String MAX_STORABLE_DATE = "2106-02-07T06:59:59.999Z";

    public static Date getCurrentDate() {
        return new Date();
    }

    public static Date getPreviousDay() {
        return new Date(System.currentTimeMillis() - MILLIS_IN_DAY);

    }

    public static Date getNextDay() {
        return new Date(System.currentTimeMillis() + MILLIS_IN_DAY);
    }

    public static String ISOFormat(Date date) {
        return ISOFormat(date, true, "UTC");
    }

    public static String ISOFormat(long t) {
        return ISOFormat(new Date(t));
    }
    public static String ISOFormat(long t, boolean withMillis, String timeZoneName) {
        return ISOFormat(new Date(t), withMillis, timeZoneName);
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


    public static class ABNF {
        private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


        private static List<Character> generateVisibleCharPool() {
            List pool = new ArrayList<>();

            for (int i = 65; i < 90; i++) { //A-Z
                pool.add((char) i);
            }

            for (int i = 97; i < 122; i++) { //a-z
                pool.add((char) i);
            }
            return pool;

        }

        public static String generateNAME(int length) {
            StringBuilder str = new StringBuilder();
            List<Character> characterPool = generateVisibleCharPool();
            Random randomGenerator = new Random();
            int poolSize = characterPool.size();
            for (int i = 0; i < length; i++) {
                char c = characterPool.get(randomGenerator.nextInt(poolSize));
                if (c == '"') {
                    str.append('\\');
                }
                str.append(c);
            }
            return str.toString();
        }

        public static String generateTEXTVALUE(int length) {
            StringBuilder str = new StringBuilder();
            List<Character> characterPool = generateVisibleCharPool();
            characterPool.add(' ');

            boolean needQuoted = false;
            Random randomGenerator = new Random();
            int poolSize = characterPool.size();
            for (int i = 0; i < length; i++) {
                char c = characterPool.get(randomGenerator.nextInt(poolSize));
                if (c == ' ') {
                    needQuoted = true;
                }
                if (c == '"') {
                    str.append('\\');
                }
                str.append(c);
            }

            if (needQuoted) {
                str.insert(0, '"');
                str.append('"');
            }
            return str.toString();
        }
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
        instance.add(Calendar.MINUTE,-offsetMinutes);
        return ISOFormat(instance.getTime());
    }
}
