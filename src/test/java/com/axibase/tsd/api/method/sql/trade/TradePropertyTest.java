package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.method.trade.session_summary.TradeSessionSummaryMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.model.financial.TradeSessionStage;
import com.axibase.tsd.api.model.financial.TradeSessionSummary;
import com.axibase.tsd.api.model.financial.TradeSessionType;
import com.axibase.tsd.api.model.property.Property;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TradePropertyTest extends SqlTradeTest {
    public static final String STATISTICS = "statistics";
    public static final String SECURITY_DEFINITIONS = "security_definitions";

    @BeforeClass
    public void prepareData() throws Exception {
        Entity entity = new Entity();
        entity.setName(entity());
        Trade trade = fromISOString("2020-06-15T10:21:49.123456Z");
        insert(trade);
        Checker.check(new EntityCheck(entity));
        trade = fromISOString("2020-06-15T10:21:49.123456Z").setExchange("MOEX");
        insert(trade);
        TradeSessionSummary sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.MORNING, TradeSessionStage.N, "2020-09-10T10:15:20Z");
        sessionSummary.addTag("rptseq", "2");
        sessionSummary.addTag("offer", "10.5");
        sessionSummary.addTag("assured", "true");
        sessionSummary.addTag("action", "test");
        sessionSummary.addTag("starttime", "10:15:20");
        sessionSummary.addTag("snapshot_datetime", "2020-09-10T10:15:20Z");
        TradeSessionSummaryMethod.importStatistics(sessionSummary);

        Property property = new Property();
        property.setType(SECURITY_DEFINITIONS);
        property.setEntity(entity.getName());
        property.setDate("2020-06-14T10:21:49.123Z");
        Map<String, String> tags = new HashMap<>();
        tags.put("roundlot", "10");
        tags.put("product", "5");
        tags.put("marketcode", "FOND");
        property.setTags(tags);
        PropertyMethod.insertPropertyCheck(property);
        property = new Property();
        property.setType(SECURITY_DEFINITIONS);
        property.setEntity(entity.getName());
        property.setDate("2020-06-15T10:21:49.123Z");
        property.setKey(Collections.singletonMap("a", "b"));
        tags = new HashMap<>();
        tags.put("roundlot", "20");
        tags.put("product", "17");
        tags.put("test", "8");
        property.setTags(tags);
        PropertyMethod.insertPropertyCheck(property);

        property = new Property();
        property.setType(STATISTICS);
        property.setEntity(entity.getName());
        property.setDate("2020-06-15T20:21:49.123Z");
        tags = new HashMap<>();
        tags.put("numoffers", "1234");
        tags.put("numbids", "5060");
        tags.put("bid", "196.04");
        property.setTags(tags);
        PropertyMethod.insertPropertyCheck(property);
        property = new Property();
        property.setType(STATISTICS);
        property.setEntity(entity.getName());
        property.setKey(Collections.singletonMap("c", "d"));
        property.setDate("2020-06-15T20:25:49.123Z");
        tags = new HashMap<>();
        tags.put("numoffers", "0");
        tags.put("numbids", "0");
        tags.put("bid", "0");
        property.setTags(tags);
        PropertyMethod.insertPropertyCheck(property);

    }

    @Test
    public void test() throws Exception {
        String sql = "select sec_def.roundlot, sec_def('product'), stat.numbids, stat('numoffers'), stat.bid " +
                "from atsd_trade where " + instrumentCondition();
        String[][] expected = new String[][]{
                {
                        "10", "5", "5060", "1234", "196.04"
                }
        };
        assertSqlQueryRows(expected, sql);
    }

    @Test
    public void testWhereClause() throws Exception {
        String sql = "SELECT symbol, exchange\n" +
                " FROM atsd_trade\n" +
                "WHERE symbol = '" + symbol() + "' AND exchange='MOEX' AND SEC_DEF.marketcode IN ('FOND', 'FNDT')";
        String[][] expected = new String[][]{
                {symbol(), "MOEX"}
        };
        assertSqlQueryRows(expected, sql);
    }

    @Test
    public void testEntityQuery() throws Exception {
        String sql = "select sec_def.roundlot, sec_def('product'), stat.numbids, stat('numoffers'), stat.bid " +
                "from atsd_entity where name = '" + entity() + "'";
        String[][] expected = new String[][]{
                {
                        "10", "5", "5060", "1234", "196.04"
                }
        };
        assertSqlQueryRows(expected, sql);
    }

    @Test
    public void testSessionSummaryQuery() {
        String sql = "select sec_def.roundlot, sec_def('product'), stat.numbids, stat('numoffers'), stat.bid " +
                "from atsd_session_summary where class = '" + clazz() + "' and symbol = '" + symbol() + "'";
        String[][] expected = new String[][]{
                {
                        "10", "5", "5060", "1234", "196.04"
                }
        };
        assertSqlQueryRows(expected, sql);
    }

    @Test
    public void testDateTimeField() {
        String sql = "select sec_def.datetime, stat.datetime from atsd_trade where " + instrumentCondition();
        String[][] expected = new String[][]{
                {
                        "2020-06-14T10:21:49.123Z", "2020-06-15T20:21:49.123Z"
                }
        };
        assertSqlQueryRows(expected, sql);
    }
}