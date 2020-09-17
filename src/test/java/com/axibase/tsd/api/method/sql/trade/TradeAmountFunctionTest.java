package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TradeAmountFunctionTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        Entity entity = new Entity();
        entity.setName(entity());
        Entity entityTwo = new Entity();
        entityTwo.setName(entityTwo());
        Entity entityThree = new Entity();
        entityThree.setName(entityThree());

        List<Trade> trades = new ArrayList<>();
        trades.add(fromISOString("2020-06-15T10:21:49.123456Z").setPrice(new BigDecimal("3.5")).setQuantity(4));
        trades.add(fromISOString("2020-06-15T12:21:49.123456Z").setPrice(new BigDecimal("5")).setQuantity(10));

        trades.add(fromISOString("2020-06-15T10:21:49.123456Z").setPrice(new BigDecimal("10")).setQuantity(2).setSymbol(symbolTwo()));
        trades.add(fromISOString("2020-06-15T12:21:49.123456Z").setPrice(new BigDecimal("5.43")).setQuantity(8).setSymbol(symbolTwo()));

        trades.add(fromISOString("2020-06-15T10:21:49.123456Z").setPrice(new BigDecimal("10")).setQuantity(2).setSymbol(symbolThree()));

        insert(trades);

        Checker.check(new EntityCheck(entity));
        Checker.check(new EntityCheck(entityTwo));
        Checker.check(new EntityCheck(entityThree));

        entity.setTags(TestUtil.createTags("lot", "2"));
        entityThree.setTags(TestUtil.createTags("lot", "abc"));

        EntityMethod.updateEntity(entity);
        EntityMethod.updateEntity(entityThree);
    }

    @Test
    public void test() throws Exception {
        String sql = "select amount() from atsd_trade where " + classCondition() + " group by exchange, class, symbol order by exchange, class, symbol";
        String[][] expected = new String[][]{
                {"128"}, // (3.5 * 4 + 5 * 10) * 2
                {"63.44"}, // (10 * 2 + 5.43 * 8) * 1
                {"NaN"}
        };
        assertSqlQueryRows(expected, sql);
    }
}