package com.axibase.tsd.api.method.checks;


public class NotPassedCheck extends AbstractCheck {
    private static final String ERROR_MESSAGE = "Failed to delete!";
    private AbstractCheck check;

    public NotPassedCheck(AbstractCheck check) {
        this.check = check;
    }

    @Override
    public boolean isChecked() {
        return !check.isChecked();
    }

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
