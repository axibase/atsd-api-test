package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.sql.function.interpolate.InterpolationParams;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.model.TimeUnit.SECOND;
import static com.axibase.tsd.api.model.sql.function.interpolate.InterpolateFunction.AUTO;
import static org.testng.Assert.assertEquals;


public class InterpolationParamsTest {
    @Test
    public void testToString() throws Exception {
        InterpolationParams params = new InterpolationParams(new Interval(1, SECOND), AUTO);
        String expectedString = "1 SECOND, AUTO";
        assertEquals(params.toString(), expectedString);
    }

}