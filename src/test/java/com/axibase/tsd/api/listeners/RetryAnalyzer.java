package com.axibase.tsd.api.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private int count = 0;
    private int maxCount = 4;

    @Override
    public boolean retry(ITestResult result) {
        if (count < maxCount) {
            count++;
            return true;
        }
        return false;
    }
}