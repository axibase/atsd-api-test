package com.axibase.tsd.api.method.financial;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class InstrumentSearchTest extends InstrumentSearchBase {
    @BeforeClass
    public void prepareTestData() throws IOException {
        tradesBundle()
                .trade("share1", "TEST", "First Share", "RU0007661625")
                .trade("share2", "TEST", "Second Share", "RU0007661624")
                .trade("share10", "TEST", "")
                .trade("share3", "TQBR", "Primary Share")
                .trade("share2", "TQBR", "Primary Share Two")
                .tradeUnassigned("share4", "EQOB", "Unassigned Share Four")
                .tradeUnassigned("share5", "TQOB", "Unassigned Share Five")
                .trade("descpfx", "TEST", "SharAEEEes4Test Inc.")
                .trade("option1", "TEST", "First Option")
                .trade("option2", "TEST", "Second Option")
                .trade("PRIM", "TEST", "ПАО 'Пример'")
                .trade("PRIS", "TEST", "ОАО Пример и Примерчики")
                .trade("TEST_ETF", "TEST", "THE ISH TEST")
                .trade("ru000a0zzwr6", "TEST", "Isin named")
                .trade("MOEX", "TEST", "", "RU000A0JR4A1")
                .trade("MOEX", "TEST2", "", "RU000A0JR4A1")
                .insert()
                .waitUntilTradesInsertedAtMost(5, TimeUnit.MINUTES);
    }

    @Test
    public void testFoundBySymbol() {
        searchAndTest("opt",
                entry("option1", "TEST", "First Option"),
                entry("option2", "TEST", "Second Option")
        );
        searchAndTest("OPt",
                entry("option1", "TEST", "First Option"),
                entry("option2", "TEST", "Second Option")
        );
        searchAndTest("share1",
                entry("share1", "TEST", "First Share"),
                entry("share10", "TEST", "")
        );
    }

    @Test
    public void testFoundCyrillic() {
        searchAndTest("Прим",
                entry("PRIM", "TEST", "ПАО 'Пример'"),
                entry("PRIS", "TEST", "ОАО Пример и Примерчики")
        );
        searchAndTest("ПримерИ",
                entry("PRIS", "TEST", "ОАО Пример и Примерчики")
        );
    }

    @Test
    public void testFoundByClassName() {
        searchAndTest("TQbR",
                entry("share2", "TQBR", "Primary Share Two"),
                entry("share3", "TQBR", "Primary Share")
        );
    }

    @Test
    public void testAcronymReplacement() {
        searchAndTest("ishares",
                entry("TEST_ETF", "TEST", "THE ISH TEST")
        );
    }

    @Test
    public void testFoundRegardingDisplayIndex() {
        // Instruments with positive display index should be placed first
        // Other instruments classes are considered same and sorted alphabetically
        searchAndTest("share",
                entry("share2", "TQBR", "Primary Share Two"),
                entry("share3", "TQBR", "Primary Share"),
                entryUnassigned("share5", "TQOB", "Unassigned Share Five"),
                entry("share1", "TEST", "First Share"),
                entry("share10", "TEST", ""),
                entry("share2", "TEST", "Second Share"),
                entryUnassigned("share4", "EQOB", "Unassigned Share Four"),
                entry("TEST_ETF", "TEST", "THE ISH TEST")
        );
    }

    @Test
    public void testNotFound() {
        searchAndTest("notfound");
    }

    @Test
    public void testFoundWithDescription() {
        searchAndTest("shar",
                entry("share2", "TQBR", "Primary Share Two"),
                entry("share3", "TQBR", "Primary Share"),
                entryUnassigned("share5", "TQOB", "Unassigned Share Five"),
                entry("share1", "TEST", "First Share"),
                entry("share10", "TEST", ""),
                entry("share2", "TEST", "Second Share"),
                entryUnassigned("share4", "EQOB", "Unassigned Share Four"),
                entry("descpfx", "TEST", "SharAEEEes4Test Inc."),
                entry("TEST_ETF", "TEST", "THE ISH TEST")
        );
    }

    @Test
    public void testFoundLimited() {
        searchAndTest("shar", 3,
                entry("share2", "TQBR", "Primary Share Two"),
                entry("share3", "TQBR", "Primary Share"),
                entryUnassigned("share5", "TQOB", "Unassigned Share Five")
        );
    }

    @Test
    public void testFoundBySubstring() {
        searchAndTest("hARe",
                entry("share2", "TQBR", "Primary Share Two"),
                entry("share3", "TQBR", "Primary Share"),
                entryUnassigned("share5", "TQOB", "Unassigned Share Five"),
                entry("share1", "TEST", "First Share"),
                entry("share10", "TEST", ""),
                entry("share2", "TEST", "Second Share"),
                entryUnassigned("share4", "EQOB", "Unassigned Share Four"),
                entry("TEST_ETF", "TEST", "THE ISH TEST")
        );
        searchAndTest("irst",
                entry("option1", "TEST", "First Option"),
                entry("share1", "TEST", "First Share")
        );
        searchAndTest("4Test",
                entry("descpfx", "TEST", "SharAEEEes4Test Inc.")
        );
    }

    @Test
    public void testFoundByIsin() {
        searchAndTest("RU0007661625", entry("share1", "TEST", "First Share"));
    }

    @Test
    public void testNonValidIsin() {
        // It's non-valid ISIN and should not be used as cache key
        searchAndTest("RU0007661624");
    }

    @Test
    public void testIsinInSymbolName() {
        searchAndTest("RU000A0ZZWR6",
                entry("ru000a0zzwr6", "TEST", "Isin named"));
    }

    @Test
    void testMultipleIsinBindings() {
        searchAndTest("RU000A0JR4A1",
                entry("MOEX", "TEST", ""),
                entry("MOEX", "TEST2", "")
        );
    }
}
