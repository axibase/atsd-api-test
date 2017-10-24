package com.axibase.tsd.api.method.checks;

import lombok.extern.slf4j.Slf4j;

import static com.axibase.tsd.api.method.series.SeriesMethod.getIndexerStatus;

@Slf4j
public class SearchIndexCheck extends AbstractCheck {
    private static final String ERROR_MESSAGE = "Failed to check search index status";

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }

    @Override
    public boolean isChecked() {
        try {
            return isIdle();
        } catch (Exception e) {
            log.error("Unexpected error on series check. Reason: {}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private boolean isIdle() throws Exception {
        String status = getIndexerStatus();
        return status != null && status.equals("Idle");
    }
}
