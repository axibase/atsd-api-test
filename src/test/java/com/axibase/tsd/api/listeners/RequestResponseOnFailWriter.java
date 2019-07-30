package com.axibase.tsd.api.listeners;

import com.axibase.tsd.logging.LoggingFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

@Slf4j
public class RequestResponseOnFailWriter implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        LoggingFilter.clear();
    }

    @Override
    public void onTestSuccess(ITestResult result) {

    }

    @Override
    public void onTestFailure(ITestResult result) {
        final String requestAndResponse = LoggingFilter.getRequestAndResponse();
        if (StringUtils.isNotBlank(requestAndResponse)) {
            log.info("{}.{}\n{}", result.getTestClass().getRealClass().getSimpleName(),
                    result.getMethod().getMethodName(),
                    requestAndResponse);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    @Override
    public void onStart(ITestContext context) {

    }

    @Override
    public void onFinish(ITestContext context) {
        LoggingFilter.clear();
    }
}
