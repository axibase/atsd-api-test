package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.SearchIndexCheck;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.sql.OutputFormat;
import com.axibase.tsd.api.model.series.*;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
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
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static <T> Response insertSeries(final T seriesList, String user, String password) {
        Invocation.Builder builder = httpApiResource.path(METHOD_SERIES_INSERT).request();

        builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, user);
        builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);

        Response response = builder.post(Entity.json(seriesList));
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
        Response response = httpApiResource.path(METHOD_SERIES_QUERY).request().post(Entity.json(queries));
        response.bufferEntity();
        return response;
    }

    public static Response urlQuerySeries(String entity, String metric, OutputFormat format, Map<String, String> parameters) {
        return urlQuerySeries(entity, metric, format, parameters, null, null);
    }

    public static Response urlQuerySeries(String entity, String metric, OutputFormat format, Map<String, String> parameters, String user, String password) {
        WebTarget webTarget = httpApiResource
                .path(METHOD_SERIES_URL_QUERY)
                .resolveTemplate("format", format.toString())
                .resolveTemplate("entity", entity)
                .resolveTemplate("metric", metric);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
        }
        Invocation.Builder builder = webTarget.request();

        if (user != null && password != null) {
            builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, user);
            builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);
        }
        Response response = builder.get();
        response.bufferEntity();
        return response;
    }

    public static Response urlQuerySeries(String entity, String metric, Map<String, String> parameters) {
        return urlQuerySeries(entity, metric, OutputFormat.JSON, parameters);
    }

    public static SeriesSearchResult searchSeries(SeriesSearchQuery query) {
        WebTarget webTarget = httpApiResource.path(METHOD_SERIES_SEARCH);
        webTarget = addParameters(webTarget, query);

        Invocation.Builder builder = webTarget.request();

        Response response = builder.get();
        response.bufferEntity();
        return response.readEntity(SeriesSearchResult.class);
    }

    public static void updateSearchIndex() throws Exception {
        Invocation.Builder builder =  httpRootResource.path(METHOD_REINDEX).request();
        Response response = builder.post(Entity.text("reindex=Reindex"));
        if (FOUND.getStatusCode() != response.getStatus()) {
            throw new Exception("Failed to execute search index update");
        }
        Checker.check(new SearchIndexCheck());
    }

    public static String getIndexerStatus() throws Exception {
        Invocation.Builder builder =  httpRootResource.path(METHOD_REINDEX).request();
        Response response = builder.get();
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

    public static <T> List<Series> executeQueryReturnSeries(T... seriesQuery) throws Exception {
        Response response = querySeries(seriesQuery);
        return response.readEntity(new GenericType<List<Series>>() {
        });
    }


    public static Response executeQueryRaw(final List<SeriesQuery> seriesQueries) {
        return executeQueryRaw(seriesQueries, null, null);
    }

    public static Response executeQueryRaw(final List<SeriesQuery> seriesQueries, String user, String password) {
        Invocation.Builder builder = httpApiResource.path(METHOD_SERIES_QUERY).request();
        if (user != null && password != null) {
            builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, user);
            builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);
        }
        Response response = builder.post(Entity.json(seriesQueries));
        response.bufferEntity();
        return response;
    }
}
