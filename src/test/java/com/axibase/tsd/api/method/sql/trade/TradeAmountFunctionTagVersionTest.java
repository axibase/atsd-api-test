package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TradeAmountFunctionTagVersionTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        Entity entity = new Entity();
        entity.setName(entity());
        entity.setTags(TestUtil.createTags("lot", "10"));
        EntityMethod.createOrReplaceEntity(entity);

        Thread.sleep(2000);

        entity.setTags(TestUtil.createTags("lot", "100"));
        EntityMethod.updateEntity(entity);

        List<Long> versions = EntityMethod.getEntityVersions(entity(), null, null);

        long timestampOne = versions.get(0) + 1000;
        long timestampTwo = versions.get(versions.size() - 1) + 1000;

        List<Trade> trades = new ArrayList<>();
        trades.add(trade(timestampOne).setPrice(new BigDecimal("3.5")).setQuantity(4));
        trades.add(trade(timestampTwo).setPrice(new BigDecimal("5")).setQuantity(10));
        insert(trades);
    }

    @Test
    public void test() {
        String query = "select amount() from atsd_trade where " + instrumentCondition() + " group by exchange, class, symbol, period( 1 second)";
        String[][] expectedRows = new String[][]{
                {"140"}, // 3.5 * 4 * 10
                {"5000"} // 5 * 10 * 100
        };
        assertSqlQueryRows(expectedRows, query);
    }
}