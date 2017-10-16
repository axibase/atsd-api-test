package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

public class SelectAliasSameAsColumnTest extends SqlTest {
    private String TEST_METRIC;

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = Mocks.series();
        TEST_METRIC = series.getMetric();

        SeriesMethod.insertSeriesCheck(series);
    }

    @Issue("4353")
    @Test(description = "Test query with column aliases are equals to column names works correct")
    public void testColumnAliasesSameAsColumnNames() {
        String query = String.format("SELECT\n" +
                "\tentity AS entity,\n" +
                "\tentity.name AS \"entity.name\",\n" +
                "    entity.label AS \"entity.label\",\n" +
                "    entity.groups AS \"entity.groups\",\n" +
                "    entity.enabled AS \"entity.enabled\",\n" +
                "    entity.interpolate AS \"entity.interpolate\",\n" +
                "    entity.timezone AS \"entity.timezone\",\n" +
                "    entity.tags AS \"entity.tags\",\n" +
                "    \n" +
                "    metric AS metric,\n" +
                "    metric.name AS \"metric.name\",\n" +
                "    metric.label AS \"metric.label\",\n" +
                "    metric.description AS \"metric.description\",\n" +
                "    metric.tags AS \"metric.tags\",\n" +
                "    metric.datatype AS \"metric.datatype\",\n" +
                "    metric.interpolate AS \"metric.interpolate\",\n" +
                "    metric.units AS \"metric.units\",\n" +
                "    metric.timezone AS \"metric.timezone\",\n" +
                "    metric.timePrecision AS \"metric.timePrecision\",\n" +
                "    metric.enabled AS \"metric.enabled\",\n" +
                "    metric.persistent AS \"metric.persistent\",\n" +
                "    metric.filter AS \"metric.filter\",\n" +
                "    metric.lastInsertTime AS \"metric.lastInsertTime\",\n" +
                "    metric.retentionIntervalDays AS \"metric.retentionIntervalDays\",\n" +
                "    metric.versioning AS \"metric.versioning\",\n" +
                "    metric.minValue AS \"metric.minValue\",\n" +
                "    metric.maxValue AS \"metric.maxValue\",\n" +
                "    metric.invalidValueAction AS \"metric.invalidValueAction\",\n" +
                "    \n" +
                "\tdatetime AS datetime,\n" +
                "    time AS time,\n" +
                "    value AS \"value\"\n" +
                "FROM \"%s\"",
                TEST_METRIC);

        Response response = queryResponse(query);
        assertOkRequest("Query with ", response);
    }

    @Issue("4353")
    @Test(description = "Test query with ambiguous (entity.name AS name, metric.name AS name) column aliases " +
            "are equals to column names works correct")
    public void testAmbiguousColumnAliasesSameAsColumnNames() {
        String query = String.format("SELECT\n" +
                        "\tentity AS entity,\n" +
                        "\tentity.name AS \"name\",\n" +
                        "    entity.label AS \"label\",\n" +
                        "    entity.groups AS \"groups\",\n" +
                        "    entity.enabled AS \"enabled\",\n" +
                        "    entity.interpolate AS \"interpolate\",\n" +
                        "    entity.timezone AS \"timezone\",\n" +
                        "    entity.tags AS \"tags\",\n" +
                        "    \n" +
                        "    metric AS metric,\n" +
                        "    metric.name AS \"name\",\n" +
                        "    metric.label AS \"label\",\n" +
                        "    metric.description AS \"description\",\n" +
                        "    metric.tags AS \"tags\",\n" +
                        "    metric.datatype AS \"datatype\",\n" +
                        "    metric.interpolate AS \"interpolate\",\n" +
                        "    metric.units AS \"units\",\n" +
                        "    metric.timezone AS \"timezone\",\n" +
                        "    metric.timePrecision AS \"timePrecision\",\n" +
                        "    metric.enabled AS \"enabled\",\n" +
                        "    metric.persistent AS \"persistent\",\n" +
                        "    metric.filter AS \"filter\",\n" +
                        "    metric.lastInsertTime AS \"lastInsertTime\",\n" +
                        "    metric.retentionIntervalDays AS \"retentionIntervalDays\",\n" +
                        "    metric.versioning AS \"versioning\",\n" +
                        "    metric.minValue AS \"minValue\",\n" +
                        "    metric.maxValue AS \"maxValue\",\n" +
                        "    metric.invalidValueAction AS \"invalidValueAction\",\n" +
                        "    \n" +
                        "\tdatetime AS datetime,\n" +
                        "    time AS time,\n" +
                        "    value AS \"value\"\n" +
                        "FROM \"%s\"",
                TEST_METRIC);

        Response response = queryResponse(query);
        assertOkRequest(response);
    }
}
