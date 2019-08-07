package com.axibase.tsd.api.method.replacementtable;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;
import com.axibase.tsd.api.util.NotCheckedException;
import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.util.authorization.RequestSenderWithAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBasicAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBearerAuthorization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Slf4j
public class ReplacementTableMethod extends BaseMethod {
    private static final String METHOD_TABLE_JSON = "/replacement-tables/json/{table}";
    private static final String METHOD_REPLACEMENT_TABLE = "/replacement-tables/{table}";

    private static Map<String, Object> nameTemplate(String name) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("table", name);
        return Collections.unmodifiableMap(map);
    }

    public static Response createResponse(ReplacementTable replacementTable, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_TABLE_JSON, nameTemplate(replacementTable.getName()),
                HttpMethod.PUT, Entity.json(replacementTable));
        response.bufferEntity();
        return response;
    }

    private static Response createResponse(ReplacementTable table) {
        return createResponse(table, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response createResponse(ReplacementTable table, String token) {
        return createResponse(table, new RequestSenderWithBearerAuthorization(token));
    }

    public static void createCheck(ReplacementTable table) {
        Response response = createResponse(table);

        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            String errorMessage = "Wasn't able to create a replacement table, Status Info is " + response.getStatusInfo();
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    public static Response getReplacementTablesResponse(String replacementTableName, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_TABLE_JSON, nameTemplate(replacementTableName), HttpMethod.GET);
        response.bufferEntity();
        return response;
    }

    private static Response getReplacementTablesResponse(String replacementTableName) {
        return getReplacementTablesResponse(replacementTableName, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response getReplacementTablesResponse(String replacementTableName, String token) {
        return getReplacementTablesResponse(replacementTableName, new RequestSenderWithBearerAuthorization(token));
    }

    private static ReplacementTable findReplacementTable(final String replacementTableName) throws NotCheckedException {
        String replacementTableNameLowerCase = replacementTableName.replace(" ", "_").toLowerCase();
        final Response response = getReplacementTablesResponse(replacementTableNameLowerCase);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            if (response.getStatus() == NOT_FOUND.getStatusCode()) {
                return null;
            }
            String message = "Fail to execute replacement table query: " + response.getStatusInfo();
            log.error(message);
            throw new NotCheckedException(message);
        }

        try {
            ReplacementTable replacementTable = response.readEntity(ReplacementTable.class);

            return replacementTable;

        } catch (ProcessingException err) {
            NotCheckedException exception = new NotCheckedException("Could not parse Replacement Table from JSON: " + err.getMessage());
            log.error(exception.getMessage());
            throw exception;
        }
    }

    private static void checkReplacementTableName(ReplacementTable replacementTable, String expectedName) throws NotCheckedException {
        if (!StringUtils.equalsIgnoreCase(replacementTable.getName(), expectedName)) {
            String message = "ReplacementTable API returned an entry we weren't asking for.";
            log.error(message);
            throw new NotCheckedException(message);
        }
    }

    private static void checkReplacementTableContent(ReplacementTable replacementTable, Map<String, String> expectedContent) throws NotCheckedException {
        for (Map.Entry<String, String> entry : expectedContent.entrySet()) {
            if (!replacementTable.getKeys().get(entry.getKey()).equals(entry.getValue())) {
                String message = "ReplacementTable with the name " + replacementTable.getName() + " exists, but does not equal to the given one.";
                throw new NotCheckedException(message);
            }
        }
    }

    public static boolean replacementTableExist(ReplacementTable replacementTable) throws NotCheckedException {
        ReplacementTable receivedReplacementTable = findReplacementTable(replacementTable.getName());

        if (receivedReplacementTable == null) {
            return false;
        }
        checkReplacementTableName(receivedReplacementTable, replacementTable.getName());
        checkReplacementTableContent(receivedReplacementTable, replacementTable.getKeys());
        return true;
    }

    public static boolean replacementTableExist(String replacementTableName) throws NotCheckedException {
        ReplacementTable receivedReplacementTable = findReplacementTable(replacementTableName);

        if (receivedReplacementTable == null) {
            return false;
        }
        checkReplacementTableName(receivedReplacementTable, replacementTableName);
        return true;
    }

    public static Response updateReplacementTableResponse(ReplacementTable table, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_TABLE_JSON, nameTemplate(table.getName()), "PATCH", Entity.json(table));
        response.bufferEntity();
        return response;
    }

    public static Response updateReplacementTableResponse(ReplacementTable table, String token) {
        return updateReplacementTableResponse(table, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response deleteReplacementTableResponse(String tableName, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_REPLACEMENT_TABLE, nameTemplate(tableName), HttpMethod.DELETE);
        response.bufferEntity();
        return response;
    }

    public static Response deleteReplacementTableResponse(String tableName, String token) {
        return deleteReplacementTableResponse(tableName, new RequestSenderWithBearerAuthorization(token));
    }
}
