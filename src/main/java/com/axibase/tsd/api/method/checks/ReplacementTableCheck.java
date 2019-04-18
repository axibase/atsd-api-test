package com.axibase.tsd.api.method.checks;

import com.axibase.tsd.api.method.replacementtable.ReplacementTableMethod;
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;

public class ReplacementTableCheck extends AbstractCheck {
    private static final String ERROR_MESSAGE = "Failed to create Replacement Table!";
    private ReplacementTable replacementTable;

    public ReplacementTableCheck(ReplacementTable replacementTable) {
        this.replacementTable = replacementTable;
    }

    @Override
    public boolean isChecked() {
        try {
            return ReplacementTableMethod.replacementTableExist(replacementTable);
        } catch (Exception e) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
    }

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
