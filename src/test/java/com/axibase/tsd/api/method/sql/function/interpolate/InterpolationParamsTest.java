package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.model.sql.function.interpolate.InterpolationParams;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.model.TimeUnit.SECOND;
import static org.testng.Assert.assertEquals;


public class InterpolationParamsTest {
    @Test
    public void testToString() {
        InterpolationParams params = new InterpolationParams(1, SECOND).fill(true);
        String expectedString = "1 SECOND, AUTO, INNER, TRUE";
        assertEquals(params.toString(), expectedString);
    }

}
