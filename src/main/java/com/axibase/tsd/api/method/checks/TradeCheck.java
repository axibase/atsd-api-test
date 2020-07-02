package com.axibase.tsd.api.method.checks;

import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.model.sql.StringTable;

public class TradeCheck extends AbstractCheck {
    public static final String SQL_PATTERN = "select trade_num from atsd_trade where exchange='%s' and class='%s' and symbol='%s' " +
            "and trade_num = %d and time between %d and %d incl";
    private Trade trade;
    private String sql;

    public TradeCheck(Trade trade) {
        this.trade = trade;
        this.sql = String.format(SQL_PATTERN, trade.getExchange(), trade.getClazz(), trade.getSymbol(),
                trade.getNumber(), trade.getTimestamp(), trade.getTimestamp());
    }

    @Override
    public boolean isChecked() {
        try {
            StringTable result = SqlMethod.queryTable(sql);
            return result.getRows().size() > 0 && result.getValueAt(0, 0).equals(String.valueOf(trade.getNumber()));
        } catch (IllegalStateException e) { // instrument has not been created yet
            return false;
        }
    }
}