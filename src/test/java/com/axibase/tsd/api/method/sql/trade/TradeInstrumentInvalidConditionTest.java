package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import org.testng.annotations.Test;

public class TradeInstrumentInvalidConditionTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-trade-instrument-invalid-condition-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_ENTITY_GROUP1_NAME = TEST_ENTITY_NAME + "-group-1";

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
        assertBadSqlRequest("Nothing found by expression: is_entity_in_group(concat(symbol, '_[', class, ']'), 'sql-trade-instrument-invalid-condition-entity-group-1')", sqlQuery);
    }

}