package com.axibase.tsd.api.method.replacementtable;


import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class ReplacementTableMethod extends BaseMethod{
    private static final String PATH = "/entities/lookup";
    private static final WebTarget resource = httpRootResource.path(PATH);

    public static void create(ReplacementTable table) throws UnsupportedEncodingException {
        Form form = new Form();
        form.param("lookupName", table.getName());

        form.param("items", squashMapIntoString(table.getMap()));

        form.param("oldName", "");

        form.param("save", "Save");

        Response response = resource
                .request()
                .post(Entity.form(form));
        response.bufferEntity();
    }

    private static String squashMapIntoString(Map<String, String> map){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
