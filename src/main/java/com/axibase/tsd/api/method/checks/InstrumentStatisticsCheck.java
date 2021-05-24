package com.axibase.tsd.api.method.checks;

import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.financial.InstrumentStatistics;
import com.axibase.tsd.api.model.sql.StringTable;

import java.time.Instant;

public class InstrumentStatisticsCheck extends AbstractCheck {
    public static final String SQL_PATTERN = "select stat.datetime from atsd_entity where name = '%s_[%s]'";
    private final String sql;
    private final InstrumentStatistics instrumentStatistics;

    public InstrumentStatisticsCheck(InstrumentStatistics instrumentStatistics) {
        this.instrumentStatistics = instrumentStatistics;
        this.sql = String.format(SQL_PATTERN, instrumentStatistics.getSymbol().toLowerCase(), instrumentStatistics.getClazz().toLowerCase());
    }

    @Override
    public boolean isChecked() {
        try {
            StringTable result = SqlMethod.queryTable(sql);
            Instant instant = Instant.ofEpochSecond(0, instrumentStatistics.epochNano());
            String expectedDate = instant.toString();
            return result.getRows().size() > 0
                    && result.getValueAt(0, 0).equals(expectedDate);
        } catch (IllegalStateException e) { // instrument has not been created yet
            return false;
        }
    }
}