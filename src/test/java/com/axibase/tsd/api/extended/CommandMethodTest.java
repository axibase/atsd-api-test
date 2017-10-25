package com.axibase.tsd.api.extended;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.NotCheckedException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

public class CommandMethodTest extends CommandMethod {

    public static void assertSeries(Series series) {
        try {
            Checker.check(new SeriesCheck(Collections.singletonList(series)));
        } catch (NotCheckedException e) {
            fail("Fail to execute series query");
        }
    }

    protected void assertTextDataEquals(Series series, String msgTemplate) throws Exception {
        assertSeries(series);

        List<String> actualData = getActualData(series);
        String expected = series.getData().get(0).getText();
        assertEquals(msgTemplate,
                expected, actualData.toString().replace("[", "").replace("]", ""));
    }

    private static List<String> getActualData(Series series) throws Exception {
        List<Series> actualSeriesList = SeriesMethod.querySeriesAsList(new SeriesQuery(series));
        return actualSeriesList.stream()
                .flatMap(actualSeries -> actualSeries.getData().stream())
                .map(Sample::getText)
                .sorted()
                .collect(Collectors.toList());
    }
}
