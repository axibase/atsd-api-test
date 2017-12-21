package com.axibase.tsd.api.model.sql.function.interpolate;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.TimeUnit;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
public class InterpolationParams {
    private static final Object[] DEFAULT_PARAMETERS = {
            null,
            InterpolateFunction.AUTO,
            Boundary.INNER,
            FillMode.FALSE,
            Alignment.CALENDAR,
            "null"
    };

    private Period interval;
    private InterpolateFunction function;
    private Boundary boundary;
    private FillMode fillMode;
    private Alignment alignment;
    private String timeZone;

    public InterpolationParams(Period interval) {
        this.interval = interval;
    }

    public InterpolationParams(int count, TimeUnit unit) {
        this.interval = new Period(count, unit);
    }

    @Override
    public String toString() {
        Object[] fields = {
                interval,
                function,
                boundary,
                fillMode,
                alignment,
                timeZone != null ? "'" + timeZone + "'" : null
        };

        int maxIndex = 0;

        /* Search for last non-null field */
        for (int i = 1; i < DEFAULT_PARAMETERS.length; i++) {
            if (fields[i] != null) {
                maxIndex = i;
            }
        }

        /* Fill-in default field values before last non-null */
        for (int i = 1; i < maxIndex; i++) {
            if (fields[i] == null) {
                fields[i] = DEFAULT_PARAMETERS[i];
            }
        }

        List<String> resultList = new ArrayList<>();
        for (Object o : fields) {
            if (o != null) {
                resultList.add(o.toString());
            }
        }

        return StringUtils.join(resultList, ", ");
    }

    public InterpolationParams linear() {
        function = InterpolateFunction.LINEAR;
        return this;
    }

    public InterpolationParams previous() {
        function = InterpolateFunction.PREVIOUS;
        return this;
    }

    public InterpolationParams auto() {
        function = InterpolateFunction.AUTO;
        return this;
    }

    public InterpolationParams inner() {
        boundary = Boundary.INNER;
        return this;
    }

    public InterpolationParams outer() {
        boundary = Boundary.OUTER;
        return this;
    }

    public InterpolationParams yes() {
        fillMode = FillMode.YES;
        return this;
    }

    public InterpolationParams no() {
        fillMode = FillMode.NO;
        return this;
    }

    public InterpolationParams fill(boolean doFill) {
        fillMode = doFill ? FillMode.TRUE : FillMode.FALSE;
        return this;
    }

    public InterpolationParams fill(double value) {
        fillMode = FillMode.value(value);
        return this;
    }

    public InterpolationParams calendar() {
        alignment = Alignment.CALENDAR;
        return this;
    }

    public InterpolationParams startTime() {
        alignment = Alignment.START_TIME;
        return this;
    }

    public InterpolationParams timeZone(String zone) {
        timeZone = zone;
        return this;
    }
}
