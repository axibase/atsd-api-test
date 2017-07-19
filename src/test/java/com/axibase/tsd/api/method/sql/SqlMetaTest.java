package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.model.sql.ColumnMetaData;
import org.testng.Assert;

public abstract class SqlMetaTest extends SqlMetaMethod {

    public static void assertSqlMetaNamesAndTypes(
            String message, String[] expectedNames, String[] expectedTypes, String query) {
        ColumnMetaData[] columnMeta = queryMetaData(query).getColumnsMeta();

        if (expectedNames.length != expectedTypes.length)
            throw new IllegalArgumentException("Incorrect expected data - unequal length");

        Assert.assertEquals(columnMeta.length, expectedNames.length,
                "Unequal size for expected and actual columns count: ");
        for (int i = 0; i < expectedNames.length; i++) {
            Assert.assertEquals(columnMeta[i].getName(), expectedNames[i],
                    "Unexpected column name for column index: " + (i + 1));
            Assert.assertEquals(columnMeta[i].getDataType(), expectedTypes[i],
                    "Unexpected column type for column index: " + (i + 1));
        }
    }
}
