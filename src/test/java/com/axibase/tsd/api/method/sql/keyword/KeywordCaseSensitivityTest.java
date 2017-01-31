package com.axibase.tsd.api.method.sql.keyword;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class KeywordCaseSensitivityTest extends SqlTest {
    private static final String ENTITY_NAME = entity();
    private static final String METRIC1_NAME = metric();
    private static final String METRIC2_NAME = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();

        {
            Series series = new Series(ENTITY_NAME, METRIC1_NAME);
            series.addData(new Sample("2016-06-03T09:20:18.000Z", "1"));
            seriesList.add(series);
        }
        {
            Series series = new Series();
            series.setEntity(ENTITY_NAME);
            series.setMetric(METRIC2_NAME);
            series.addData(new Sample("2016-06-03T09:20:18.000Z", "2"));
            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @DataProvider(name = "keywordTestProvider")
    public Object[][] provideTestsDataForKeywordCaseSensitivityTest() {
        return new Object[][]{
                {"SELECT", "CASE", "WHEN", "VALUE", "THEN", "ELSE", "END", "AS", "ISNULL", "FROM", "OUTER", "JOIN",
                        "USING", "ENTITY", "WHERE", "IN", "AND", "OR", "IS", "NOT", "NULL", "LOOKUP", "CAST", "LIKE",
                        "REGEX", "BETWEEN", "WITH", "LAST_TIME", "INTERPOLATE", "LINEAR", "INNER", "NAN", "START_TIME",
                        "GROUP", "BY", "PERIOD", "HAVING", "ROW_NUMBER", "DESC", "ORDER", "ASC", "LIMIT", "OFFSET",
                        "OPTION", "ROW_MEMORY_THRESHOLD"},
                {"select", "case", "when", "value", "then", "else", "end", "as", "isnull", "from", "outer", "join",
                        "using", "entity", "where", "in", "and", "or", "is", "not", "null", "lookup", "cast", "like",
                        "regex", "between", "with", "last_time", "interpolate", "linear", "inner", "nan", "start_time",
                        "group", "by", "period", "having", "row_number", "desc", "order", "asc", "limit", "offset",
                        "option", "row_memory_threshold"},
                {"SeLeCt", "CaSe", "WhEn", "VaLuE", "tHeN", "eLsE", "eNd", "As", "IsNuLl", "FrOm", "OuTeR", "jOiN",
                        "uSiNg", "EnTiTy", "WhErE", "iN", "aNd", "Or", "Is", "NoT", "nUlL", "lOoKuP", "cAsT", "lIkE",
                        "rEgEx", "BeTwEeN", "wItH", "lAsT_TiMe", "InTeRpOlAtE", "lInEaR", "iNnEr", "NaN", "sTaRt_tImE",
                        "gRoUp", "By", "PeRiOd", "HaViNg", "RoW_NuMbEr", "DeSc", "OrDeR", "aSc", "LiMiT", "oFfSeT",
                        "oPtIoN", "rOw_mEmOrY_ThReShOlD"}
        };
    }

    /**
     * #3843
     */
    @Test(dataProvider = "keywordTestProvider")
    public void testBasicKeywordsForCaseSensitivity(String select, String CASE, String when, String value, String then,
           String ELSE, String end, String as, String isnull, String from, String outer, String join, String using,
           String entity, String where, String in, String and, String or, String is, String not, String NULL,
           String lookup, String cast, String like, String regex, String between, String with, String last_time,
           String interpolate, String linear, String inner, String nan, String start_time, String group, String by,
           String period, String having, String row_number, String desc, String order, String asc, String limit,
           String offset, String option, String row_memory_threshold) {

        String sqlQuery = String.format(
                        "    %1$s " +
                        "            %2$s " +
                        "    %3$s t1.%4$s > -1 %5$s COUNT(t1.%4$s) " +
                        "    %6$s COUNT(t1.%4$s) " +
                        "    %7$s %8$s 'word', %9$s(1, 1) " +
                        "    %10$s '%46$s' t1 %11$s %12$s %13$s %14$s '%47$s' t2 " +
                        "    %15$s t1.%14$s %16$s ('" + ENTITY_NAME + "') " +
                        "    %17$s t1.%4$s > 0 %18$s t1.%4$s < 500 " +
                        "    %17$s t1.%4$s %19$s %20$s %21$s " +
                        "    %17$s %22$s('a', %4$s) %19$s %21$s " +
                        "    %17$s %23$s(\"5\") = 5 " +
                        "    %17$s t1.%14$s %24$s '*' " +
                        "    %17$s t1.%14$s %25$s '.*' " +
                        "    %17$s t1.datetime %26$s '2000-01-01T00:00:00.000Z' %17$s '2020-01-01T00:00:00.000Z' " +
                        "    %27$s time >= %28$s - 10 * YEAR, %29$s (100 YEAR, %30$s, %31$s, %32$s, %33$s) " +
                        "    %34$s %35$s %36$s(5 YEAR), t1.%4$s " +
                        "    %37$s count(t1.%4$s) > 1 " +
                        "    %27$s %38$s(t1.%14$s %40$s %35$s t1.time %39$s) <= 100 " +
                        "    %40$s %35$s t1.%4$s %41$s " +
                        "    %42$s 10 %43$s 0 " +
                        "    %44$s (%45$s 10000)",
                select, CASE, when, value, then, ELSE, end, as, isnull, from,
                outer, join, using, entity, where, in, and, or, is, not,
                NULL, lookup, cast, like, regex, between, with, last_time, interpolate, linear,
                inner, nan, start_time, group, by, period, having, row_number, desc, order,
                asc, limit, offset, option, row_memory_threshold,
                METRIC1_NAME,
                METRIC2_NAME
        );

        String[][] expectedRows = {
        };

        assertSqlQueryRows("Keywords are case sensitive", expectedRows, sqlQuery);
    }

    @DataProvider(name = "aggregationsKeywordTestProvider")
    public Object[][] provideTestsDataForAggregationsKeywordCaseSensitivityTest() {
        return new Object[][]{
                {"SUM", "AVG", "MIN", "MAX", "COUNT", "COUNTER", "DELTA", "FIRST", "LAST", "MAX_VALUE_TIME", "MIN_VALUE_TIME", "PERCENTILE",
                        "STDDEV", "WAVG", "WTAVG"},
                {"sum", "avg", "min", "max", "count", "counter", "delta", "first", "last", "max_value_time", "min_value_time", "percentile",
                        "stddev", "wavg", "wtavg"},
                {"SuM", "aVg", "MiN", "mAx", "CoUnT", "cOuNtEr", "DeLtA", "fIrSt", "LaSt", "MaX_VaLuE_TiMe", "MiN_VaLuE_TiMe", "PeRcEnTiLe",
                        "StDdEv", "WaVg", "WtAvG"}
        };
    }

    /**
     * #3843
     */
    @Test(dataProvider = "aggregationsKeywordTestProvider")
    public void testAggregationsKeywordsForCaseSensitivity(String sum, String avg, String min, String max, String count,
           String counter, String delta, String first, String last, String max_value_time, String min_value_time,
           String percentile, String stddev, String wavg, String wtavg) {

        String sqlQuery = String.format(
                "select %1$s(value), %2$s(value), %3$s(value), %4$s(value), %5$s(value), %6$s(value), %7$s(value), " +
                        "%8$s(value), %9$s(value), %10$s(value), %11$s(value), %12$s(75, value), %13$s(value), " +
                        "%14$s(value), %15$s(value) " +
                        "from '%16$s'",
                sum, avg, min, max, count, counter, delta, first, last, max_value_time,
                min_value_time, percentile, stddev, wavg, wtavg,
                METRIC1_NAME
        );

        String[][] expectedRows = {
                {"1", "1", "1", "1", "1", "0", "0", "1", "1", "1464945618000", "1464945618000", "1", "0", "1", "1"}
        };

        assertSqlQueryRows("Aggregation keywords are case sensitive", expectedRows, sqlQuery);
    }

    @DataProvider(name = "mathematicalKeywordTestProvider")
    public Object[][] provideTestsDataForMathematicalKeywordCaseSensitivityTest() {
        return new Object[][]{
                {"ABS", "CEIL", "FLOOR", "ROUND", "MOD", "POWER", "EXP", "LN", "LOG", "SQRT"},
                {"abs", "ceil", "floor", "round", "mod", "power", "exp", "ln", "log", "sqrt"},
                {"AbS", "cEiL", "fLoOr", "RoUnD", "mOd", "PoWeR", "eXp", "Ln", "LoG", "sQrT"}
        };
    }

    /**
     * #3843
     */
    @Test(dataProvider = "mathematicalKeywordTestProvider")
    public void testMathematicalKeywordsForCaseSensitivity(String abs, String ceil, String floor, String round,
                                        String mod, String power, String exp, String ln, String log, String sqrt) {

        String sqlQuery = String.format(
                "SELECT %1$s(value), %2$s(value), %3$s(value), %4$s(value), %5$s(value, 3), " +
                        "%6$s(value, 2), %7$s(value), %8$s(value), %9$s(10, value), %10$s(value) " +
                        "FROM '%11$s'",
                abs, ceil, floor, round, mod, power, exp, ln, log, sqrt,
                METRIC1_NAME
        );

        String[][] expectedRows = {
                {"1", "1", "1", "1", "1", "1", "2.718281828459045", "0", "0", "1"}
        };

        assertSqlQueryRows("Mathematical keywords are case sensitive", expectedRows, sqlQuery);
    }

    @DataProvider(name = "stringKeywordTestProvider")
    public Object[][] provideTestsDataForStringKeywordCaseSensitivityTest() {
        return new Object[][]{
                {"UPPER", "LOWER", "REPLACE", "LENGTH", "CONCAT", "LOCATE", "SUBSTR"},
                {"upper", "lower", "replace", "length", "concat", "locate", "substr"},
                {"UpPeR", "lOwEr", "RePlAcE", "lEnGtH", "cOnCaT", "lOcAtE", "sUbStR"}
        };
    }

    /**
     * #3843
     */
    @Test(dataProvider = "stringKeywordTestProvider")
    public void testStringKeywordsForCaseSensitivity(String upper, String lower, String replace, String length,
                                                     String concat, String locate, String substr) {

        String sqlQuery = String.format(
                "SELECT %1$s('a'), %2$s('A'), %3$s('a', 'a', 'b'), %4$s('a'), %5$s('a', 'b'), %6$s('a', 'a'), " +
                        "%7$s('a', 1, 1) " +
                        "FROM '%8$s'",
                upper, lower, replace, length, concat, locate, substr,
                METRIC1_NAME
        );

        String[][] expectedRows = {
                {"A", "a", "b", "1", "ab", "1", "a"}
        };

        assertSqlQueryRows("String keywords are case sensitive", expectedRows, sqlQuery);
    }

    @DataProvider(name = "timeKeywordTestProvider")
    public Object[][] provideTestsDataForTimeKeywordCaseSensitivityTest() {
        return new Object[][]{
                {"NOW"}, {"NEXT_MINUTE"}, {"NEXT_HOUR"}, {"NEXT_DAY"}, {"TOMORROW"}, {"NEXT_WORKING_DAY"},
                {"NEXT_VACATION_DAY"}, {"NEXT_WEEK"}, {"LAST_WORKING_DAY"}, {"LAST_VACATION_DAY"},
                {"NEXT_MONTH"}, {"NEXT_QUARTER"}, {"NEXT_YEAR"},
                {"CURRENT_MINUTE"}, {"PREVIOUS_MINUTE"}, {"CURRENT_HOUR"}, {"PREVIOUS_HOUR"}, {"CURRENT_DAY"},
                {"TODAY"}, {"PREVIOUS_DAY"},
                {"YESTERDAY"}, {"PREVIOUS_WORKING_DAY"}, {"PREVIOUS_VACATION_DAY"}, {"FIRST_DAY"},
                {"FIRST_WORKING_DAY"}, {"FIRST_VACATION_DAY"}, {"CURRENT_WEEK"}, {"PREVIOUS_WEEK"},
                {"CURRENT_MONTH"}, {"PREVIOUS_MONTH"},
                {"CURRENT_QUARTER"}, {"PREVIOUS_QUARTER"}, {"CURRENT_YEAR"}, {"PREVIOUS_YEAR"},
                {"MONDAY"}, {"MON"}, {"TUESDAY"}, {"TUE"}, {"WEDNESDAY"}, {"WED"},
                {"THURSDAY"}, {"THU"}, {"FRIDAY"}, {"FRI"}, {"SATURDAY"}, {"SAT"}, {"SUNDAY"}, {"SUN"},
                {"now"}, {"next_minute"}, {"next_hour"}, {"next_day"}, {"tomorrow"}, {"next_working_day"},
                {"next_vacation_day"}, {"next_week"}, {"last_working_day"}, {"last_vacation_day"},
                {"next_month"}, {"next_quarter"}, {"next_year"},
                {"current_minute"}, {"previous_minute"}, {"current_hour"}, {"previous_hour"}, {"current_day"},
                {"today"}, {"previous_day"},
                {"yesterday"}, {"previous_working_day"}, {"previous_vacation_day"}, {"first_day"},
                {"first_working_day"}, {"first_vacation_day"}, {"current_week"}, {"previous_week"}, {"current_month"},
                {"previous_month"},
                {"current_quarter"}, {"previous_quarter"}, {"current_year"}, {"previous_year"},
                {"monday"}, {"mon"}, {"tuesday"}, {"tue"}, {"wednesday"}, {"wed"},
                {"thursday"}, {"thu"}, {"friday"}, {"fri"}, {"saturday"}, {"sat"}, {"sunday"}, {"sun"},
                {"nOw"}, {"NeXt_mInUtE"}, {"nExT_HoUr"}, {"NeXt_dAy"}, {"ToMoRrOw"}, {"NeXt_wOrKiNg_dAy"},
                {"NeXt_vAcAtIoN_DaY"}, {"nExT_WeEk"}, {"LaSt_wOrKiNg_dAy"}, {"LaSt_vAcAtIoN_DaY"},
                {"nExT_MoNtH"}, {"nExT_QuArTeR"}, {"nExT_YeAr"},
                {"CuRrEnT_MiNuTe"}, {"PrEvIoUs_mInUtE"}, {"cUrReNt_hOuR"}, {"pReViOuS_HoUr"}, {"CuRrEnT_DaY"},
                {"tOdAy"}, {"PrEvIoUs_dAy"},
                {"YeStErDaY"}, {"pReViOuS_WoRkInG_DaY"}, {"pReViOuS_VaCaTiOn_dAy"}, {"FiRsT_DaY"},
                {"fIrSt_wOrKiNg_dAy"}, {"FiRsT_VaCaTiOn_dAy"}, {"CuRrEnT_WeEk"}, {"PrEvIoUs_wEeK"}, {"cUrReNt_mOnTh"},
                {"PrEvIoUs_mOnTh"},
                {"CuRrEnT_QuArTeR"}, {"pReViOuS_QuArTeR"}, {"cUrReNt_yEaR"}, {"pReViOuS_YeAr"},
                {"MoNdAy"}, {"MoN"}, {"tUeSdAy"}, {"TuE"}, {"wEdNeSdAy"}, {"WeD"},
                {"tHuRsDaY"}, {"tHu"}, {"FrIdAy"}, {"FrI"}, {"sAtUrDaY"}, {"sAt"}, {"SuNdAy"}, {"SuN"}
        };
    }

    /**
     * #3843
     */
    @Test(dataProvider = "timeKeywordTestProvider")
    public void testTimeKeywordsForCaseSensitivity(String time) {

        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%1$s' " +
                        "WHERE datetime < %2$s OR datetime >= %2$s",
                METRIC1_NAME,
                time
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows("Time keywords are case sensitive", expectedRows, sqlQuery);
    }

    @DataProvider(name = "timeIntervalKeywordTestProvider")
    public Object[][] provideTestsDataForTimeIntervalKeywordCaseSensitivityTest() {
        return new Object[][]{
                {"MILLISECOND"}, {"SECOND"}, {"MINUTE"}, {"HOUR"}, {"DAY"}, {"WEEK"}, {"MONTH"}, {"QUARTER"}, {"YEAR"},
                {"millisecond"}, {"second"}, {"minute"}, {"hour"}, {"day"}, {"week"}, {"month"}, {"quarter"}, {"year"},
                {"mIlLiSeCoNd"}, {"SeCoNd"}, {"MiNuTe"}, {"HoUr"}, {"DaY"}, {"wEeK"}, {"mOnTh"}, {"QuArTeR"}, {"yEaR"}
        };
    }

    /**
     * #3843
     */
    @Test(dataProvider = "timeIntervalKeywordTestProvider")
    public void testTimeIntervalKeywordsForCaseSensitivity(String time) {

        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%1$s' " +
                        "WHERE datetime < now - %2$s OR datetime >= now - %2$s",
                METRIC1_NAME,
                time
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows("Time interval keywords are case sensitive", expectedRows, sqlQuery);
    }
}
