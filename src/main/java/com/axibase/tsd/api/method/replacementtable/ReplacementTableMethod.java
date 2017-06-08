package com.axibase.tsd.api.method.replacementtable;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;
import com.axibase.tsd.api.util.NotCheckedException;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.OK;

public class ReplacementTableMethod extends BaseMethod {
    private static final Logger logger = LoggerFactory.getLogger(ReplacementTableMethod.class);
    private static final String METHOD_REPLACEMENT_TABLE_NEW = "/replacement-tables/new";
    private static final String METHOD_REPLACEMENT_TABLE_LIST = "/replacement-tables/";
    private static final String METHOD_REPLACEMENT_TABLE = "/replacement-tables/{replacementTable}";

    private static Response createResponse(ReplacementTable table) {
        MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        parameters.add("lookupName", table.getName());
        parameters.add("items", squashMapIntoString(table.getMap()));
        parameters.add("oldName", "");
        parameters.add("save", "Save");
        Form form = new Form(parameters);

        Response response = httpRootResource.path(METHOD_REPLACEMENT_TABLE_NEW)
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .request()
                .post(Entity.form(form));
        response.bufferEntity();

        return response;
    }

    public static void createCheck(ReplacementTable table) {
        Response response = createResponse(table);

        // It's confusing, but we get code 302 -- FOUND - Redirect, only if table was created
        if (response.getStatus() != Response.Status.FOUND.getStatusCode()) {
            String errorMessage = "Wasn't able to create a replacement table, Status Info is " + response.getStatusInfo();
            logger.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    private static String squashMapIntoString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    private static Response getReplacementTablesResponse() {
        Response response = httpRootResource.path(METHOD_REPLACEMENT_TABLE_LIST).request().get();
        response.bufferEntity();
        return response;
    }

    public static boolean replacementTableExist(String replacementTable) throws NotCheckedException {
        replacementTable = replacementTable.replace(" ", "_").toLowerCase();
        final Response response = ReplacementTableMethod.getReplacementTablesResponse();
        if (response.getStatus() != OK.getStatusCode()) {
            throw new NotCheckedException("Fail to execute replacement table query");
        }

        InputStream bodyStream = (InputStream) response.getEntity();
        String body;
        try {
            body = IOUtils.toString(bodyStream, "UTF-8");
        }
        catch (IOException ignored) {
            throw new IllegalStateException("Error in decoding stream to string");
        }

        Document doc = Jsoup.parse(body);
        Element table = doc.select("table").get(0);
        Elements trs = table.select("tr");
        for (Element tr : trs) {
            Elements tds = tr.select("td");
            for (Element td : tds) {
                if (td.text().contains(replacementTable)) {
                    return true;
                }
            }
        }

        return false;
    }
}
