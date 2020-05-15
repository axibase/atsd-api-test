package com.axibase.tsd.api.method.financial;

import com.axibase.tsd.api.model.command.EntityCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.financial.InstrumentSearchEntry;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.transport.tcp.TCPTradesSender;
import com.axibase.tsd.api.util.ResponseAsList;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBasicAuthorization;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.testng.AssertJUnit.assertSame;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

public class InstrumentSearchBase {
    protected static String DEFAULT_EXCHANGE = "test";

    protected static void searchAndTest(String query, InstrumentSearchEntry... expected) throws AssertionError {
        searchAndTest(query, -1, expected);
    }

    protected static void searchAndTest(String query, int limit, InstrumentSearchEntry... expected) throws AssertionError {
        List<InstrumentSearchEntry> foundEntries = search(query, limit);
        assertArrayEquals(expected, foundEntries.toArray(new InstrumentSearchEntry[0]));
    }

    protected static List<InstrumentSearchEntry> search(String query, int limit) {
        RequestSenderWithBasicAuthorization sender = RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER;
        Map<String, Object> params = new HashMap<>();
        params.put("pattern", query);
        params.put("limit", limit > 0 ? String.valueOf(limit) : "");
        Response response = sender.executeRootRequestWithParams("/financial/trades/suggest/instruments", params, HttpMethod.GET);
        response.bufferEntity();
        return response.readEntity(ResponseAsList.ofInstrumentSearchEntries());
    }


    protected static InstrumentSearchEntry entry(String symbol, String classCode, String description) {
        return entry(symbol, classCode, description, DEFAULT_EXCHANGE);
    }

    protected static InstrumentSearchEntry entryUnassigned(String symbol, String classCode, String description) {
        return entry(symbol, classCode, description, "");
    }

    private static InstrumentSearchEntry entry(String symbol, String classCode, String description, String exchange) {
        classCode = classCode.toUpperCase();
        symbol = symbol.toUpperCase();
        exchange = exchange.toUpperCase();

        String entity = makeEntityName(symbol, classCode);
        return new InstrumentSearchEntry(classCode, symbol, exchange, description, entity);
    }

    private static String makeEntityName(String symbol, String classCode) {
        return symbol.toLowerCase() + "_[" + classCode.toLowerCase() + "]";
    }

    protected static TradeBundle tradesBundle() {
        return new TradeBundle();
    }

    protected static class TradeBundle {
        @Data
        private static class Entry {
            private final String classCode;
            private final String symbol;
            private final String description;
            private final boolean assigned;
        }
        private List<Entry> items = new ArrayList<>();

        public TradeBundle trade(String symbol, String classCode, String description) {
            return trade(symbol, classCode, description, true);
        }

        public TradeBundle tradeUnassigned(String symbol, String classCode, String description) {
            return trade(symbol, classCode, description, false);
        }

        private TradeBundle trade(String symbol, String classCode, String description, boolean assigned) {
            items.add(new Entry(classCode, symbol, description, assigned));
            return this;
        }

        @SneakyThrows
        public void insert() throws IOException {
            // INSERT TRADES WITH CSV UPLOAD
            String[] commands = items.stream().filter(Entry::isAssigned).map(TradeBundle::makeCommand).toArray(String[]::new);
            TCPTradesSender.send(commands);

            // Insert descriptions for instruments, as well as unassigned instruments
            List<PlainCommand> entityCommands = items.stream()
                    .filter(TradeBundle::needUpdateEntity)
                    .map(TradeBundle::makeEntityCommand)
                    .collect(Collectors.toList());
            TCPSender.send(entityCommands);

            // Let ATSD apply incoming commands
            Thread.sleep(1000);

            updateInstrumentIndex();
        }

        private static boolean needUpdateEntity(Entry entry) {
            return !entry.isAssigned() || StringUtils.isNotBlank(entry.getDescription());
        }

        private static PlainCommand makeEntityCommand(Entry entry) {
            Map<String, String> entityTags = new HashMap<>();
            entityTags.put("name", entry.getDescription());
            entityTags.put("short_name", entry.getDescription());
            entityTags.put("symbol", entry.getSymbol().toUpperCase());
            entityTags.put("class_code", entry.getClassCode().toUpperCase());

            Entity entity = new Entity();
            entity.setName(makeEntityName(entry.getSymbol(), entry.getClassCode()));
            entity.setTags(entityTags);
            return new EntityCommand(entity);
        }

        private static String makeCommand(Entry entry) {
            long ts = IndicesGenerator.timestamp();
            long tn = IndicesGenerator.trade();
            String cls = entry.getClassCode();
            String sym = entry.getSymbol();
            String exch = DEFAULT_EXCHANGE;

            return String.format("%d,%d,0,%s,%s,%s,B,1,42.42,", tn, ts, cls, sym, exch);
        }

        private static void updateInstrumentIndex() {
            Response resp = RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER.executeRootRequest("/admin/financial/instruments/reindex", HttpMethod.POST);
            assertSame("Failed to sync instrument index", resp.getStatusInfo().getFamily(), Response.Status.Family.SUCCESSFUL);
        }
    }

    private static class IndicesGenerator {
        private static AtomicLong timestamp = new AtomicLong(System.currentTimeMillis());
        private static AtomicLong trade = new AtomicLong(1084842/* random */);

        public static long timestamp() {
            return timestamp.addAndGet(5);
        }

        public static long trade() {
            return trade.incrementAndGet();
        }
    }
}
