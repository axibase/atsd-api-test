package com.axibase.tsd.api.model.extended;


import com.axibase.tsd.api.util.Util;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommandSendingResult {
    private final int fail;
    private final int success;
    private final int total;

    public CommandSendingResult(int fail, int success) {
        this(fail, success, fail + success);
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }
}
