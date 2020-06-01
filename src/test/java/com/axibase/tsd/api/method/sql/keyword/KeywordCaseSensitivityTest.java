package com.axibase.tsd.api.method.sql.keyword;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class KeywordCaseSensitivityTest extends SqlTest {
    private static final String ENTITY_NAME = entity();
    private static final String METRIC1_NAME = metric();
    private static final String METRIC2_NAME = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();

        Series series = new Series(ENTITY_NAME, METRIC1_NAME);
        series.addSamples(Sample.ofDateInteger("2016-06-03T09:20:18.000Z", 1));
        seriesList.add(series);

        series = new Series(ENTITY_NAME, METRIC2_NAME);
        series.addSamples(Sample.ofDateInteger("2016-06-03T09:20:18.000Z", 2));
        seriesList.add(series);

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @DataProvider(name = "keywordTestProvider")
    public Object[][] provideTestsDataForKeywordCaseSensitivityTest() {
        return new Object[][]{
                {"SELECT"}, {"CASE"}, {"WHEN"}, {"VALUE"}, {"THEN"}, {"ELSE"}, {"END"}, {"AS"}, {"ISNULL"}, {"FROM"},
                {"OUTER"}, {"JOIN"}, {"USING"}, {"ENTITY"}, {"WHERE"}, {"IN"}, {"AND"}, {"OR"}, {"IS"}, {"NOT"},
                {"NULL"}, {"LOOKUP"}, {"CAST"}, {"LIKE"}, {"REGEX"}, {"BETWEEN"}, {"WITH"}, {"LAST_TIME"},
                {"INTERPOLATE"}, {"LINEAR"}, {"INNER"}, {"NAN"}, {"START_TIME"}, {"GROUP"}, {"BY"}, {"PERIOD"},
                {"HAVING"}, {"ROW_NUMBER"}, {"DESC"}, {"ORDER"}, {"ASC"}, {"LIMIT"}, {"OFFSET"},
                {"OPTION"}, {"ROW_MEMORY_THRESHOLD"},
                {"SeLeCt"}, {"CaSe"}, {"WhEn"}, {"VaLuE"}, {"tHeN"}, {"eLsE"}, {"eNd"}, {"As"}, {"IsNuLl"}, {"FrOm"},
                {"OuTeR"}, {"jOiN"}, {"uSiNg"}, {"EnTiTy"}, {"WhErE"}, {"iN"}, {"aNd"}, {"Or"}, {"Is"}, {"NoT"},
                {"nUlL"}, {"lOoKuP"}, {"cAsT"}, {"lIkE"}, {"rEgEx"}, {"BeTwEeN"}, {"wItH"}, {"lAsT_TiMe"},
                {"InTeRpOlAtE"}, {"lInEaR"}, {"iNnEr"}, {"NaN"}, {"sTaRt_tImE"}, {"gRoUp"}, {"By"}, {"PeRiOd"},
                {"HaViNg"}, {"RoW_NuMbEr"}, {"DeSc"}, {"OrDeR"}, {"aSc"}, {"LiMiT"}, {"oFfSeT"},
                {"oPtIoN"}, {"rOw_mEmOrY_ThReShOlD"}
        };
    }

    @Issue("3843")
    @Test
    public void testBasicKeywordsForCaseSensitivityInLowerCase() {
        String sqlQuery = String.format(
                "select case when t1.value > -1 then COUNT(t1.value)     else nan     end as \"word\", isnull(1, 1) " +
                        "FROM \"%1$s\" t1 outer join using entity \"%2$s\" t2 " +
                        "where t1.entity in ('" + ENTITY_NAME + "') " +
                        "and t1.value > 0 or t1.value < 500 " +
                        "and t1.value is not null     and lookup('a', t1.value) is null " +
                        "and cast('5') = 5 " +
                        "and t1.entity like '*' " +
                        "and t1.entity regex '.*' " +
                        "and t1.datetime between '2000-01-01T00:00:00.000Z' and '2020-01-01T00:00:00.000Z' " +
                        "with time >= last_time - 10 * YEAR, interpolate (1 YEAR, linear, inner, false, start_time) " +
                        "group by t1.period(1 YEAR), t1.value " +
                        "having count(t1.value) >= 1 " +
                        "with row_number(t1.entity order by t1.time desc) <= 100 " +
                        "order by t1.value asc " +
                        "limit 10 offset 0 " +
                        "option (row_memory_threshold 10000)",
                METRIC1_NAME,
                METRIC2_NAME
        );


        assertOkRequest(queryResponse(sqlQuery));
    }

    @Issue("3843")
    @Test(dataProvider = "keywordTestProvider", dependsOnMethods = {"testBasicKeywordsForCaseSensitivityInLowerCase"})
    public void testBasicKeywordsForCaseSensitivity(String keyword) {
        String sqlQuery = String.format(
                "select case when t1.value > -1 then COUNT(t1.value) else nan end as \"word\", isnull(1, 1) " +
                        "FROM \"%1$s\" t1 outer join using entity \"%2$s\" t2 " +
                        "where t1.entity in ('" + ENTITY_NAME + "') " +
                        "and t1.value > 0 or t1.value < 500 " +
                        "and t1.value is not null     and lookup('a', t1.value) is null " +
                        "and cast('5') = 5 " +
                        "and t1.entity like '*' " +
                        "and t1.entity regex '.*' " +
                        "and t1.datetime between '2000-01-01T00:00:00.000Z' and '2020-01-01T00:00:00.000Z' " +
                        "with time >= last_time - 10 * YEAR, interpolate (1 YEAR, linear, inner, false, start_time) " +
                        "group by t1.period(1 YEAR), t1.value " +
                        "having count(t1.value) >= 1 " +
                        "with row_number(t1.entity order by t1.time desc) <= 100 " +
                        "order by t1.value asc " +
                        "limit 10 offset 0 " +
                        "option (row_memory_threshold 10000)",
                METRIC1_NAME,
                METRIC2_NAME
        ).replaceAll(keyword.toLowerCase(), keyword);

        assertOkRequest(queryResponse(sqlQuery));
    }

    @DataProvider(name = "aggregationsKeywordTestProvider")
    public Object[][] provideTestsDataForAggregationsKeywordCaseSensitivityTest() {
        return new Object[][]{
                {"SUM"}, {"AVG"}, {"MIN"}, {"MAX"}, {"COUNT"}, {"COUNTER"}, {"DELTA"}, {"FIRST"}, {"LAST"},
                {"MAX_VALUE_TIME"}, {"MIN_VALUE_TIME"}, {"PERCENTILE"}, {"STDDEV"}, {"WAVG"}, {"WTAVG"},
                {"SuM"}, {"aVg"}, {"MiN"}, {"mAx"}, {"CoUnT"}, {"cOuNtEr"}, {"DeLtA"}, {"fIrSt"}, {"LaSt"},
                {"MaX_VaLuE_TiMe"}, {"MiN_VaLuE_TiMe"}, {"PeRcEnTiLe"}, {"StDdEv"}, {"WaVg"}, {"WtAvG"}
        };
    }

    @Issue("3843")
    @Test
    public void testAggregationsKeywordsForCaseSensitivityInLowerCase() {
        String sqlQuery = String.format(
                "select sum(value), avg(value), min(value), max(value), count(value), counter(value), delta(value), " +
                        "first(value), last(value), max_value_time(value), min_value_time(value), " +
                        "percentile(75, value), stddev(value), wavg(value), wtavg(value) " +
                        "FROM \"%s\"",
                METRIC1_NAME
        );

        String[][] expectedRows = {
                {"1", "1", "1", "1", "1", "NaN", "NaN", "1", "1", "1464945618000", "1464945618000", "1", "0", "1.0", "1.0"}
        };

        assertSqlQueryRows("There's a problem with aggregations keywords in lowercase", expectedRows, sqlQuery);
    }

    @Issue("3843")
    @Test(dataProvider = "aggregationsKeywordTestProvider",
            dependsOnMethods = {"testAggregationsKeywordsForCaseSensitivityInLowerCase"})
    public void testAggregationsKeywordsForCaseSensitivity(String keyword) {
        String sqlQuery = String.format(
                "select sum(value), avg(value), min(value), max(value), count(value), counter(value), delta(value), " +
                        "first(value), last(value), max_value_time(value), min_value_time(value), " +
                        "percentile(75, value), stddev(value), wavg(value), wtavg(value) " +
                        "FROM \"%s\"",
                METRIC1_NAME
        ).replaceAll(keyword.toLowerCase(), keyword);

        String[][] expectedRows = {
                {"1", "1", "1", "1", "1", "NaN", "NaN", "1", "1", "1464945618000", "1464945618000", "1", "0", "1.0", "1.0"}
        };

        assertSqlQueryRows("Aggregation keyword " + keyword + " is case sensitive", expectedRows, sqlQuery);
    }

    @DataProvider(name = "mathematicalKeywordTestProvider")
    public Object[][] provideTestsDataForMathematicalKeywordCaseSensitivityTest() {
        return new Object[][]{
                {"ABS"}, {"CEIL"}, {"FLOOR"}, {"ROUND"}, {"MOD"}, {"POWER"}, {"EXP"}, {"LN"}, {"LOG"}, {"SQRT"},
                {"AbS"}, {"cEiL"}, {"fLoOr"}, {"RoUnD"}, {"mOd"}, {"PoWeR"}, {"eXp"}, {"Ln"}, {"LoG"}, {"sQrT"}
        };
    }

    @Issue("3843")
    @Test
    public void testMathematicalKeywordsForCaseSensitivityInLowerCase() {
        String sqlQuery = String.format(
                "select abs(value), ceil(value), floor(value), round(value), mod(value, 3), power(value, 2), " +
                        "exp(value), ln(value), log(10, value), sqrt(value) " +
                        "FROM \"%s\"",
                METRIC1_NAME
        );

        String[][] expectedRows = {
                {"1", "1", "1", "1", "1", "1", "2.718281828459045", "0", "0", "1"}
        };

        assertSqlQueryRows("There's a problem with mathematical keywords in lowercase", expectedRows, sqlQuery);
    }

    @Issue("3843")
    @Test(dataProvider = "mathematicalKeywordTestProvider",
            dependsOnMethods = {"testMathematicalKeywordsForCaseSensitivityInLowerCase"})
    public void testMathematicalKeywordsForCaseSensitivity(String keyword) {
        String sqlQuery = String.format(
                "select abs(value), ceil(value), floor(value), round(value), mod(value, 3), power(value, 2), " +
                        "exp(value), ln(value), log(10, value), sqrt(value) " +
                        "FROM \"%s\"",
                METRIC1_NAME
        ).replaceAll(keyword.toLowerCase(), keyword);

        String[][] expectedRows = {
                {"1", "1", "1", "1", "1", "1", "2.718281828459045", "0", "0", "1"}
        };

        assertSqlQueryRows("Mathematical keyword " + keyword + " is case sensitive", expectedRows, sqlQuery);
    }

    @DataProvider(name = "stringKeywordTestProvider")
    public Object[][] provideTestsDataForStringKeywordCaseSensitivityTest() {
        return new Object[][]{
                {"UPPER"}, {"LOWER"}, {"REPLACE"}, {"LENGTH"}, {"CONCAT"}, {"LOCATE"}, {"SUBSTR"},
                {"UpPeR"}, {"lOwEr"}, {"RePlAcE"}, {"lEnGtH"}, {"cOnCaT"}, {"lOcAtE"}, {"sUbStR"}
        };
    }

    @Issue("3843")
    @Test
    public void testStringKeywordsForCaseSensitivityInLowerCase() {
        String sqlQuery = String.format(
                "select upper('a'), lower('A'), replace('a', 'a', 'b'), length('a'), concat('a', 'b'), " +
                        "locate('a', 'a'), substr('a', 1, 1) " +
                        "FROM \"%s\"",
                METRIC1_NAME
        );

        String[][] expectedRows = {
                {"A", "a", "b", "1", "ab", "1", "a"}
        };

        assertSqlQueryRows("There's a problem with string keywords in lowercase", expectedRows, sqlQuery);
    }

    @Issue("3843")
    @Test(dataProvider = "stringKeywordTestProvider",
            dependsOnMethods = {"testStringKeywordsForCaseSensitivityInLowerCase"})
    public void testStringKeywordsForCaseSensitivity(String keyword) {
        String sqlQuery = String.format(
                "select upper('a'), lower('A'), replace('a', 'a', 'b'), length('a'), concat('a', 'b'), " +
                        "locate('a', 'a'), substr('a', 1, 1) " +
                        "FROM \"%s\"",
                METRIC1_NAME
        ).replaceAll(keyword.toLowerCase(), keyword);

        String[][] expectedRows = {
                {"A", "a", "b", "1", "ab", "1", "a"}
        };

        assertSqlQueryRows("String keyword " + keyword + " is case sensitive", expectedRows, sqlQuery);
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

    @Issue("3843")
    @Test(dataProvider = "timeKeywordTestProvider")
    public void testTimeKeywordsForCaseSensitivity(String keyword) {

        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM \"%1$s\" " +
                        "WHERE datetime < %2$s OR datetime >= %2$s",
                METRIC1_NAME,
                keyword
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows("Time keyword " + keyword + " is case sensitive", expectedRows, sqlQuery);
    }

    @DataProvider(name = "timeIntervalKeywordTestProvider")
    public Object[][] provideTestsDataForTimeIntervalKeywordCaseSensitivityTest() {
        return new Object[][]{
                {"MILLISECOND"}, {"SECOND"}, {"MINUTE"}, {"HOUR"}, {"DAY"}, {"WEEK"}, {"MONTH"}, {"QUARTER"}, {"YEAR"},
                {"millisecond"}, {"second"}, {"minute"}, {"hour"}, {"day"}, {"week"}, {"month"}, {"quarter"}, {"year"},
                {"mIlLiSeCoNd"}, {"SeCoNd"}, {"MiNuTe"}, {"HoUr"}, {"DaY"}, {"wEeK"}, {"mOnTh"}, {"QuArTeR"}, {"yEaR"}
        };
    }

    @Issue("3843")
    @Test(dataProvider = "timeIntervalKeywordTestProvider")
    public void testTimeIntervalKeywordsForCaseSensitivity(String keyword) {

        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM \"%1$s\" " +
                        "WHERE datetime < now - %2$s OR datetime >= now - %2$s",
                METRIC1_NAME,
                keyword
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows("Time interval keyword " + keyword + " is case sensitive", expectedRows, sqlQuery);
    }
}
