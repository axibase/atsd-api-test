package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.transport.http.AtsdHttpResponse;
import com.axibase.tsd.api.transport.http.HTTPMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.util.Map;

public class CSVInsertMethod extends SeriesMethod {
    protected static final String METHOD_CSV_INSERT = "/series/csv/";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected Boolean csvInsert(String entity, String csv, Map<String, String> tags, long sleepDuration) throws IOException, InterruptedException {
        StringBuilder uri = new StringBuilder(METHOD_CSV_INSERT);
        uri.append(URLEncoder.encode(entity, "UTF-8"));
        if (tags != null && tags.size() > 0) {
            uri.append("?");
            for (String key : tags.keySet()) {
                uri.append("&").append(key).append("=").append(tags.get(key));
            }
        }

        AtsdHttpResponse response = httpSender.send(HTTPMethod.POST, uri.toString(), csv);
        Thread.sleep(sleepDuration);
        if (200 == response.getCode()) {
            logger.debug("CSV looks inserted");
        } else {
            logger.error("Fail to insert csv");
        }
        return 200 == response.getCode();
    }

    protected Boolean csvInsert(String entity, String csv, Map<String, String> tags) throws IOException, InterruptedException {
        return csvInsert(entity, csv, tags, 0);
    }

    protected Boolean csvInsert(String entity, String csv, long sleepDuration) throws IOException, InterruptedException {
        return csvInsert(entity, csv, null, sleepDuration);
    }

    protected Boolean csvInsert(String entity, String csv) throws IOException, InterruptedException {
        return csvInsert(entity, csv, null, 0);
    }
}
