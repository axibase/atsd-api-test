package com.axibase.tsd.api.model.extended;


import com.axibase.tsd.api.util.Util;

public class CommandSendingResult {
    private Integer fail;
    private Integer success;
    private Integer total;

    private CommandSendingResult() {
    }

    public CommandSendingResult(Integer fail, Integer success) {
        this.fail = fail;
        this.success = success;
        this.total = fail + success;
    }

    public Integer getFail() {
        return fail;
    }

    public void setFail(Integer fail) {
        this.fail = fail;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandSendingResult result = (CommandSendingResult) o;

        if (!fail.equals(result.fail)) return false;
        if (!success.equals(result.success)) return false;
        return total.equals(result.total);

    }

    @Override
    public int hashCode() {
        int result = fail.hashCode();
        result = 31 * result + success.hashCode();
        result = 31 * result + total.hashCode();
        return result;
    }
}
