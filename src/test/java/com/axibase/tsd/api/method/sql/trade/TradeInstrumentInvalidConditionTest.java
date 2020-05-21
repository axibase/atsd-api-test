package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TradeSender;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class TradeInstrumentInvalidConditionTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-trade-instrument-invalid-condition-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_ENTITY_GROUP1_NAME = TEST_ENTITY_NAME + "-group-1";
    private static final String TEST_ENTITY_GROUP2_NAME = TEST_ENTITY_NAME + "-group-2";
    private static final String EXCHANGE = TEST_PREFIX + "exchange";
    private static final String CLASS_1 = TEST_PREFIX + "class1";
    private static final String CLASS_2 = TEST_PREFIX + "class11";
    private static final String CLASS_3 = TEST_PREFIX + "class3";
    private static final String SYMBOL_1 = TEST_PREFIX + "symbol1";
    private static final String SYMBOL_2 = TEST_PREFIX + "symbol11";
    private static final String SYMBOL_3 = TEST_PREFIX + "symbol3";


    @BeforeClass
    public void prepareData() throws Exception {
        long timestamp = ZonedDateTime.parse("2020-05-21T10:15:14Z").toInstant().toEpochMilli();
        Trade trade1 = new Trade(EXCHANGE, CLASS_1, SYMBOL_1, 1, timestamp, BigDecimal.ONE, 1);
        Trade trade2 = new Trade(EXCHANGE, CLASS_2, SYMBOL_2, 2, timestamp, BigDecimal.ONE, 1);
        Trade trade3 = new Trade(EXCHANGE, CLASS_3, SYMBOL_3, 3, timestamp, BigDecimal.ONE, 1);
        TradeSender.send(trade1, trade2, trade3).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
    }

    @Test
    public void testNonExistentSymbol() throws Exception {
        String sqlQuery = "select * from atsd_trade where symbol = 'non-existent-symbol'";
        assertBadSqlRequest("TRADE_SYMBOL not found for name: 'NON-EXISTENT-SYMBOL'", sqlQuery);

        sqlQuery = "select * from atsd_trade where symbol LIKE 'non-existent-symbol'";
        assertBadSqlRequest("TRADE_SYMBOL not found for name: 'NON-EXISTENT-SYMBOL'", sqlQuery);

        sqlQuery = "select * from atsd_trade where symbol in ('non-existent-symbol', 'non-existent-symbol')";
        assertBadSqlRequest("TRADE_SYMBOL not found for name: 'NON-EXISTENT-SYMBOL'", sqlQuery);
    }

    @Test
    public void testNonExistentClass() throws Exception {
        String sqlQuery = "select * from atsd_trade where class = 'non-existent-class'";
        assertBadSqlRequest("TRADE_CLASS not found for name: 'NON-EXISTENT-CLASS'", sqlQuery);

        sqlQuery = "select * from atsd_trade where class LIKE 'non-existent-class'";
        assertBadSqlRequest("TRADE_CLASS not found for name: 'NON-EXISTENT-CLASS'", sqlQuery);

        sqlQuery = "select * from atsd_trade where class in ('non-existent-class', 'non-existent-class')";
        assertBadSqlRequest("TRADE_CLASS not found for name: 'NON-EXISTENT-CLASS'", sqlQuery);
    }

    @Test
    public void testNonExistentExchange() throws Exception {
        String sqlQuery = "select * from atsd_trade where exchange = 'non-existent-exchange'";
        assertBadSqlRequest("TRADE_EXCHANGE not found for name: 'NON-EXISTENT-EXCHANGE'", sqlQuery);

        sqlQuery = "select * from atsd_trade where exchange in ('non-existent-exchange', 'non-existent-exchange')";
        assertBadSqlRequest("TRADE_EXCHANGE not found for name: 'NON-EXISTENT-EXCHANGE'", sqlQuery);
    }

    @Test
    public void testMutualExclusiveCondition() throws Exception {
        String sqlQuery = "select * from atsd_trade where symbol='a' and symbol='b'";
        assertBadSqlRequest("Mutual exclusive conditions: symbol = 'a', symbol = 'b'", sqlQuery);

        sqlQuery = "select * from atsd_trade where class='a' and class='b'";
        assertBadSqlRequest("Mutual exclusive conditions: class = 'a', class = 'b'", sqlQuery);

        sqlQuery = "select * from atsd_trade where exchange='a' and exchange='b'";
        assertBadSqlRequest("Mutual exclusive conditions: exchange = 'a', exchange = 'b'", sqlQuery);
    }

    @Test
    public void testNothingFoundByLikeCondition() throws Exception {
        String sqlQuery = "select * from atsd_trade where symbol LIKE 'non-existent-symbol%'";
        assertBadSqlRequest("Nothing found by expression: symbol like 'non-existent-symbol%'", sqlQuery);

        sqlQuery = "select * from atsd_trade where class LIKE 'non-existent-class%'";
        assertBadSqlRequest("Nothing found by expression: class like 'non-existent-class%'", sqlQuery);
    }

    @Test
    public void testNothingFoundByEntityGroupExpression() throws Exception {
        EntityGroupMethod.createOrReplaceEntityGroup(new EntityGroup(TEST_ENTITY_GROUP1_NAME));
        String sqlQuery = String.format("select * from atsd_trade where is_entity_in_group(concat(symbol, '_[', class, ']'), '%s')", TEST_ENTITY_GROUP1_NAME);
        assertBadSqlRequest("Nothing found by expression: is_entity_in_group(concat(symbol, '_[', class, ']'), '" + TEST_ENTITY_GROUP1_NAME + "')", sqlQuery);
    }

    @Test
    public void testMutualExclusiveConditionLikeExpression() throws Exception {
        String sqlQuery = "select * from atsd_trade where symbol like '" + TEST_PREFIX + "symbol1%' and symbol in ('" + TEST_PREFIX + "symbol3')";
        assertBadSqlRequest("Mutual exclusive conditions: symbol like '" + TEST_PREFIX + "symbol1%', symbol in ('" + TEST_PREFIX + "symbol3')", sqlQuery);

        sqlQuery = "select * from atsd_trade where class like '" + TEST_PREFIX + "class1%' and class = '" + TEST_PREFIX + "class3'";
        assertBadSqlRequest("Mutual exclusive conditions: class like '" + TEST_PREFIX + "class1%', class = '" + TEST_PREFIX + "class3'", sqlQuery);
    }

    @Test
    public void testMutualExclusiveConditionEntityGroup() throws Exception {
        EntityGroupMethod.createOrReplaceEntityGroup(new EntityGroup(TEST_ENTITY_GROUP2_NAME));
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP2_NAME, Collections.singletonList(TEST_PREFIX + "symbol_[class]"));
        String sqlQuery = String.format("select * from atsd_trade where class='a' and is_entity_in_group(concat(symbol, '_[', class, ']'), '%s')", TEST_ENTITY_GROUP2_NAME);
        assertBadSqlRequest("Mutual exclusive class conditions", sqlQuery);

        sqlQuery = String.format("select * from atsd_trade where symbol='a' and is_entity_in_group(concat(symbol, '_[', class, ']'), '%s')", TEST_ENTITY_GROUP2_NAME);
        assertBadSqlRequest("Mutual exclusive symbol conditions", sqlQuery);
    }


}