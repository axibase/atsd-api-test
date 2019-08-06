package com.axibase.tsd.api.util;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.version.Version;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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

    public static String getHBaseVersion() {
        return AtsdVersionInfo.HBASE_VERSION;
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

    public static Response.Status.Family responseFamily(final Response response) {
        if (response == null) return null;
        return response.getStatusInfo().getFamily();
    }

    public static String addOneMS(String date) {
        return ISOFormat(parseDate(date).getTime() + 1);
    }

    public static long getUnixTime(String date){
        return parseDate(date).getTime();
    }

    public static Date parseDate(String date) {
        try {
            return ISO8601Utils.parse(date, new ParsePosition(0));
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Fail to parse date: %s", date));
        }
    }

    public static ZonedDateTime parseAsServerZoned(final String dateString) {
        final LocalDateTime localDateTime = LocalDateTime.parse(dateString);
        return ZonedDateTime.of(localDateTime, getServerTimeZone().toZoneId());
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
        private static final String HBASE_VERSION;

        static {
            final Version version = VersionMethod.queryVersion().readEntity(Version.class);
            TIME_ZONE = TimeZone.getTimeZone(version.getDate().getTimeZone().getName());
            HBASE_VERSION = version.getBuildInfo().getHbaseVersion();
        }
    }
}
