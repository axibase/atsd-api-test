package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import java.util.Collections;

public class TcpParsingTest {
    private static final String ENTITY_NAME = Mocks.entity();
    private static final String METRIC_NAME = Mocks.metric();
    private static final String METRIC_DESCRIPTION = "Some\ndescription";
    private static final int METRIC_VALUE = 123;

    private static final String NETWORK_COMMAND = String.format(
            "metric m:\"%1$s\" d:\"%2$s\"\n" +
            "series e:\"%3$s\" m:\"%1$s\"=%4$s d:%5$s",
            METRIC_NAME, METRIC_DESCRIPTION, ENTITY_NAME,
            METRIC_VALUE, Mocks.ISO_TIME
    );

    /*
        The main problem of #4411 was that series command after metric command with newlines
        is interpreted as metric command also.

        For example

        metric m:"metric-name" d:"Some
        description"
        series e:"entity-name" m:"some-other-metric-name"=123
     */
    @Issue("4411")
    @Test(
            description = "Test that series command that follows metric command with newline " +
                    "is parsed correctly"
    )
    public void testNetworkParser() throws Exception {
        Metric expectedMetric = new Metric(METRIC_NAME);
        expectedMetric.setDescription(METRIC_DESCRIPTION);

        Series expectedSeries = new Series(ENTITY_NAME, METRIC_NAME);
        expectedSeries.addSamples(Sample.ofDateInteger(Mocks.ISO_TIME, METRIC_VALUE));

        TCPSender.send(NETWORK_COMMAND);

        Checker.check(new MetricCheck(expectedMetric));
        Checker.check(new SeriesCheck(Collections.singletonList(expectedSeries)));
    }
}
