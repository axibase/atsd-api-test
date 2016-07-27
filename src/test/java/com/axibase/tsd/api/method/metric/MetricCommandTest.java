package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

import org.json.JSONException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Grigoriy Romanov.
 */
public class MetricCommandTest extends MetricMethod {

    /*
     * Global metric name for tests which works with existing metric.
     */

    String existingMetricName = "metric-3088";

    /* #3088 */

    /*
     * Test sets all possible parameters in a command.
     * Also creates metric which will use like existing in other tests. Its reason why uderscore in method name.
     */
    @Test
    public void _testOverall() throws Exception {

        DataType dataType = DataType.FLOAT;
        Boolean versioned = false;
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("m-tag-1", "m-val-1");

        final Metric metric = new Metric(existingMetricName);
        metric.setVersioned(versioned);
        metric.setDataType(dataType);
        metric.setTags(tags);

        StringBuffer sb = new StringBuffer("metric");
        sb.append(" m:").append(metric.getName());
        sb.append(" p:").append(metric.getDataType().toString().toLowerCase());
        sb.append(" v:").append(metric.getVersioned().toString());

        for(Map.Entry<String, String> entry : tags.entrySet()) {
            sb.append(" t:").append(entry.getKey() + "=" + entry.getValue());
        }

        tcpSender.send(sb.toString(), 1000);

        Thread.sleep(1000);
        Response response = queryMetric(existingMetricName);

        if (response.getStatus() != OK.getStatusCode()) {
            response.close();
            throw new IOException("Fail to execute metric command.");
        }

        String actual = response.readEntity(String.class);
        String expected = "{\"name\":\"" + metric.getName() + "\",\"dataType\":\"" + metric.getDataType().toString() +
                "\",\"versioned\":" + metric.getVersioned() + ",\"tags\":{";

        for(Map.Entry<String,String> entry : metric.getTags().entrySet()) {
            expected += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"," ;
        }
        expected = expected.substring(0, expected.length()-1);
        expected += "}}";

        if(!compareJsonString(expected, actual, false)) {
            throw new IOException("Test is not passed.");
        }
    }

    /* #3088 */
    @Test
    /*
     * Existing metric updates own tags by tags with random names.
     */
    public void testAddingNewTags() throws Exception {

        Map<String, String> tags = new HashMap<String, String>();
        for(int i = 0; i < 3; i++) {
            int randomInt = (int)(Math.random() * 1000 + 2);
            tags.put("m-tag-"+Integer.toString(randomInt), "m-val-"+Integer.toString(randomInt));
        }

        StringBuffer sb = new StringBuffer("metric");
        sb.append(" m:").append(existingMetricName);

        for(Map.Entry<String, String> entry : tags.entrySet()) {
            sb.append(" t:").append(entry.getKey() + "=" + entry.getValue());
        }

        tcpSender.send(sb.toString(), 1000);

        Thread.sleep(1000);
        Response response = queryMetric(existingMetricName);

        if (response.getStatus() != OK.getStatusCode()) {
            response.close();
            throw new IOException("Fail to execute metric command.");
        }

        String actual = response.readEntity(String.class);
        String expected = "{\"name\":\"" + existingMetricName + "\",\"tags\":{";

        for(Map.Entry<String,String> entry : tags.entrySet()) {
            expected += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"," ;
        }
        expected = expected.substring(0, expected.length()-1);
        expected += "}}";

        if(!compareJsonString(expected, actual, false)) {
            throw new IOException("Test is not passed.");
        }
    }

    /* #3088 */
    @Test
    /*
     * Datatype of existing methon changes to LONG, versioning is on.
     */
    public void testChangeDataTypeAndVersioned() throws Exception {

        DataType dataType = DataType.LONG;
        Boolean versioned = true;

        StringBuffer sb = new StringBuffer("metric");
        sb.append(" m:").append(existingMetricName);
        sb.append(" v:").append(versioned.toString());
        sb.append(" p:").append(dataType.toString().toLowerCase());

        tcpSender.send(sb.toString(), 1000);

        Thread.sleep(1000);
        Response response = queryMetric(existingMetricName);

        if (response.getStatus() != OK.getStatusCode()) {
            response.close();
            throw new IOException("Fail to execute metric command.");
        }

        String actual = response.readEntity(String.class);
        String expected = "{\"name\":\"" + existingMetricName + "\",\"dataType\":\"" + dataType.toString() +
                "\",\"versioned\":" + versioned +"}";

        if(!compareJsonString(expected, actual, false)) {
            throw new IOException("Test is not passed.");
        }

    }

    /* #3088 */

    @Test
    public void testCreateNewMetricWithTags() throws Exception {

        String name = "metric-1";
        Map<String, String> tags = new HashMap<String, String>();

        for(int i = 0; i < 3; i++) {
            int randomInt = (int)(Math.random() * 100 + 2);
            tags.put("m-tag-"+Integer.toString(randomInt), "m-val-"+Integer.toString(randomInt));
        }

        Metric metric = new Metric(name);
        metric.setTags(tags);

        StringBuffer sb = new StringBuffer("metric");
        sb.append(" m:").append(metric.getName());

        for(Map.Entry<String, String> entry : metric.getTags().entrySet()) {
            sb.append(" t:").append(entry.getKey() + "=" + entry.getValue());
        }

        tcpSender.send(sb.toString(), 1000);

        Thread.sleep(1000);
        Response response = queryMetric(name);

        if (response.getStatus() != OK.getStatusCode()) {
            response.close();
            throw new IOException("Fail to execute metric command.");
        }

        String actual = response.readEntity(String.class);
        String expected = "{\"name\":\"" + metric.getName() + "\",\"tags\":{";

        for(Map.Entry<String,String> entry : metric.getTags().entrySet()) {
            expected += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"," ;
        }

        expected = expected.substring(0, expected.length()-1);
        expected += "}}";

        if(!compareJsonString(expected, actual, false)) {
            throw new IOException("Test is not passed.");
        }
    }

    /* #3088 */
    @Test
    public void testCreateNewLongVersionedMetric() throws Exception {

        String name = "metric-2";
        Boolean versioned = true;
        DataType dataType = DataType.LONG;

        Metric metric = new Metric(name);
        metric.setVersioned(versioned);
        metric.setDataType(dataType);

        StringBuffer sb = new StringBuffer("metric");
        sb.append(" m:").append(metric.getName());
        sb.append(" v:").append(metric.getVersioned());
        sb.append(" p:").append(dataType.toString().toLowerCase());

        tcpSender.send(sb.toString(), 1000);

        Thread.sleep(1000);
        Response response = queryMetric(name);

        if (response.getStatus() != OK.getStatusCode()) {
            response.close();
            throw new IOException("Fail to execute metric command.");
        }

        String actual = response.readEntity(String.class);
        String expected = "{\"name\":\"" + metric.getName() + "\",\"versioned\":" +
                metric.getVersioned().toString() +",\"dataType\":\"" + metric.getDataType().toString() +
                "\"}}";

        if(!compareJsonString(expected, actual, false)) {
            throw new IOException("Test is not passed.");
        }
    }

    /* #3088 */

    /*
     * Test checks metric with whitespaced tag creates or not.
     */
    @Test
    public void testMalformedTag() throws Exception {
        String name = "metric-malformed-1";
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("m-ws tag","m-val-404");

        Metric metric = new Metric(name);
        metric.setTags(tags);

        StringBuffer sb =  new StringBuffer("metric");
        sb.append(" m:").append(metric.getName());

        for(Map.Entry<String, String> entry : metric.getTags().entrySet()) {
            sb.append(" t:").append(entry.getKey() + "=" + entry.getValue());
        }

        tcpSender.send(sb.toString(), 1000);

        Thread.sleep(1000);
        Response response = queryMetric(name);

        if (response.getStatus() != NOT_FOUND.getStatusCode()) {
            response.close();
            throw new IOException("Fail to execute metric command.");
        }

    }
}
