package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Igor Shmagrinskiy
 *         <p>
 *         Class that make sql queries to ATSD instanse
 *         and retrive result in specifed format.
 *         Usage:
 *         * <pre>
 *                         {@code
 *                              SqlMethod
 *                                          .executeQuery("SELECT 1")
 *                                          .readEntity(String.class);
 *                         }
 *                         </pre>
 */
public class SqlMethod extends BaseMethod {
    private static final String METHOD_SQL_API = "/api/sql";
    private static final Logger logger = LoggerFactory.getLogger(SqlMethod.class);
    protected static WebTarget httpSqlApiResource = httpRootResource
            .property(ClientProperties.CONNECT_TIMEOUT, 1000)
            .property(ClientProperties.READ_TIMEOUT, 1000)
            .path(METHOD_SQL_API);

    /**
     * Execute SQL executeQuery and retrieve result in specified format
     *
     * @param sqlQuery     SQL query in a String format
     * @param outputFormat some field from {@link OutputFormat}
     * @return instance of Response
     */
    public static Response executeQuery(String sqlQuery, OutputFormat outputFormat) {
        logger.debug("SQL query : {}", sqlQuery);
        Form form = new Form();
        form.param("q", sqlQuery);
        form.param("outputFormat", outputFormat.toString());
        return httpSqlApiResource
                .request()
                .post(Entity.form(form));
    }

    /**
     * Execute SQL executeQuery and retrieve result in specified format
     *
     * @param sqlQuery SQL executeQuery in a String format
     * @return instance of Response
     */
    public static Response executeQuery(String sqlQuery) {
        return executeQuery(sqlQuery, OutputFormat.JSON);
    }


    protected static void sendSamplesToSeries(Series series, Sample... samples) {
        boolean isSuccessInsert;
        series.setData(Arrays.asList(samples));
        try {
            isSuccessInsert = SeriesMethod.insertSeries(series, 1000);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to insert series: " + series);
        }
        if (!isSuccessInsert) {
            throw new IllegalStateException("Failed to insert series: " + series);
        }
    }

    protected static void updateSeriesEntityTags(Series series, Map<String, String> tags) {
        com.axibase.tsd.api.model.entity.Entity entity = new com.axibase.tsd.api.model.entity.Entity();
        entity.setTags(tags);
        try {
            EntityMethod.updateEntity(series.getEntity(), entity);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
