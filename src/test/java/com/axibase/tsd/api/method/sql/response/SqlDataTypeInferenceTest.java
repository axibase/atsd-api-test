package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

public class SqlDataTypeInferenceTest extends SqlMethod {

    private static Map<DataType, String> typeToName = new HashMap<>();

    @BeforeClass
    public static void prepareData() throws Exception {
        final String entityName = TestUtil.TestNames.entity();
        Registry.Entity.register(entityName);

        List<Series> seriesList = new ArrayList<>();
        for (DataType type : DataType.values()) {
            Metric metric = Mocks.metric();
            metric.setDataType(type);
            metric.setEnabled(true);
            typeToName.put(type, metric.getName());
            MetricMethod.createOrReplaceMetricCheck(metric);

            Series s = new Series();
            s.setMetric(metric.getName());
            s.setEntity(entityName);
            s.addData(Mocks.SAMPLE);
            seriesList.add(s);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @DataProvider(name = "dataTypeInferenceProvider")
    public static Object[][] provideTypeInferenceTestData() {
        return new Object[][]{
                {new DataTypeArray(DataType.SHORT, DataType.INTEGER, DataType.LONG),
                        "bigint"},

                {new DataTypeArray(DataType.SHORT, DataType.INTEGER),
                        "integer"},

                {new DataTypeArray(DataType.SHORT),
                        "smallint"},

                {new DataTypeArray(DataType.SHORT, DataType.FLOAT),
                        "decimal"},

                {new DataTypeArray(DataType.FLOAT),
                        "float"},

                {new DataTypeArray(DataType.FLOAT, DataType.DOUBLE, DataType.DECIMAL),
                        "decimal"},

                {new DataTypeArray(DataType.FLOAT, DataType.DOUBLE),
                        "double"},

                {new DataTypeArray(DataType.LONG, DataType.DOUBLE),
                        "decimal"},
        };
    }

    /**
     * #3773
     */
    @Test(dataProvider = "dataTypeInferenceProvider")
    public void testSqlDataTypeInference(DataTypeArray selectedTypes, String expectedType) {
        String queryTemplate = "SELECT value FROM atsd_series WHERE metric IN (%s)";
        String sqlQuery = String.format(queryTemplate, selectedTypes.toString());
        StringTable table = queryTable(sqlQuery);
        String actualType = table.getColumnMetaData(0).getDataType();

        String assertMessageTemplate = "Inferred type expected to be %s, but got %s for types [%s] ";
        assertEquals(String.format(assertMessageTemplate, expectedType, actualType, selectedTypes.toString()),
                expectedType, actualType);
    }

    private static class DataTypeArray {
        private DataType[] dataTypes;

        DataTypeArray(DataType... dataTypes) {
            this.dataTypes = dataTypes;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dataTypes.length; i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append("'").append(typeToName.get(dataTypes[i])).append("'");
            }
            return sb.toString();
        }
    }

}
