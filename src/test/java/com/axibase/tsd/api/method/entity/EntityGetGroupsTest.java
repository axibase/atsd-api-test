package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGetGroupsTest extends EntityMethod {


    @Test //1278
    public void testURLGetGroupsWhitespace() throws Exception {
        Entity entity = new Entity("getgroupsentity 1");
        assertEquals(BAD_REQUEST.getStatusCode(), getGroups(entity.getName()).getStatus());
    }


    @Test //1278
    public void testURLGetGroupsSlash() throws Exception {
        Entity entity = new Entity("getgroups/entity-2");
        createOrReplaceEntityCheck(entity);
        checkUrlencodedPathHandledSuccessfullyOnGetGroups(entity);

    }

    @Test //1278
    public void testURLGetGroupsCyrillic() throws Exception {
        Entity entity = new Entity("getgroupsйёentity-3");
        createOrReplaceEntityCheck(entity);
        checkUrlencodedPathHandledSuccessfullyOnGetGroups(entity);
    }

    private void checkUrlencodedPathHandledSuccessfullyOnGetGroups(final Entity entity) throws Exception {
        Response response = getGroups(entity.getName());
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertTrue(compareJsonString("[]", formatToJsonString(response)));
    }
}
