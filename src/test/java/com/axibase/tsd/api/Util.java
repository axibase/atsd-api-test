package com.axibase.tsd.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Dmitry Korchagin.
 */
public class Util {

    private static final Long MILLIS_IN_DAY = 1000 * 60 * 60 * 24L;

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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    public static String ISOFormat(long t) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(t);
        return ISOFormat(calendar.getTime());
    }

    public static Date getDate(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.parse(date);
    }

    public static String getMinDate() {
        return "1970-01-01T00:00:00Z";
    }

    public static String getMaxDate() {
        return "9999-01-01T00:00:00Z";
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

//            for (int i = 0x21; i < 0x7e; i++) { //visible character
//                pool.add((char) i);
//            }

//            for (int i = 0x80; i < 0xFF; i++) { //Latin-1 Supplement
//                pool.add((char) i);
//            }

//            for (int i = 0x100; i < 0x17F; i++) { //Latin Extended-A
//                pool.add((char) i);
//            }
//
//            for (int i = 0x370; i < 0x52F; i++) { //Greek and Coptic, Cyrillic, Cyrillic Supplement
//                pool.add((char) i);
//            }

//            for (int i = 0x4E00; i < 0x4E5F; i++) {//9FFF; i++) { //CJK Unified Ideographs
//                pool.add((char) i);
//            }
//            logger.debug("visible character pool: {}", pool);
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
}
