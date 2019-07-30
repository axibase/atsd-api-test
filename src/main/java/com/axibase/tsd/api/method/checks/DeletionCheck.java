package com.axibase.tsd.api.method.checks;


public class DeletionCheck extends AbstractCheck {
    private AbstractCheck check;
    private final String errorMessage = check.getClass().getSimpleName() + " passed, but must have failed.";

    public DeletionCheck(AbstractCheck check) {
        this.check = check;
    }

    @Override
    public boolean isChecked() {
        return !check.isChecked();
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
