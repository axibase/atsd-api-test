package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TradeSender;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class TradeInstrumentInvalidConditionTest extends SqlTradeTest {
    private static final String TEST_PREFIX = "sql-trade-instrument-invalid-condition-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_ENTITY_GROUP1_NAME = TEST_ENTITY_NAME + "-group-1";
    private static final String TEST_ENTITY_GROUP2_NAME = TEST_ENTITY_NAME + "-group-2";
    private static final String CLASS_1 = TEST_PREFIX + "class1";
    private static final String CLASS_2 = TEST_PREFIX + "class11";
    private static final String CLASS_3 = TEST_PREFIX + "class3";
    private static final String SYMBOL_1 = TEST_PREFIX + "symbol1";
    private static final String SYMBOL_2 = TEST_PREFIX + "symbol11";
    private static final String SYMBOL_3 = TEST_PREFIX + "symbol3";


    @BeforeClass
    public void prepareData() throws Exception {
        EntityGroupMethod.createOrReplaceEntityGroup(new EntityGroup(TEST_ENTITY_GROUP1_NAME));
        EntityGroupMethod.createOrReplaceEntityGroup(new EntityGroup(TEST_ENTITY_GROUP2_NAME));
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP2_NAME, Collections.singletonList(TEST_PREFIX + "symbol_[class]"));

        long timestamp = Util.getUnixTime("2020-05-21T10:15:14Z");
        Trade trade1 = trade(timestamp).setClazz(CLASS_1).setSymbol(SYMBOL_1);
        Trade trade2 = trade(timestamp).setClazz(CLASS_2).setSymbol(SYMBOL_2);
        Trade trade3 = trade(timestamp).setClazz(CLASS_3).setSymbol(SYMBOL_3);
        TradeSender.send(trade1, trade2, trade3).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
    }

    @Test(dataProvider = "testData")
    public void test(String sql, String expectedErrorMessage, String description) {
        assertBadRequest(description, expectedErrorMessage, sql);
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][]{
                {
                        "select * from atsd_trade where symbol = 'non-existent-symbol'",
                        "TRADE_SYMBOL not found for name: 'NON-EXISTENT-SYMBOL'",
                        "Test non-existent symbol (equals operator)"
                },
                {
                        "select * from atsd_trade where symbol LIKE 'non-existent-symbol'",
                        "TRADE_SYMBOL not found for name: 'NON-EXISTENT-SYMBOL'",
                        "Test non-existent symbol (like operator)"
                },
                {
                        "select * from atsd_trade where symbol in ('non-existent-symbol', 'non-existent-symbol')",
                        "TRADE_SYMBOL not found for name: 'NON-EXISTENT-SYMBOL'",
                        "Test non-existent symbol (in operator)"
                },
                {
                        "select * from atsd_trade where class = 'non-existent-class'",
                        "TRADE_CLASS not found for name: 'NON-EXISTENT-CLASS'",
                        "Test non-existent class (equals operator)"
                },
                {
                        "select * from atsd_trade where class LIKE 'non-existent-class'",
                        "TRADE_CLASS not found for name: 'NON-EXISTENT-CLASS'",
                        "Test non-existent class (like operator)"
                },
                {
                        "select * from atsd_trade where class in ('non-existent-class', 'non-existent-class')",
                        "TRADE_CLASS not found for name: 'NON-EXISTENT-CLASS'",
                        "Test non-existent class (in operator)"
                },
                {
                        "select * from atsd_trade where exchange = 'non-existent-exchange'",
                        "TRADE_EXCHANGE not found for name: 'NON-EXISTENT-EXCHANGE'",
                        "Test non-existent exchange (equals operator)"
                },
                {
                        "select * from atsd_trade where exchange in ('non-existent-exchange', 'non-existent-exchange')",
                        "TRADE_EXCHANGE not found for name: 'NON-EXISTENT-EXCHANGE'",
                        "Test non-existent exchange (in operator)"
                },
                {
                        "select * from atsd_trade where symbol='a' and symbol='b'",
                        "Mutual exclusive conditions: symbol = 'a', symbol = 'b'",
                        "Test mutual exclusive symbol conditions"
                },
                {
                        "select * from atsd_trade where class='a' and class='b'",
                        "Mutual exclusive conditions: class = 'a', class = 'b'",
                        "Test mutual exclusive class conditions"
                },
                {
                        "select * from atsd_trade where exchange='a' and exchange='b'",
                        "Mutual exclusive conditions: exchange = 'a', exchange = 'b'",
                        "Test mutual exclusive exchange conditions"
                },
                {
                        "select * from atsd_trade where symbol LIKE 'non-existent-symbol%'",
                        "Nothing found by expression: symbol like 'non-existent-symbol%'",
                        "Test nothing found by symbol like condition"
                },
                {
                        "select * from atsd_trade where class LIKE 'non-existent-class%'",
                        "Nothing found by expression: class like 'non-existent-class%'",
                        "Test nothing found by class like condition"
                },
                {
                        String.format("select * from atsd_trade where is_entity_in_group(concat(symbol, '_[', class, ']'), '%s')", TEST_ENTITY_GROUP1_NAME),
                        "Nothing found by expression: is_entity_in_group(concat(symbol, '_[', class, ']'), '" + TEST_ENTITY_GROUP1_NAME + "')",
                        "Test nothing found by entity group expression"
                },
                {
                        "select * from atsd_trade where symbol like '" + TEST_PREFIX + "symbol1%' and symbol in ('" + TEST_PREFIX + "symbol3')",
                        "Mutual exclusive conditions: symbol like '" + TEST_PREFIX + "symbol1%', symbol in ('" + TEST_PREFIX + "symbol3')",
                        "Test mutual exclusive condition with like expression (symbol)"
                },
                {
                        "select * from atsd_trade where class like '" + TEST_PREFIX + "class1%' and class = '" + TEST_PREFIX + "class3'",
                        "Mutual exclusive conditions: class like '" + TEST_PREFIX + "class1%', class = '" + TEST_PREFIX + "class3'",
                        "Test mutual exclusive condition with like expression (class)"
                },
                {
                        String.format("select * from atsd_trade where class='a' and is_entity_in_group(concat(symbol, '_[', class, ']'), '%s')", TEST_ENTITY_GROUP2_NAME),
                        "Mutual exclusive class conditions",
                        "Test mutual exclusive condition with entity group expression (class)"
                },
                {
                        String.format("select * from atsd_trade where symbol='a' and is_entity_in_group(concat(symbol, '_[', class, ']'), '%s')", TEST_ENTITY_GROUP2_NAME),
                        "Mutual exclusive symbol conditions",
                        "Test mutual exclusive condition with entity group expression (symbol)"
                }

        };
    }
}