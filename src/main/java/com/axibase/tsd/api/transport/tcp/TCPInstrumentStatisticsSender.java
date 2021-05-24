package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.model.financial.InstrumentStatistics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@Slf4j
public class TCPInstrumentStatisticsSender extends TCPSenderBase {

    private TCPInstrumentStatisticsSender() {
    }

    public static void send(String command) throws IOException {
        final Config config = Config.getInstance();
        final int port = config.getInstrumentStatisticsTcpPort();
        sendHelper(command, port, log, true);
    }

    public static void send(String... commands) throws IOException {
        send(StringUtils.join(commands, LINE_SEPARATOR));
    }

    public static void send(InstrumentStatistics... statistics) throws IOException {
        send(Arrays.asList(statistics));
    }

    public static void send(Collection<InstrumentStatistics> statistics) throws IOException {
        send(statistics.stream().map(InstrumentStatistics::toCsvLine).toArray(String[]::new));
    }
}