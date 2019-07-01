package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.model.command.EntityCommand;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.CommonAssertions.assertCheck;
import static com.axibase.tsd.api.util.Mocks.*;
import static java.util.Collections.singletonList;
import static org.testng.AssertJUnit.assertEquals;

public class TCPSenderTest {
    @Test
    public void testSend() throws Exception {
        Series series = Mocks.series();
        TCPSender.send(series.toCommands());
        assertCheck(new SeriesCheck(singletonList(series)));
    }

    @Test
    public void testDebugSend() throws Exception {
        Entity entity = new Entity(entity(), TAGS);
        entity.setTimeZoneID(TIMEZONE_ID);
        entity.setInterpolationMode(InterpolationMode.LINEAR);
        entity.setLabel(LABEL);

        String result = TCPSender.send(singletonList(new EntityCommand(entity)), true);
        assertEquals("ok", result);
        assertCheck(new EntityCheck(entity));
    }
}
