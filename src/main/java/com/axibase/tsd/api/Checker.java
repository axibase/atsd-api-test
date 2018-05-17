package com.axibase.tsd.api;


import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.util.NotCheckedException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Checker {

    public static void check(AbstractCheck check) {
        new Checker().verify(check);
    }

    public void verify(AbstractCheck check) {
        Long startTime = System.currentTimeMillis();
        while (!check.isChecked()) {
            if (System.currentTimeMillis() - BaseMethod.UPPER_BOUND_FOR_CHECK > startTime) {
                throw new NotCheckedException(check.getErrorMessage());
            } else {
                try {
                    Thread.sleep(BaseMethod.REQUEST_INTERVAL);
                } catch (InterruptedException e) {
                    log.error("Check sleep was interrupted! {}", e.getCause());
                    throw new IllegalStateException(e.getMessage());
                }
            }
        }
    }
}
