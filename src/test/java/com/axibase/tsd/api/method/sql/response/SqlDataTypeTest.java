package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

public class SqlDataTypeTest extends SqlMethod {

    private static final String entityName = "tst-type";
    private static final String timestampString = "2017-01-12T00:00:00.000Z";

    private static final ValueDescription shortMetric =
            new ValueDescription("tst-metric-short", "10", DataType.SHORT);
    private static final ValueDescription integerMetric =
            new ValueDescription("tst-metric-integer", "10", DataType.INTEGER);
    private static final ValueDescription longMetric =
            new ValueDescription("tst-metric-long", "10", DataType.LONG);
    private static final ValueDescription floatMetric =
            new ValueDescription("tst-metric-float", "10.0", DataType.FLOAT);
    private static final ValueDescription doubleMetric =
            new ValueDescription("tst-metric-double", "10.0", DataType.DOUBLE);
    private static final ValueDescription decimalMetric =
            new ValueDescription("tst-metric-decimal", "10.0", DataType.DECIMAL);

    private static final ValueDescription[] metricsData = new ValueDescription[]{
            shortMetric, integerMetric, longMetric,
            floatMetric, doubleMetric, decimalMetric
    };

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        for (ValueDescription desc : metricsData) {
            MetricMethod.createOrReplaceMetricCheck(desc.getMetric());

            Series s = new Series();
            s.setMetric(desc.metricName);
            s.setEntity(entityName);
            s.addData(new Sample(timestampString, desc.value));
            seriesList.add(s);
        }

        /* Note: there are problems with number format in checker */
        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @DataProvider(name = "testSqlType")
    public static Object[][] metricsType() {
        return new Object[][]{
                {new StringArrayHolder(shortMetric.metricName, integerMetric.metricName, longMetric.metricName),
                        "bigint"},

                {new StringArrayHolder(shortMetric.metricName, integerMetric.metricName),
                        "integer"},

                {new StringArrayHolder(shortMetric.metricName),
                        "smallint"},

                {new StringArrayHolder(shortMetric.metricName, floatMetric.metricName),
                        "decimal"},

                {new StringArrayHolder(floatMetric.metricName),
                        "float"},

                {new StringArrayHolder(floatMetric.metricName, doubleMetric.metricName, decimalMetric.metricName),
                        "decimal"},

                {new StringArrayHolder(floatMetric.metricName, doubleMetric.metricName),
                        "double"},

                {new StringArrayHolder(longMetric.metricName, doubleMetric.metricName),
                        "decimal"},
        };
    }

    /**
     * #3773
     */
    @Test(dataProvider = "testSqlType")
    public void testDataType(StringArrayHolder arrayHolder, String expectedType) {
        String queryTemplate =
                "SELECT datetime, entity, metric, value\n" +
                        " FROM atsd_series\n" +
                        "WHERE metric IN (%s)";
        String sqlQuery = String.format(queryTemplate, arrayHolder.toString());
        StringTable table = queryResponse(sqlQuery).readEntity(StringTable.class);
        String actualType = table.getColumnMetaData(3).getDataType();

        assertEquals(expectedType, actualType);
    }


    private static class ValueDescription {
        public String metricName;
        public String value;
        public DataType type;

        ValueDescription(String metricName, String value, DataType type) {
            this.metricName = metricName;
            this.value = value;
            this.type = type;
        }

        Metric getMetric() {
            return new Metric(metricName).setDataType(type);
        }
    }

    private static class StringArrayHolder {
        private String[] array;

        StringArrayHolder(String... array) {
            this.array = array;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("'");
                sb.append(array[i]);
                sb.append("'");
            }
            return sb.toString();
        }
    }

}
