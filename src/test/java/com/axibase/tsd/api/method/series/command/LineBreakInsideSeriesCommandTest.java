package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.AssertJUnit.assertTrue;

@Slf4j
public class LineBreakInsideSeriesCommandTest {
    private static Sample testSample = Sample.ofDateInteger(Mocks.ISO_TIME, 1234);

    enum TestType {
        NETWORK_API, DATA_API
    }

    @AllArgsConstructor
    @ToString
    private static class TestData {
        TestType testType;
        String insertData;
        String responseData;
    }

    @DataProvider
    public static Object[][] provideTestTypeAndValue() {
        String[][] valueResults = {
                /*    Original                        Network API                  Data API      */
                {"test\ntest",                    "test\ntest",                "test\ntest"},
                {"test\ntest\ntest",              "test\ntest\ntest",          "test\ntest\ntest"},
                {"test\rtest\rtest",              "test\rtest\rtest",          "test\ntest\ntest"},
                {"test\r\ntest\n\rtest",          "test\ntest\n\rtest",        "test\ntest\ntest"},
                {"test\n\ntest\n\ntest",          "test\ntest\ntest",          "test\ntest\ntest"},
                {"test\n\r",                      "test\n\r",                  "test\n"},
                {"  \r\r\n\rtest\n\rtest  \n\r ", "  \n\rtest\n\rtest  \n\r ", "\ntest\ntest\n"},
        };

        List<Object[]> parameters = new ArrayList<>();

        for (TestType type : TestType.values()) {
            for (String[] valueResult : valueResults) {
                String send = valueResult[0];
                String get = type == TestType.NETWORK_API ? valueResult[1] : valueResult[2];
                parameters.add(new Object[]{new TestData(type, send, get)});
            }
        }

        Object[][] result = new Object[parameters.size()][];
        parameters.toArray(result);

        return result;
    }

    @Issue("3878")
    @Issue("3906")
    @Test(
            description = "Test line break processing for tag value",
            dataProvider = "provideTestTypeAndValue"
    )
    public void testTagLineBreak(TestData data) throws Exception {
        log.info("Data", data);
        Series seriesWithBreak = new Series(Mocks.entity(), Mocks.metric());
        seriesWithBreak.addTag("test-tag", data.insertData);
        seriesWithBreak.addSamples(testSample);

        Series responseSeries = seriesWithBreak.copy();
        responseSeries.addTag("test-tag", data.responseData);

        String message = String.format("Unexpected actual tag value in response for %s, " +
                "incorrect line breaks processing", data.testType);
        sendAndCheck(message, seriesWithBreak, responseSeries, data.testType);
    }

    @Issue("3878")
    @Issue("3906")
    @Test(
            description = "Test line break processing for text field",
            dataProvider = "provideTestTypeAndValue"
    )
    public void testMetricTextLineBreak(TestData data) {
        Sample sampleWithBreak = Sample.ofDateText(Mocks.ISO_TIME, data.insertData);
        Sample responseSample = Sample.ofDateText(Mocks.ISO_TIME, data.responseData);

        Series seriesWithBreak = new Series(Mocks.entity(), Mocks.metric());
        Series responseSeries = seriesWithBreak.copy();

        seriesWithBreak.addSamples(sampleWithBreak);
        responseSeries.addSamples(responseSample);

        String message = String.format("Unexpected actual text (x field) in response for %s, " +
                "incorrect line breaks processing", data.testType);
        sendAndCheck(message, seriesWithBreak, responseSeries, data.testType);
    }

    private void sendAndCheck(String message, Series insert, Series response, TestType type) {
        List<PlainCommand> commands = new ArrayList<>();
        commands.addAll(insert.toCommands());

        boolean checked;
        try {
            switch (type) {
                case NETWORK_API:
                    TCPSender.sendChecked(new SeriesCheck(Collections.singletonList(response)), commands);
                    break;
                case DATA_API:
                    CommandMethod.send(commands);
                    Checker.check(new SeriesCheck(Collections.singletonList(response)));
                    break;
            }
            checked = true;
        } catch (IOException e) {
            e.printStackTrace();
            checked = false;
        }

        assertTrue(message, checked);
    }
}
