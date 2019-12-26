package com.axibase.tsd.api.util;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.version.Version;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;


public class Util {
    public static final String MIN_QUERYABLE_DATE = "1000-01-01T00:00:00.000Z";
    public static final String MAX_QUERYABLE_DATE = "9999-12-31T23:59:59.999Z";
    public static final String MIN_STORABLE_DATE = "1970-01-01T00:00:00.000Z";
    public static final String MAX_STORABLE_DATE = "2106-02-07T06:59:59.999Z";
    public static final long MIN_STORABLE_TIMESTAMP = 0L;
    public static final long MAX_STORABLE_TIMESTAMP = 4294969199999L;
    public static final String DEFAULT_TIMEZONE_NAME = "UTC";
    public static final String API_PATH = Config.getInstance().getApiPath();
    private static final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    public static TimeZone getServerTimeZone() {
        return AtsdVersionInfo.TIME_ZONE;
    }

    public static ZoneId getServerZoneId() {
        return AtsdVersionInfo.ZONE_ID;
    }

    public static String getHBaseVersion() {
        return AtsdVersionInfo.HBASE_VERSION;
    }

    public static String ISOFormat(Date date) {
        return ISOFormat(date.getTime());
    }

    public static String ISOFormat(long timestamp) {
        return DateProcessorManager.ISO.print(timestamp, ZoneOffset.UTC);
    }

    public static String ISOFormat(ZonedDateTime dateTime) {
        return DateProcessorManager.ISO.print(dateTime.withZoneSameInstant(ZoneOffset.UTC));
    }

    public static Response.Status.Family responseFamily(final Response response) {
        if (response == null) return null;
        return response.getStatusInfo().getFamily();
    }

    public static String addOneMS(String date) {
        return ISOFormat(getUnixTime(date) + 1);
    }

    public static long getUnixTime(String date){
        return DateProcessorManager.ISO.parseMillis(date);
    }

    public static Date parseDate(String date) {
        return new Date(getUnixTime(date));
    }

    public static long parseAsMillis(String date, String format, ZoneId timeZone) {
        return DateProcessorManager.getTimeProcessor(format).parseMillis(date, timeZone);
    }

    public static ZonedDateTime parseAsZonedDateTime(String date, String format, ZoneId timeZone) {
        return DateProcessorManager.getTimeProcessor(format).parse(date, timeZone);
    }

    public static ZonedDateTime parseAsServerZoned(final String dateString) {
        final LocalDateTime localDateTime = LocalDateTime.parse(dateString);
        return ZonedDateTime.of(localDateTime, getServerZoneId());
    }

    /** Return zoned date time in the {@link #DEFAULT_TIMEZONE_NAME}. */
    public static ZonedDateTime fromMillis(long epochMillis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.of(DEFAULT_TIMEZONE_NAME));
    }

    public static String prettyPrint(Object o) {
        if (o instanceof Form) {
            final List<BasicNameValuePair> keyValuePairs = ((Form) o).asMap().entrySet()
                    .stream()
                    .flatMap(e -> e.getValue().stream().map(value -> new BasicNameValuePair(e.getKey(), value)))
                    .collect(Collectors.toList());
            return URLEncodedUtils.format(keyValuePairs, StandardCharsets.UTF_8);
        }
        try {
            return objectWriter.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return o.toString();
        }
    }

    public static Map<String, Object> toStringObjectMap(Map<String, String> map) {
        return map.entrySet().stream().collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    private static final class AtsdVersionInfo {
        private static final TimeZone TIME_ZONE;
        private static final ZoneId ZONE_ID;
        private static final String HBASE_VERSION;

        static {
            final Version version = VersionMethod.queryVersion().readEntity(Version.class);
            TIME_ZONE = TimeZone.getTimeZone(version.getDate().getTimeZone().getName());
            ZONE_ID = TIME_ZONE.toZoneId();
            HBASE_VERSION = version.getBuildInfo().getHbaseVersion();
        }
    }
}
