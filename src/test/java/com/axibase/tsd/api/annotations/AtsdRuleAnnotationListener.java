package com.axibase.tsd.api.annotations;

import com.axibase.tsd.api.method.version.ProductVersion;
import com.axibase.tsd.api.method.version.VersionMethod;
import org.testng.*;

public class AtsdRuleAnnotationListener implements IInvokedMethodListener, ITestListener {
    private ProductVersion version = null;
    private boolean testSkipped = false;

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult result) {
        if (method.isTestMethod() && annotationPresent(method, AtsdRule.class)) {

        }
    }

    private boolean annotationPresent(IInvokedMethod method, Class clazz) {
        boolean retVal = method.getTestMethod().getConstructorOrMethod().getMethod().isAnnotationPresent(clazz);
        return retVal;
    }


    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            if (method.getTestMethod().getConstructorOrMethod().getMethod().isAnnotationPresent(AtsdRule.class)) {
                if (testSkipped) {

                    testResult.setStatus(ITestResult.SKIP);
                }
            }

        }
    }

    @Override
    public void onTestStart(ITestResult iTestResult) {
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {

    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

    }

    @Override
    public void onStart(ITestContext context) {
        for (ITestNGMethod m1 : context.getAllTestMethods()) {
            if (m1.getConstructorOrMethod().getMethod().isAnnotationPresent(AtsdRule.class)) {
                //capture metadata information.
                version = m1.getConstructorOrMethod().getMethod().getAnnotation(AtsdRule.class).version();
                ProductVersion actualVersion = VersionMethod.queryVersionCheck().getLicence().getProductVersion();
                if (version != actualVersion) {
                    testSkipped = true;
                }
            }
        }
    }

    @Override
    public void onFinish(ITestContext iTestContext) {
    }
}
