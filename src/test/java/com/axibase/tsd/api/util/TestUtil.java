package com.axibase.tsd.api.util;

import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
    public static final Long MILLIS_IN_DAY = 86400000L;
    public static final String NaN = Double.toString(Double.NaN);

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

    public static Date getPreviousDay() {
        return new Date(System.currentTimeMillis() - MILLIS_IN_DAY);
    }

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

    public static Sample sampleToServerTimezone(final Sample sample) {
        final String translatedDate = timeTranslateDefault(sample.getRawDate(), TimeTranslation.LOCAL_TO_UNIVERSAL);
        return Sample.ofDateDecimal(translatedDate, sample.getValue());
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
                localDate = localDate.plusMonths(3L * amount);
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
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), trucnationTimeZone.toZoneId())
                .truncatedTo(truncationUnit)
                .toInstant()
                .toEpochMilli();
    }

    public static long plusTime(long time, long amount,
                                TimeZone plusTimeZone, TemporalUnit plusUnit) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), plusTimeZone.toZoneId())
                .plus(amount, plusUnit)
                .toInstant()
                .toEpochMilli();
    }

    public static <T> List<List<T>> twoDArrayToList(T[][] twoDArray) {
        List<List<T>> list = new ArrayList<>();
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
        byte[] rawInput = inputString.getBytes(Charsets.UTF_8);
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

    /**
     * Read Object from file.
     *
     * @param file  - {@link File} instance.
     * @param clazz - object's class
     * @param <T>   - type of returned object.
     * @return return object casted to json.
     * @throws IOException - may be thrown in file's reading process.
     */
    private static <T> T jsonFile(File file, Class<T> clazz) throws IOException {
        return new ObjectMapper().readValue(file, clazz);
    }

    /**
     * Read JSON Array from file.
     *
     * @param file  - {@link File} instance.
     * @param clazz - object's class
     * @param <T>   - type of returned object.
     * @return return object casted to json.
     * @throws IOException - may be thrown in file's reading process.
     */
    private static <T> T[] jsonArrayFile(File file, Class<T[]> clazz) throws IOException {
        return jsonFile(file, clazz);
    }

    /**
     * Read Provider from local json file.
     *
     * @param file  {@link File} instance.
     * @param clazz Class to serialize.
     * @param <T>   Class type.
     * @return return provider in TestNG format.
     * @throws IOException in case of IO problems with provider's file while opening.
     */
    public static <T> Object[][] jsonProvider(File file, Class<T[]> clazz) throws IOException {
        return convertTo2DimArray(jsonArrayFile(file, clazz));
    }

    /**
     * [A, B, C] -> [[A], [B], [C]]
     */
    public static Object[][] convertTo2DimArray(Object[] data) {
        return Arrays.stream(data).map(ArrayUtils::toArray).toArray(Object[][]::new);
    }

    public static Object[][] convertTo2DimArray(Collection<?> data) {
        return data.stream().map(ArrayUtils::toArray).toArray(Object[][]::new);
    }

    public static Map<String, String> createTags(String... tags) {
        if (tags.length % 2 != 0) {
            throw new IllegalArgumentException("Tag name without value in arguments");
        }

        Map<String, String> mapTags = new HashMap<>();
        for (int i = 0; i < tags.length; i += 2) {
            String name = tags[i];
            String value = tags[i + 1];

            if (name == null || value == null || name.isEmpty() || value.isEmpty()) {
                throw new IllegalArgumentException("Series tag name or value is null or empty");
            }

            mapTags.put(name, value);
        }

        return mapTags;
    }

    /**
     * Transforms object vararg to unmodifiable {@code LinkedHashMap<String, Object>}. Use this method instead of guava ImmutableMap when you have null in values
     * @param objects vararg to transform into map. Count must be even and every non-even element must be String
     * @return Unmodifiable LinkedHashMap<String, Object>
     */
    public static Map<String, Object> toUnmodifiableMap(Object... objects) {
        if(objects.length %2 != 0) {
            throw new IllegalArgumentException("Objects count must be even!");
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for(int i = 0; i < objects.length; i+=2) {
            if(!(objects[i] instanceof String)) {
                throw new IllegalArgumentException("Keys must be Strings!");
            }
            map.put(objects[i].toString(), objects[i+1]);
        }
        return Collections.unmodifiableMap(map);
    }
}
