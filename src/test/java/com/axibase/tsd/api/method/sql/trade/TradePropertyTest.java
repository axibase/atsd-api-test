package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.method.trade.session_summary.TradeSessionSummaryMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.financial.*;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.util.InstrumentStatisticsSender;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TradePropertyTest extends SqlTradeTest {
    public static final String SECURITY_DEFINITIONS = "security_definitions";

    @BeforeClass
    public void prepareData() throws Exception {
        Entity entity = new Entity();
        entity.addTag("class_code", clazz());
        entity.setName(entity());
        Entity entityTwo = new Entity();
        entityTwo.addTag("class_code", clazz());
        entityTwo.setName(entityTwo());
        Entity entityThree = new Entity();
        entityThree.addTag("class_code", clazz());
        entityThree.setName(entityThree());

        Trade trade = fromISOString("2020-06-15T10:21:49.123456Z");
        Trade tradeTwo = fromISOString("2020-06-15T10:21:49.123456Z").setSymbol(symbolTwo());
        Trade tradeThree = fromISOString("2020-06-15T10:21:49.123456Z").setSymbol(symbolThree());
        insert(trade, tradeTwo, tradeThree);
        Checker.check(new EntityCheck(entity));
        Checker.check(new EntityCheck(entityTwo));
        Checker.check(new EntityCheck(entityThree));


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

        InstrumentStatistics instrumentStatistics =
                new InstrumentStatistics()
                        .setClazz(clazz())
                        .setSymbol(symbol())
                        .setTimestamp(Util.getUnixTime("2020-06-15T20:21:49.123Z"))
                        .setMicros(456)
                        .addValue("2", "1234") // numoffers
                        .addValue("5", "5060") // numbids
                        .addValue("10", "196.04") // bid
                ;
        InstrumentStatistics instrumentStatisticsTwo =
                new InstrumentStatistics()
                        .setClazz(clazz())
                        .setSymbol(symbolTwo())
                        .setTimestamp(Util.getUnixTime("2020-06-14T20:21:49.123Z"))
                        .setMicros(456)
                        .addValue("31", "10")    // auctvolume
                        .addValue("41", "10.23") // auctvalue
                        .addValue("44", "1.2") // prevprice
                        .addValue("37", "true") // assured
                        .addValue("32", "20210501") //prevdate
                        .addValue("35", "11:12:13") //plannedtime
                        .addValue("66", "2020-06-14T20:21:49.654321Z") //snapshot_start_datetime
                ;
        InstrumentStatistics instrumentStatisticsThree =
                new InstrumentStatistics()
                        .setClazz(clazz())
                        .setSymbol(symbolThree())
                        .setTimestamp(Util.getUnixTime("2020-06-16T20:21:49.123Z"))
                        .setMicros(456)
                        .addValue("31", "2")    // auctvolume
                        .addValue("41", "1.2") // auctvalue
                        .addValue("44", "2.5") // prevprice
                        .addValue("37", "false") // assured
                        .addValue("32", "20210502") //prevdate
                        .addValue("35", "14:15:16") // plannedtime
                        .addValue("66", "2020-06-16T20:21:49.123456Z") //snapshot_start_datetime
                ;

        InstrumentStatisticsSender
                .send(instrumentStatistics, instrumentStatisticsTwo, instrumentStatisticsThree)
                .waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);

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
    public void testEntityQueryWhereClause() {
        String sql = "select entity from atsd_entity where tags.class_code = '" + clazz() + "' and SEC_DEF.marketcode = 'FOND'";
        String[][] expected = new String[][]{
                {entity()}
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
                        "2020-06-14T10:21:49.123000Z", "2020-06-15T20:21:49.123456Z"
                }
        };
        assertSqlQueryRows(expected, sql);
    }

    @Test
    public void testEntityQueryStatFields() {
        String sql = "select stat.auctvolume, stat('auctvolume'), stat.auctvalue, stat('auctvalue'), " +
                "stat.prevprice, stat('prevprice'), stat.assured, stat('assured'), stat.prevdate, stat('prevdate'), " +
                "stat.plannedtime, stat('plannedtime'), stat.snapshot_start_datetime, stat('snapshot_start_datetime'), " +
                "ROUND(stat.auctvalue,0) AS auctvalue, 100*(stat.auctvalue/stat.prevprice-1) AS auct_to_prev " +
                "from atsd_entity where name in ('" + entityTwo() + "', '" + entityThree() + "') order by stat.auctvolume";
        String[][] expected = new String[][]{
                {
                        "2", "2", "1.2", "1.2", "2.5", "2.5", "false", "false", "20210502", "20210502", "14:15:16", "14:15:16", "2020-06-16T20:21:49.123456Z", "2020-06-16T20:21:49.123456Z", "1", "-52.0"
                },
                {
                        "10", "10", "10.23", "10.23", "1.2", "1.2", "true", "true", "20210501", "20210501", "11:12:13", "11:12:13", "2020-06-14T20:21:49.654321Z", "2020-06-14T20:21:49.654321Z", "10", "752.5"
                }
        };
        assertSqlQueryRows(expected, sql);
    }

}