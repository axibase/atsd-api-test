package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;


public class TradeInstrumentEntityConditionTest extends SqlTradeTest {
    private final String TEST_PREFIX = clazz().toLowerCase();
    private final String TEST_ENTITY_GROUP1_NAME = TEST_PREFIX + "-group-1";
    private final String TEST_ENTITY_GROUP2_NAME = TEST_PREFIX + "-group-2";
    private final String CLASS_1 = (TEST_PREFIX + "class1").toUpperCase();
    private final String CLASS_2 = (TEST_PREFIX + "class11").toUpperCase();
    private final String SYMBOL_1 = (TEST_PREFIX + "symbol1").toUpperCase();
    private final String SYMBOL_2 = (TEST_PREFIX + "symbol11").toUpperCase();
    private final String SYMBOL_3 = (TEST_PREFIX + "symbol3").toUpperCase();

    @BeforeClass
    public void prepareData() throws Exception {
        String entityOne = (SYMBOL_1 + "_[" + CLASS_1 + "]").toLowerCase();
        String entityTwo = (SYMBOL_2 + "_[" + CLASS_1 + "]").toLowerCase();
        String entityThree = (SYMBOL_3 + "_[" + CLASS_2 + "]").toLowerCase();

        EntityGroupMethod.createOrReplaceEntityGroup(new EntityGroup(TEST_ENTITY_GROUP1_NAME));
        EntityGroupMethod.createOrReplaceEntityGroup(new EntityGroup(TEST_ENTITY_GROUP2_NAME));

        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP1_NAME, Arrays.asList(entityOne, entityTwo));
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP2_NAME, Arrays.asList(entityOne, entityThree));

        long timestamp = Util.getUnixTime("2020-05-21T10:15:14Z");
        Trade trade1 = trade(timestamp).setClazz(CLASS_1).setSymbol(SYMBOL_1);
        Trade trade2 = trade(timestamp).setClazz(CLASS_1).setSymbol(SYMBOL_2);
        Trade trade3 = trade(timestamp).setClazz(CLASS_2).setSymbol(SYMBOL_3);
        insert(trade1, trade2, trade3);
    }

    @Test(dataProvider = "testData")
    public void test(String sql, String[][] expected) throws Exception {
        assertSqlQueryRows("Unexpected result for query: " + sql, expected, sql);
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][]{
                {"select class, symbol from atsd_trade where is_entity_in_group(to_entity(symbol, class), '" + TEST_ENTITY_GROUP1_NAME + "')",
                        new String[][]{{CLASS_1, SYMBOL_1}, {CLASS_1, SYMBOL_2}}
                },
                {"select class, symbol from atsd_trade where class='" + CLASS_1 + "' and is_entity_in_group(to_entity(symbol, class), '" + TEST_ENTITY_GROUP1_NAME + "')",
                        new String[][]{{CLASS_1, SYMBOL_1}, {CLASS_1, SYMBOL_2}}
                },
                {"select class, symbol from atsd_trade where class='" + CLASS_1 + "' and symbol='" + SYMBOL_2 + "' and is_entity_in_group(to_entity(symbol, class), '" + TEST_ENTITY_GROUP1_NAME + "')",
                        new String[][]{{CLASS_1, SYMBOL_2}}
                },
                {"select class, symbol from atsd_trade where class='" + CLASS_1 + "' and symbol LIKE '" + TEST_PREFIX.toUpperCase() + "%' and is_entity_in_group(to_entity(symbol, class), '" + TEST_ENTITY_GROUP1_NAME + "')",
                        new String[][]{{CLASS_1, SYMBOL_1}, {CLASS_1, SYMBOL_2}}
                },
                {"select class, symbol from atsd_trade where symbol LIKE '" + TEST_PREFIX.toUpperCase() + "SYMBOL1%' and is_entity_in_group(to_entity(symbol, class), '" + TEST_ENTITY_GROUP2_NAME + "')",
                        new String[][]{{CLASS_1, SYMBOL_1}}
                },
                {"select class, symbol from atsd_trade where class='" + CLASS_1 + "' and is_entity_in_group(to_entity(symbol, class), '" + TEST_ENTITY_GROUP2_NAME + "')",
                        new String[][]{{CLASS_1, SYMBOL_1}}
                },

        };
    }
}