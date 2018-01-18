package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.SearchIndexCheck;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.sql.OutputFormat;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.search.SeriesSearchQuery;
import com.axibase.tsd.api.model.series.search.SeriesSearchResult;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.FOUND;
import static javax.ws.rs.core.Response.Status.OK;

public class SeriesMethod extends BaseMethod {
    private static final String METHOD_SERIES_INSERT = "/series/insert";
    private static final String METHOD_SERIES_QUERY = "/series/query";
    private static final String METHOD_SERIES_URL_QUERY = "/series/{format}/{entity}/{metric}";
    private static final String METHOD_SERIES_SEARCH = "/search";
    private static final String METHOD_REINDEX = "/admin/series/index";

    public static <T> Response insertSeries(final T seriesList, String user, String password) {
        Response response = executeApiRequest(webTarget -> {
            Invocation.Builder builder = webTarget.path(METHOD_SERIES_INSERT).request();

            builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, user);
            builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);

            return builder.post(Entity.json(seriesList));
        });

        response.bufferEntity();
        return response;
    }

    public static <T> Response insertSeries(final T seriesList) throws FileNotFoundException {
        return insertSeries(seriesList, Config.getInstance().getLogin(), Config.getInstance().getPassword());
    }

    public static <T> Response querySeries(T... queries) {
        return querySeries(Arrays.asList(queries));
    }

    public static <T> Response querySeries(List<T> queries) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_SERIES_QUERY)
                .request()
                .post(Entity.json(queries)));
        response.bufferEntity();
        return response;
    }

    public static Response querySeries(String query) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_SERIES_QUERY)
                .request()
                .post(Entity.text(query)));
        response.bufferEntity();
        return response;
    }

    public static Response urlQuerySeries(String entity, String metric, OutputFormat format, Map<String, String> parameters) {
        return urlQuerySeries(entity, metric, format, parameters, null, null);
    }

    public static Response urlQuerySeries(
            String entity,
            String metric,
            OutputFormat format,
            Map<String, String> parameters,
            String user,
            String password) {
        Response response = executeApiRequest(webTarget -> {
            WebTarget target = webTarget
                .path(METHOD_SERIES_URL_QUERY)
                .resolveTemplate("format", format.toString())
                .resolveTemplate("entity", entity)
                .resolveTemplate("metric", metric);

            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                target = target.queryParam(entry.getKey(), entry.getValue());
            }

            Invocation.Builder builder = target.request();

            if (user != null && password != null) {
                builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, user);
                builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);
            }
            return builder.get();
        });

        response.bufferEntity();
        return response;
    }

    public static Response urlQuerySeries(String entity, String metric, Map<String, String> parameters) {
        return urlQuerySeries(entity, metric, OutputFormat.JSON, parameters);
    }

    public static SeriesSearchResult searchSeries(SeriesSearchQuery query) {
        Response response = executeApiRequest(webTarget -> {
            WebTarget target = webTarget.path(METHOD_SERIES_SEARCH);
            target = addParameters(target, query);
            Invocation.Builder builder = target.request();
            return builder.get();
        });

        response.bufferEntity();
        return response.readEntity(SeriesSearchResult.class);
    }

    public static Response searchRawSeries(SeriesSearchQuery query) {
        Response response = executeApiRequest(webTarget -> {
            WebTarget target = webTarget.path(METHOD_SERIES_SEARCH);
            target = addParameters(target, query);
            Invocation.Builder builder = target.request();
            return builder.get();
        });
        response.bufferEntity();
        return response;
    }

    public static void updateSearchIndex() throws Exception {
        Response response = executeRootRequest(webTarget -> webTarget
                .path(METHOD_REINDEX)
                .request()
                .post(Entity.text("reindex=Reindex")));
        if (FOUND.getStatusCode() != response.getStatus()) {
            throw new Exception("Failed to execute search index update");
        }
        Checker.check(new SearchIndexCheck());
    }

    public static String getIndexerStatus() throws Exception {
        Response response = executeRootRequest(webTarget -> webTarget
                .path(METHOD_REINDEX)
                .request()
                .get());

        if (OK.getStatusCode() != response.getStatus()) {
            throw new Exception("Failed to get search index status");
        }

        response.bufferEntity();
        try {
            Document document = Jsoup.parse(response.readEntity(String.class));
            Element indexInfoTableElement = document.getElementById("indexInfo");
            Elements tableInfoElements = indexInfoTableElement.select("tr");
            Element statusRow = tableInfoElements.get(3);
            return statusRow.child(1).text();
        } catch (Exception e) {
            throw new Exception("Failed to parse search index status page", e);
        }
    }

    public static void insertSeriesCheck(Series... series) throws Exception {
        insertSeriesCheck(Arrays.asList(series));
    }

    public static void insertSeriesCheck(final List<Series> seriesList) throws Exception {
        insertSeriesCheck(seriesList, new SeriesCheck(seriesList));
    }

    public static void insertSeriesCheck(final List<Series> seriesList, AbstractCheck check) throws Exception {
        Response response = insertSeries(seriesList);
        if (OK.getStatusCode() != response.getStatus()) {
            throw new Exception("Fail to execute insertSeries query");
        }
        Checker.check(check);
    }

    public static List<Series> querySeriesAsList(SeriesQuery... seriesQuery) {
        Response response = querySeries(seriesQuery);
        return Arrays.asList(response.readEntity(Series[].class));
    }


    public static Response executeQueryRaw(final List<SeriesQuery> seriesQueries) {
        return executeQueryRaw(seriesQueries, null, null);
    }

    public static Response executeQueryRaw(final List<SeriesQuery> seriesQueries, String user, String password) {
        Response response = executeApiRequest(webTarget -> {
            Invocation.Builder builder = webTarget.path(METHOD_SERIES_QUERY).request();
            if (user != null && password != null) {
                builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, user);
                builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);
            }
            return builder.post(Entity.json(seriesQueries));
        });

        response.bufferEntity();
        return response;
    }
}
