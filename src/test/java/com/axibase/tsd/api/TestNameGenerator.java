package com.axibase.tsd.api;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class TestNameGenerator {
    private static final String API_METHODS_PACKAGE_NAME = "com.axibase.tsd.api.method";
    private Map<String, Integer> dictionary;

    public TestNameGenerator() {
        this.dictionary = new HashMap<>();
    }


    public String getEntityName() {
        return getTestName(Keys.ENTITY);
    }

    public String getMetricName() {
        return getTestName(Keys.METRIC);
    }

    private String getTestName(Keys key) {
        String keyName = getKeyName(key);
        Integer testNumber = (this.dictionary.containsKey(keyName)) ? dictionary.get(keyName) + 1 : 0;
        dictionary.put(keyName, testNumber);
        return String.format("%s-%d", keyName, testNumber);
    }

    private synchronized String getKeyName(Keys key) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        Method testMethod = null;
        Class testClass = null;
        for (StackTraceElement stackTraceElement : ste) {
            try {
                Class<?> clazz = Class.forName(stackTraceElement.getClassName());
                Method method = clazz.getDeclaredMethod(stackTraceElement.getMethodName());
                if (method.getAnnotation(org.testng.annotations.Test.class) != null) {
                    testMethod = method;
                    testClass = clazz;
                    break;
                }
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                continue;
            }
        }

        if (testMethod == null) {
            throw new IllegalStateException("Test name generator must be called from Test method!");
        }
        return methodToKeyName(testClass, testMethod).concat(key.toString());
    }

    private String extractBaseName(Class clazz) {
        String canonicalClassName = clazz.getCanonicalName();
        String className = clazz.getSimpleName();
        if (canonicalClassName.contains(API_METHODS_PACKAGE_NAME)) {
            String result = canonicalClassName.replace(API_METHODS_PACKAGE_NAME + '.', "").replace(className, "").replace('.', '-');
            return result + camelToLisp(className);
        } else {
            throw new IllegalStateException("Failed to generate test name for non-method package");
        }

    }

    private String methodToKeyName(Class<?> clazz, Method method) {
        return String.format("%s%s", extractBaseName(clazz), camelToLisp(method.getName()));
    }

    private String camelToLisp(String camelCaseName) {
        return camelCaseName.replaceAll("(.)(\\p{Upper})", "$1-$2").toLowerCase().concat("-");
    }

    private enum Keys {
        ENTITY("entity"), METRIC("metric");

        private String textValue;

        Keys(String textValue) {
            this.textValue = textValue;
        }


        @Override
        public String toString() {
            return this.textValue;
        }
    }
}