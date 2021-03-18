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
import com.axibase.tsd.api.util.JsonParsingException;
import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.util.authorization.RequestSenderWithAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBasicAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBearerAuthorization;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.*;

import static javax.ws.rs.core.Response.Status.FOUND;

@Slf4j
public class SeriesMethod extends BaseMethod {
    private static final String METHOD_SERIES_INSERT = "/series/insert";
    private static final String METHOD_SERIES_QUERY = "/series/query";
    private static final String METHOD_SERIES_URL_QUERY = "/series/{format}/{entity}/{metric}";
    private static final String METHOD_SERIES_SEARCH = "/search";
    private static final String METHOD_REINDEX = "/admin/series/index";
    private static final String METHOD_SERIES_DELETE = "/series/delete";

    private static Map<String, Object> formatEntityMetricTemplate(String format, String entity, String metric) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("format", format);
        map.put("entity", entity);
        map.put("metric", metric);
        return Collections.unmodifiableMap(map);
    }

    public static <T> Response insertSeries(final T seriesList, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_SERIES_INSERT, HttpMethod.POST, Entity.json(seriesList));
        response.bufferEntity();
        return response;
    }

    public static <T> Response insertSeries(final T seriesList, String user, String password) {
        return insertSeries(seriesList, new RequestSenderWithBasicAuthorization(user, password));
    }

    public static <T> Response insertSeries(final T seriesList) {
        return insertSeries(seriesList, Config.getInstance().getLogin(), Config.getInstance().getPassword());
    }

    public static <T> Response insertSeries(final T seriesList, String token) {
        return insertSeries(seriesList, new RequestSenderWithBearerAuthorization(token));
    }

    public static <T> Response querySeries(List<T> queries, String token) {
        return querySeries(queries, new RequestSenderWithBearerAuthorization(token));
    }

    public static <T> Response querySeries(T query) {
        return querySeries(Collections.singletonList(query));
    }

    public static <T> Response querySeries(List<T> queries) {
        return querySeries(queries, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response querySeries(String query) {
        return querySeries(query, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static<T> Response querySeries(T query, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_SERIES_QUERY, HttpMethod.POST, Entity.json(query));
        response.bufferEntity();
        exceptionIfNotJson(response);
        return response;
    }

    /**
     * Try to parse response string as json and throw an exception if parsing fails.
     * This check is used here because the Jackson and JSONAssert libraries
     * happily parse some bad ATSD responses, like following:
     *
     * [{"metric":"cpu_usage"}]DATA_API_SERIES_QUERY_PROCESSING_ERRORcom.axibase.Class: Exception
     *
     * As alternative implementation we can check that response does not contain the string
     * "DATA_API_SERIES_QUERY_PROCESSING_ERROR".
     */
    private static void exceptionIfNotJson(Response response) {
        String responseAsString = response.readEntity(String.class);
        try {
            JSONValue.parseWithException(responseAsString);
        } catch (ParseException e) {
            String message = String.format("%s %n Response: %n%s", e.toString(), responseAsString);
            throw new JsonParsingException(message);
        }
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
        return urlQuerySeries(entity, metric, format, parameters, new RequestSenderWithBasicAuthorization(user, password));
    }

    public static Response urlQuerySeries(String entity, String metric, Map<String, String> parameters) {
        return urlQuerySeries(entity, metric, OutputFormat.JSON, parameters);
    }

    public static Response urlQuerySeries(String entity, String metric, Map<String, String> parameters, String token){
        return urlQuerySeries(entity, metric, OutputFormat.JSON, parameters, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response urlQuerySeries(
            String entity,
            String metric,
            OutputFormat format,
            Map<String, String> parameters,
            RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_SERIES_URL_QUERY, formatEntityMetricTemplate(format.toString(), entity, metric)
                ,Util.toStringObjectMap(parameters), Collections.EMPTY_MAP, HttpMethod.GET);
        response.bufferEntity();
        return response;
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

        response.bufferEntity();
        String result = response.readEntity(String.class);

        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            log.error("Failed to get search index status. Response:\n{}", response);
            throw new Exception("Failed to get search index status");
        }

        try {
            Document document = Jsoup.parse(result);
            Element indexInfoTableElement = document.getElementById("indexInfo");
            Elements tableInfoElements = indexInfoTableElement.select("tr");
            Element statusRow = tableInfoElements.get(3);
            return statusRow.child(1).text();
        } catch (Exception e) {
            throw new Exception("Failed to parse search index status page", e);
        }
    }

    public static void insertSeriesCheck(final Collection<Series> series) throws Exception {
        insertSeriesCheck(new ArrayList<>(series));
    }

    public static void insertSeriesCheck(Series... series) throws Exception {
        insertSeriesCheck(Arrays.asList(series));
    }

    public static void insertSeriesCheck(final List<Series> seriesList) throws Exception {
        insertSeriesCheck(seriesList, new SeriesCheck(seriesList));
    }

    public static void insertSeriesCheck(final List<Series> seriesList, AbstractCheck check) throws Exception {
        Response response = insertSeries(seriesList);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new Exception("Fail to execute insertSeries query");
        }
        Checker.check(check);
    }

    public static List<Series> querySeriesAsList(SeriesQuery... seriesQuery) {
        Response response = querySeries(Arrays.asList(seriesQuery));
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

    public static <T> Response deleteSeries(T query, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_SERIES_DELETE, HttpMethod.POST, Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static <T> Response deleteSeries(T query, String token) {
        return deleteSeries(query, new RequestSenderWithBearerAuthorization(token));
    }

    public static JsonNode getResponseAsTree(SeriesQuery query) throws JsonProcessingException {
        Response response = SeriesMethod.querySeries(query);
        return BaseMethod.responseAsTree(response);
    }
}
