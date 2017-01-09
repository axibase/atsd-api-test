package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.function.interpolate.Alignment;
import com.axibase.tsd.api.model.sql.function.interpolate.Boundary;
import com.axibase.tsd.api.model.sql.function.interpolate.FillMode;
import com.axibase.tsd.api.model.sql.function.interpolate.InterpolateFunction;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;
import static java.util.Collections.singletonList;

public class ApplyTest extends SqlTest {
    private static final String APPLY_METRIC = metric();
    private static final Sample DEFAULT_SAMPLE = new Sample("2016-06-29T08:00:00.000Z", "0");


    @BeforeClass
    public static void prepareApplyData() throws Exception {
        Series series = new Series(entity(), APPLY_METRIC);
        series.addData(DEFAULT_SAMPLE);
        SeriesMethod.insertSeriesCheck(singletonList(series));
    }

    @DataProvider(name = "interpolateVariantsProvider")
    private static Object[][] interpolateParamVariants() {
        ArrayList<List<String>> params = new ArrayList<>();
        params.add(singletonList(new Interval(1, TimeUnit.MINUTE).toString()));
        params.add(enumStringLists(InterpolateFunction.class));
        params.add(enumStringLists(Boundary.class));
        params.add(enumStringLists(FillMode.class));
        params.add(enumStringLists(Alignment.class));
        Set<ArrayList<String>> variants = generateSetOfParams(params);
        StringBuilder builder = new StringBuilder();
        int j = 0;
        Object[][] result = new Object[variants.size()][1];
        for (ArrayList<String> variant : variants) {
            builder.setLength(0);
            int variantSize = variant.size();
            for (int i = 0; i < variantSize; i++) {
                String element = variant.get(i);
                if (element == null) {
                    continue;
                } else {
                    builder.append(String.format("%s, ", element));
                }
            }
            builder.setLength(builder.length() - 2);
            result[j][0] = builder.toString();
            j++;
        }
        return result;
    }

    private static Set<ArrayList<String>> generateSetOfParams(ArrayList<List<String>> enumLists) {
        Set<ArrayList<String>> result = new HashSet<>();
        Set<ArrayList<String>> previousMultiplication = new HashSet<>();
        ArrayList<String> list = new ArrayList<>();
        for (String param : enumLists.get(0)) {
            list.add(param);
        }
        previousMultiplication.add(list);
        for (int i = 1; i < enumLists.size(); i++) {
            Set<ArrayList<String>> multiplication = new HashSet<>();
            List<String> currentEnumList = enumLists.get(i);
            for (ArrayList<String> previousCombo : previousMultiplication) {
                for (String enumListItem : currentEnumList) {
                    ArrayList<String> newCombo = new ArrayList<>(previousCombo);
                    newCombo.add(enumListItem);
                    multiplication.add(newCombo);
                }
            }

            result.addAll(multiplication);
            previousMultiplication = multiplication;
        }
        return result;
    }

    private static <E extends Enum<E>> List<String> enumStringLists(Class<E> enumClass) {
        List<String> enumValues = new ArrayList<>();
        //enumValues.add(null);
        for (Enum e : enumClass.getEnumConstants()) {
            enumValues.add(e.toString());
        }
        return enumValues;
    }

    /**
     * #3388
     */
    @Test(dataProvider = "interpolateVariantsProvider")
    public void testApplyWithDateTimeInterval(String param) {
        String sqlQuery = String.format("SELECT * FROM '%s'%nWHERE datetime BETWEEN '2016-06-29T07:00:00.000Z' " +
                        "AND '2016-06-29T08:00:00.000Z'%nWITH INTERPOLATE(%s)",
                APPLY_METRIC, param
        );
        String assertMessage = String.format("Sql query: %n	%s should return 200 http code",
                sqlQuery);
        assertOkRequest(assertMessage, queryResponse(sqlQuery));
    }


    /**
     * #3462
     */
    @Test
    public void testNullSeries() throws Exception {
        Long startTime = Util.parseDate("2016-06-29T07:00:00.000Z'").getTime();
        Long endTime = Util.parseDate("2016-06-29T10:00:00.000Z").getTime();
        Series series = new Series(entity(), metric());
        for (long i = startTime; i < endTime; i += 60000) {
            series.addData(new Sample(Util.ISOFormat(i), (BigDecimal) null));
        }
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        String sqlQuery = String.format("SELECT * FROM '%s'%nWHERE time >= %d AND time < %d%nWITH INTERPOLATE(1 MINUTE, LINEAR)",
                series.getMetric(), startTime, endTime
        );
        String assertMessage = String.format(
                "Failed to apply interpolation to series with only null data.%n\tQuery: %s",
                sqlQuery
        );
        assertOkRequest(assertMessage, queryResponse(sqlQuery));
    }

}

