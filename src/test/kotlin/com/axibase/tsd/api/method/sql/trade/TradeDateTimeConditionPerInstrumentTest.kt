package com.axibase.tsd.api.method.sql.trade

import com.axibase.tsd.api.util.Mocks
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TradeDateTimeConditionPerInstrumentTest : SqlTradeTest() {
    private val classTwo = Mocks.tradeClass();

    private fun classTwo(): String {
        return classTwo;
    }

    @BeforeClass
    @Throws(Exception::class)
    fun prepareData() {
        val trades = fromISO(
                "2020-03-22T10:00:01.123450Z",
                "2020-03-22T10:00:09.123451Z",
                "2020-03-22T10:00:49.123452Z",
                "2020-03-22T10:00:55.123453Z",
                "2020-03-22T11:01:05.123454Z",
                "2020-03-22T11:01:14.123455Z",
                "2020-03-22T11:01:29.123456Z",
                "2020-03-22T11:01:49.123457Z",
                "2020-03-22T11:01:50.123458Z")

        trades.add(fromISOString("2020-03-22T11:01:05.123454Z").setSymbol(symbolTwo()))
        trades.add(fromISOString("2020-03-22T11:01:14.123455Z").setSymbol(symbolTwo()))
        trades.add(fromISOString("2020-03-22T11:01:29.123456Z").setSymbol(symbolTwo()))
        trades.add(fromISOString("2020-03-22T11:01:49.123457Z").setSymbol(symbolTwo()))
        trades.add(fromISOString("2020-03-22T11:01:50.123458Z").setSymbol(symbolTwo()))

        trades.add(fromISOString("2020-03-22T12:01:05.123454Z").setSymbol(symbolTwo()))
        trades.add(fromISOString("2020-03-22T12:01:14.123455Z").setSymbol(symbolTwo()))
        trades.add(fromISOString("2020-03-22T12:01:29.123456Z").setSymbol(symbolTwo()))
        trades.add(fromISOString("2020-03-22T12:01:49.123457Z").setSymbol(symbolTwo()))
        trades.add(fromISOString("2020-03-22T12:01:50.123458Z").setSymbol(symbolTwo()))

        trades.add(fromISOString("2020-03-22T10:00:01.123450Z").setClazz(classTwo()).setSymbol(symbolThree()))
        trades.add(fromISOString("2020-03-22T10:00:09.123451Z").setClazz(classTwo()).setSymbol(symbolThree()))
        trades.add(fromISOString("2020-03-22T10:00:49.123452Z").setClazz(classTwo()).setSymbol(symbolThree()))
        trades.add(fromISOString("2020-03-22T10:00:55.123453Z").setClazz(classTwo()).setSymbol(symbolThree()))
        trades.add(fromISOString("2020-03-22T11:01:05.123454Z").setClazz(classTwo()).setSymbol(symbolThree()))

        insert(trades);
    }

    @Test(dataProvider = "testData")
    fun testSearch(where: String, expected: Array<Array<String>>) {
        val sql = "select symbol, datetime from atsd_trade where $where order by symbol, datetime";
        assertSqlQueryRows(where, expected, sql);
    }

    @Test(dataProvider = "testDataAggregate")
    fun testAggregate(where: String, expected: Array<Array<String>>) {
        val sql = "select symbol, count(*), min(datetime), max(datetime) from atsd_trade where $where group by exchange, class, symbol order by symbol";
        assertSqlQueryRows(where, expected, sql);
    }

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("exchange = '${exchange()}' and " +
                        "((class = '${clazz()}' and symbol= '${symbol()}' and time between '2020-03-22T10:00:49.123452Z' and '2020-03-22T11:01:14.123455Z' EXCL) " +
                        "or (class = '${clazz()}' and symbol= '${symbolTwo()}' and time between '2020-03-22T11:01:49.123457Z' and '2020-03-22T12:01:14.123455Z' EXCL))",
                        arrayOf(
                                arrayOf(symbol(), "2020-03-22T10:00:49.123452Z"),
                                arrayOf(symbol(), "2020-03-22T10:00:55.123453Z"),
                                arrayOf(symbol(), "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:49.123457Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:50.123458Z"),
                                arrayOf(symbolTwo(), "2020-03-22T12:01:05.123454Z")
                        )
                ),
                arrayOf("((exchange = '${exchange()}' and class = '${clazz()}' and symbol= '${symbol()}' and time between '2020-03-22T10:00:49.123452Z' and '2020-03-22T11:01:14.123455Z' EXCL) " +
                        "or (exchange = '${exchange()}' and symbol= '${symbolTwo()}' and time between '2020-03-22T11:01:49.123457Z' and '2020-03-22T12:01:14.123455Z' EXCL))",
                        arrayOf(
                                arrayOf(symbol(), "2020-03-22T10:00:49.123452Z"),
                                arrayOf(symbol(), "2020-03-22T10:00:55.123453Z"),
                                arrayOf(symbol(), "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:49.123457Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:50.123458Z"),
                                arrayOf(symbolTwo(), "2020-03-22T12:01:05.123454Z")
                        )
                ),
                arrayOf("exchange = '${exchange()}' and " +
                        "((class = '${clazz()}' and symbol= '${symbol()}' and time >= '2020-03-22T10:00:49.123452Z' and  time < '2020-03-22T11:01:14.123455Z') " +
                        "or (class = '${clazz()}' and symbol= '${symbolTwo()}' and time >= '2020-03-22T11:01:49.123457Z' and time < '2020-03-22T12:01:14.123455Z'))",
                        arrayOf(
                                arrayOf(symbol(), "2020-03-22T10:00:49.123452Z"),
                                arrayOf(symbol(), "2020-03-22T10:00:55.123453Z"),
                                arrayOf(symbol(), "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:49.123457Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:50.123458Z"),
                                arrayOf(symbolTwo(), "2020-03-22T12:01:05.123454Z")
                        )
                ),
                arrayOf("exchange = '${exchange()}' and " +
                        "((class in ('${clazz()}', '${classTwo()}') and symbol in  ('${symbol()}', '${symbolThree()}') and time >= '2020-03-22T10:00:49.123452Z' and  time < '2020-03-22T11:01:14.123455Z') " +
                        "or (class = '${clazz()}' and symbol= '${symbolTwo()}' and time >= '2020-03-22T11:01:49.123457Z' and time < '2020-03-22T12:01:14.123455Z'))",
                        arrayOf(
                                arrayOf(symbol(), "2020-03-22T10:00:49.123452Z"),
                                arrayOf(symbol(), "2020-03-22T10:00:55.123453Z"),
                                arrayOf(symbol(), "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:49.123457Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:50.123458Z"),
                                arrayOf(symbolTwo(), "2020-03-22T12:01:05.123454Z"),
                                arrayOf(symbolThree(), "2020-03-22T10:00:49.123452Z"),
                                arrayOf(symbolThree(), "2020-03-22T10:00:55.123453Z"),
                                arrayOf(symbolThree(), "2020-03-22T11:01:05.123454Z"),
                        )
                ),
                arrayOf("exchange = '${exchange()}' and " +
                        "((class = '${clazz()}' and symbol= '${symbol()}' and time < '2020-03-22T11:01:14.123455Z') " +
                        "or (class = '${clazz()}' and symbol= '${symbolTwo()}' and time < '2020-03-22T12:01:14.123455Z'))",
                        arrayOf(
                                arrayOf(symbol(), "2020-03-22T10:00:01.123450Z"),
                                arrayOf(symbol(), "2020-03-22T10:00:09.123451Z"),
                                arrayOf(symbol(), "2020-03-22T10:00:49.123452Z"),
                                arrayOf(symbol(), "2020-03-22T10:00:55.123453Z"),
                                arrayOf(symbol(), "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:14.123455Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:29.123456Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:49.123457Z"),
                                arrayOf(symbolTwo(), "2020-03-22T11:01:50.123458Z"),
                                arrayOf(symbolTwo(), "2020-03-22T12:01:05.123454Z")
                        )
                ),
                arrayOf("exchange = '${exchange()}' and " +
                        "((class = '${clazz()}' and symbol= '${symbol()}' and time < '2020-03-22T11:01:14.123455Z') " +
                        "or (class = '${clazz()}' and symbol= '${symbolTwo()}' and time > '2020-03-22T12:01:05.123454Z'))",
                        arrayOf(
                                arrayOf(symbol(), "2020-03-22T10:00:01.123450Z"),
                                arrayOf(symbol(), "2020-03-22T10:00:09.123451Z"),
                                arrayOf(symbol(), "2020-03-22T10:00:49.123452Z"),
                                arrayOf(symbol(), "2020-03-22T10:00:55.123453Z"),
                                arrayOf(symbol(), "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "2020-03-22T12:01:14.123455Z"),
                                arrayOf(symbolTwo(), "2020-03-22T12:01:29.123456Z"),
                                arrayOf(symbolTwo(), "2020-03-22T12:01:49.123457Z"),
                                arrayOf(symbolTwo(), "2020-03-22T12:01:50.123458Z"),
                        )
                ),
                arrayOf("exchange = '${exchange()}' and time > '2020-03-22T10:00:09.123451Z' and time < '2020-03-22T12:01:49.123457Z' and " +
                        "((class = '${clazz()}' and symbol= '${symbol()}' and time < '2020-03-22T11:01:14.123455Z') " +
                        "or (class = '${clazz()}' and symbol= '${symbolTwo()}' and time > '2020-03-22T12:01:05.123454Z'))",
                        arrayOf(
                                arrayOf(symbol(), "2020-03-22T10:00:49.123452Z"),
                                arrayOf(symbol(), "2020-03-22T10:00:55.123453Z"),
                                arrayOf(symbol(), "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "2020-03-22T12:01:14.123455Z"),
                                arrayOf(symbolTwo(), "2020-03-22T12:01:29.123456Z"),
                        )
                ),
        );
    }

    @DataProvider
    fun testDataAggregate(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("exchange = '${exchange()}' and " +
                        "((class = '${clazz()}' and symbol= '${symbol()}' and time between '2020-03-22T10:00:49.123452Z' and '2020-03-22T11:01:14.123455Z' EXCL) " +
                        "or (class = '${clazz()}' and symbol= '${symbolTwo()}' and time between '2020-03-22T11:01:49.123457Z' and '2020-03-22T12:01:14.123455Z' EXCL))",
                        arrayOf(
                                arrayOf(symbol(), "3", "2020-03-22T10:00:49.123452Z", "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "3", "2020-03-22T11:01:49.123457Z", "2020-03-22T12:01:05.123454Z"),
                        )
                ),
                arrayOf("((exchange = '${exchange()}' and class = '${clazz()}' and symbol= '${symbol()}' and time between '2020-03-22T10:00:49.123452Z' and '2020-03-22T11:01:14.123455Z' EXCL) " +
                        "or (exchange = '${exchange()}' and symbol= '${symbolTwo()}' and time between '2020-03-22T11:01:49.123457Z' and '2020-03-22T12:01:14.123455Z' EXCL))",
                        arrayOf(
                                arrayOf(symbol(), "3", "2020-03-22T10:00:49.123452Z", "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "3", "2020-03-22T11:01:49.123457Z", "2020-03-22T12:01:05.123454Z"),
                        )
                ),
                arrayOf("exchange = '${exchange()}' and " +
                        "((class = '${clazz()}' and symbol= '${symbol()}' and time >= '2020-03-22T10:00:49.123452Z' and  time < '2020-03-22T11:01:14.123455Z') " +
                        "or (class = '${clazz()}' and symbol= '${symbolTwo()}' and time >= '2020-03-22T11:01:49.123457Z' and time < '2020-03-22T12:01:14.123455Z'))",
                        arrayOf(
                                arrayOf(symbol(), "3", "2020-03-22T10:00:49.123452Z", "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "3", "2020-03-22T11:01:49.123457Z", "2020-03-22T12:01:05.123454Z"),
                        )
                ),
                arrayOf("exchange = '${exchange()}' and " +
                        "((class in ('${clazz()}', '${classTwo()}') and symbol in  ('${symbol()}', '${symbolThree()}') and time >= '2020-03-22T10:00:49.123452Z' and  time < '2020-03-22T11:01:14.123455Z') " +
                        "or (class = '${clazz()}' and symbol= '${symbolTwo()}' and time >= '2020-03-22T11:01:49.123457Z' and time < '2020-03-22T12:01:14.123455Z'))",
                        arrayOf(
                                arrayOf(symbol(), "3", "2020-03-22T10:00:49.123452Z", "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "3", "2020-03-22T11:01:49.123457Z", "2020-03-22T12:01:05.123454Z"),
                                arrayOf(symbolThree(), "3", "2020-03-22T10:00:49.123452Z", "2020-03-22T11:01:05.123454Z"),
                        )
                ),
                arrayOf("exchange = '${exchange()}' and " +
                        "((class = '${clazz()}' and symbol= '${symbol()}' and time < '2020-03-22T11:01:14.123455Z') " +
                        "or (class = '${clazz()}' and symbol= '${symbolTwo()}' and time < '2020-03-22T12:01:14.123455Z'))",
                        arrayOf(
                                arrayOf(symbol(), "5", "2020-03-22T10:00:01.123450Z", "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "6", "2020-03-22T11:01:05.123454Z", "2020-03-22T12:01:05.123454Z"),
                        )
                ),
                arrayOf("exchange = '${exchange()}' and " +
                        "((class = '${clazz()}' and symbol= '${symbol()}' and time < '2020-03-22T11:01:14.123455Z') " +
                        "or (class = '${clazz()}' and symbol= '${symbolTwo()}' and time > '2020-03-22T12:01:05.123454Z'))",
                        arrayOf(
                                arrayOf(symbol(), "5", "2020-03-22T10:00:01.123450Z", "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "4", "2020-03-22T12:01:14.123455Z", "2020-03-22T12:01:50.123458Z"),
                        )
                ),
                arrayOf("exchange = '${exchange()}' and time > '2020-03-22T10:00:09.123451Z' and time < '2020-03-22T12:01:49.123457Z' and " +
                        "((class = '${clazz()}' and symbol= '${symbol()}' and time < '2020-03-22T11:01:14.123455Z') " +
                        "or (class = '${clazz()}' and symbol= '${symbolTwo()}' and time > '2020-03-22T12:01:05.123454Z'))",
                        arrayOf(
                                arrayOf(symbol(), "3", "2020-03-22T10:00:49.123452Z", "2020-03-22T11:01:05.123454Z"),
                                arrayOf(symbolTwo(), "2", "2020-03-22T12:01:14.123455Z", "2020-03-22T12:01:29.123456Z"),
                        )
                ),
        );
    }
}