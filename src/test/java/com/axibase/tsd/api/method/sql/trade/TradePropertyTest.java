package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.financial.Trade;
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
        Property property = new Property();
        property.setType(SECURITY_DEFINITIONS);
        property.setEntity(entity.getName());
        property.setDate("2020-06-14T10:21:49.123Z");
        Map<String, String> tags = new HashMap<>();
        tags.put("roundlot", "10");
        tags.put("product", "5");
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

    private String entity() {
        return symbol() + "_[" + clazz() + "]";
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
}