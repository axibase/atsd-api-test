package com.axibase.tsd.api.method.checks;


import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class Check extends AbstractCheck {
    private final String errorMessage;
    private final Supplier<Boolean> checker;

    @Override
    public boolean isChecked() {
        try {
            return checker.get();
        } catch (Exception e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
