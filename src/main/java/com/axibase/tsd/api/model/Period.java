package com.axibase.tsd.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.TimeZone;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Period {
    private int count;
    private TimeUnit unit;
    private PeriodAlignment align;
    private String timezone;

    public Period(int count, TimeUnit unit) {
        this.count = count;
        this.unit = unit;
    }

    /**
     * Create a Period object with count, unit, and timezone fields.
     *
     * @param count      - an amount of time units.
     * @param unit       {@link TimeUnit} instance.
     * @param timezoneId one of {@link TimeZone#getAvailableIDs()}.
     */
    public Period(final int count, final TimeUnit unit, final String timezoneId) {
        this.count = count;
        this.unit = unit;
        this.timezone = timezoneId;
    }

    /**
     * Create a Period object with count, unit, and alignment.
     *
     * @param count           an amount of time units.
     * @param unit            {@link TimeUnit} instance.
     * @param periodAlignment period alignment.
     */
    public Period(final int count, final TimeUnit unit, final PeriodAlignment periodAlignment) {
        this.count = count;
        this.unit = unit;
        this.align = periodAlignment;
    }


    @Override
    public String toString() {
        if (align != null) {
            return String.format("%d %s %s", count, unit.toString(), align.toString());
        }
        return String.format("%d %s", count, unit.toString());
    }
}
