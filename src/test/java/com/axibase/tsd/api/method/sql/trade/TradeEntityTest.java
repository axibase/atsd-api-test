package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;

public class TradeEntityTest extends SqlTradeTest {
    private static final String SQL = "select open() {field} from atsd_trade where {instrument} and {condition} group by exchange, class, symbol";

    @BeforeClass
    public void prepareData() throws Exception {
        Entity entity = new Entity();
        entity.setName(entity());
        Entity entityTwo = new Entity();
        entityTwo.setName(entityTwo());
        Trade tradeOne = fromISOString("2020-06-15T10:21:49.123456Z").setPrice(BigDecimal.ONE);
        Trade tradeTwo = fromISOString("2020-06-15T12:21:49.123456Z").setSymbol(symbolTwo()).setPrice(BigDecimal.TEN);
        insert(tradeOne, tradeTwo);
        Checker.check(new EntityCheck(entity));
        Checker.check(new EntityCheck(entityTwo));

        entity.setTags(TestUtil.createTags("a", "10", "b", "abc"));
        entity.setLabel("entity1");

        EntityMethod.updateEntity(entity);

        entityTwo.setEnabled(false);
        entityTwo.setLabel("entity2");
        entityTwo.setTags(TestUtil.createTags("a", "15", "b", "def"));

        EntityMethod.updateEntity(entityTwo);

        Checker.check(new EntityCheck(entity));
        Checker.check(new EntityCheck(entityTwo));
    }

    @Test(dataProvider = "testData")
    public void test(TestConfig testConfig) throws Exception {
        String sql = testConfig.composeQuery(SQL);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        TestConfig[] data = {
                test("Test enabled").condition("entity.enabled").addExpected("1"),
                test("Test enabled equal operator").condition("entity.enabled = true").addExpected("1"),
                test("Test enabled not equal operator").condition("entity.enabled <> false").addExpected("1"),
                test("Test disabled not operator").condition("not entity.enabled").addExpected("10"),
                test("Test disabled not equal operator").condition("entity.enabled <> true").addExpected("10"),
                test("Test disabled equal operator").condition("entity.enabled = false").addExpected("10"),
                test("Test numeric tag condition").condition("entity.tags.a < 12").addExpected("1"),
                test("Test text tag condition").condition("entity.tags.b like 'd%'").addExpected("10"),
                test("Test entity label condition").condition("entity.label = 'entity2'").addExpected("10"),
                test("Test complex condition").condition("entity.enabled and entity.tags.a = 10 and entity.tags.b = 'abc'").addExpected("1"),
                test("Test columns").condition("entity.enabled").field("entity.enabled, entity.tags.a, entity.tags.b").addExpected("1", "true", "10", "abc")
        };
        return TestUtil.convertTo2DimArray(data);
    }


    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
            setVariable("instrument", classCondition());
            setVariable("field", "");
        }

        public TestConfig condition(String condition) {
            setVariable("condition", condition);
            return this;
        }

        public TestConfig field(String field) {
            setVariable("field", ", " + field);
            return this;
        }
    }
}