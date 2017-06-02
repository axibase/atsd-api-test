package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.command.FieldFormat;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.axibase.tsd.api.method.series.SeriesTest.assertSeriesExisting;
import static com.axibase.tsd.api.util.Mocks.ISO_TIME;
import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class LengthTest extends SeriesMethod {

    private static final int MAX_LENGTH = 128 * 1024;


    /**
     * #2412
     */
    @Test
    public void testMaxLength() throws Exception {
        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setTimeISO(ISO_TIME);
        seriesCommand.setEntityName(entity());

        Integer currentLength = seriesCommand.compose().length();

        List<Series> seriesList = new ArrayList<>();
        Map<String, String> values = new HashMap<>();

        while (currentLength <= MAX_LENGTH) {
            Series series = new Series(seriesCommand.getEntityName(), metric());
            series.addSamples(new Sample(ISO_TIME, 1));
            String appendix = FieldFormat.keyValue("m", series.getMetric(), "1");
            currentLength += appendix.length();
            if (currentLength < MAX_LENGTH) {
                values.put(series.getMetric(), "1");
                seriesList.add(series);
            } else {
                currentLength -= appendix.length();
                Integer leftCount = MAX_LENGTH - currentLength;
                String repeated = new String(new char[leftCount + 1]).replace("\0", "1");
                Integer lastIndex = seriesList.size() - 1;
                Series lastSeries = seriesList.get(lastIndex);
                seriesList.remove(lastSeries);
                lastSeries.setSamples(Collections.singletonList(new Sample(ISO_TIME, new BigDecimal(repeated))));
                values.put(lastSeries.getMetric(), repeated);
                seriesList.add(lastSeries);
                break;
            }
        }
        seriesCommand.setValues(values);
        assertEquals("Command length is not maximal", seriesCommand.compose().length(), MAX_LENGTH);
        CommandMethod.send(seriesCommand);
        assertSeriesExisting(seriesList);
    }

    /**
     * #2412
     */
    @Test
    public void testMaxLengthOverflow() throws Exception {
        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setTimeISO(ISO_TIME);
        seriesCommand.setEntityName(entity());

        Integer currentLength = seriesCommand.compose().length();

        Map<String, String> values = new HashMap<>();

        while (currentLength <= MAX_LENGTH) {
            Series series = new Series(seriesCommand.getEntityName(), metric());
            series.addSamples(new Sample(ISO_TIME, 1));
            String appendix = FieldFormat.keyValue("m", series.getMetric(), "1");
            currentLength += appendix.length();
            values.put(series.getMetric(), "1");
        }
        seriesCommand.setValues(values);
        assertTrue("SeriesCommand length is not overflow", seriesCommand.compose().length() > MAX_LENGTH);
        CommandSendingResult expectedResult = new CommandSendingResult(1, 0);

        assertEquals("Sending result must contain one failed command",
                expectedResult,
                CommandMethod.send(seriesCommand)
        );
    }


}
