package com.axibase.tsd.api.util;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.InstrumentStatisticsCheck;
import com.axibase.tsd.api.model.financial.InstrumentStatistics;
import com.axibase.tsd.api.transport.tcp.TCPInstrumentStatisticsSender;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class InstrumentStatisticsSender {

    public static InstrumentStatisticsBatchChecker send(Collection<InstrumentStatistics> statistics) throws IOException {
        TCPInstrumentStatisticsSender.send(statistics);
        return new InstrumentStatisticsBatchChecker(statistics);
    }

    public static InstrumentStatisticsBatchChecker send(InstrumentStatistics... statistics) throws IOException {
        return send(Arrays.asList(statistics));
    }

    @RequiredArgsConstructor
    public static class InstrumentStatisticsBatchChecker {
        private final Collection<InstrumentStatistics> statistics;

        public void waitUntilTradesInsertedAtMost(long time, TimeUnit timeUnit) {
            statistics.stream().map(InstrumentStatisticsCheck::new).forEach(check -> Checker.check(check, time, timeUnit));
        }
    }
}