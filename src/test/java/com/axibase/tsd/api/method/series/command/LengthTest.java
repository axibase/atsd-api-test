package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.command.FieldFormat;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.transport.Transport;
import io.qameta.allure.Issue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import com.axibase.tsd.api.util.Mocks;

import java.math.BigDecimal;
import java.util.*;

import static com.axibase.tsd.api.method.series.SeriesTest.assertSeriesExisting;
import static org.testng.AssertJUnit.*;

public class LengthTest extends SeriesMethod {
    private final Transport transport;
    private static final int MAX_LENGTH = 128 * 1024;

    @Factory(dataProvider = "transport", dataProviderClass = Transport.class)
    public LengthTest(Transport transport) {
        this.transport = transport;
    }


    @Issue("2412")
    @Issue("6319")
    @Test
    public void testMaxLength() throws Exception {
        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setTimeISO(Mocks.ISO_TIME);
        seriesCommand.setEntityName(Mocks.entity());

        int currentLength = seriesCommand.compose().length();

        List<Series> seriesList = new ArrayList<>();
        Map<String, String> values = new HashMap<>();

        while (currentLength <= MAX_LENGTH) {
            Series series = new Series(seriesCommand.getEntityName(), Mocks.metric());
            series.addSamples(Sample.ofDateInteger(Mocks.ISO_TIME, 1));
            String appendix = FieldFormat.keyValue("m", series.getMetric(), "1");
            currentLength += appendix.length();
            if (currentLength < MAX_LENGTH) {
                values.put(series.getMetric(), "1");
                seriesList.add(series);
            } else {
                currentLength -= appendix.length();
                int leftCount = MAX_LENGTH - currentLength;
                String repeated = new String(new char[leftCount + 1]).replace("\0", "1");
                int lastIndex = seriesList.size() - 1;
                Series lastSeries = seriesList.get(lastIndex);
                seriesList.remove(lastSeries);
                lastSeries.setSamples(Collections.singletonList(Sample.ofDateDecimal(Mocks.ISO_TIME, new BigDecimal(repeated))));
                values.put(lastSeries.getMetric(), repeated);
                seriesList.add(lastSeries);
                break;
            }
        }
        seriesCommand.setValues(values);
        assertEquals("Command length is not maximal", seriesCommand.compose().length(), MAX_LENGTH);
        transport.sendNoDebug(seriesCommand);
        assertSeriesExisting("Cannot send series with " + transport, seriesList);
    }

    @Issue("2412")
    @Issue("6319")
    @Test
    public void testMaxLengthOverflow() throws Exception {
        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setTimeISO(Mocks.ISO_TIME);
        seriesCommand.setEntityName(Mocks.entity());

        int currentLength = seriesCommand.compose().length();

        Map<String, String> values = new HashMap<>();

        while (currentLength <= MAX_LENGTH) {
            Series series = new Series()
                    .setMetric(Mocks.metric())
                    .setEntity(seriesCommand.getEntityName())
                    .addSamples(Sample.ofDateInteger(Mocks.ISO_TIME, 1));
            String appendix = FieldFormat.keyValue("m", series.getMetric(), "1");
            currentLength += appendix.length();
            values.put(series.getMetric(), "1");
        }
        seriesCommand.setValues(values);
        assertTrue("SeriesCommand length is not overflow", seriesCommand.compose().length() > MAX_LENGTH);

        assertFalse("Sending result must contain one failed command", transport.sendNoDebug(seriesCommand));
    }


}
