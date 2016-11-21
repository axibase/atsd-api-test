package com.axibase.tsd.api.util;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class TestNameGenerator {
    private static final String API_METHODS_PACKAGE_NAME = "com.axibase.tsd.api.method";
    private Map<String, Integer> dictionary;
    private Class TEST_ANNOTATION = org.testng.annotations.Test.class;

    public TestNameGenerator() {
        this.dictionary = new HashMap<>();
    }


    public String getEntityName() {
        return getTestName(Keys.ENTITY);
    }

    public String getMetricName() {
        return getTestName(Keys.METRIC);
    }

    String getTestName(Keys key) {
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
                Class clazz = Class.forName(stackTraceElement.getClassName());
                Method method = clazz.getDeclaredMethod(stackTraceElement.getMethodName());
                if (isTestMethod(method)) {
                    testMethod = method;
                    testClass = clazz;
                    break;
                }
            } catch (NoClassDefFoundError | NoSuchMethodException | ClassNotFoundException e) {
                continue;
            }
        }

        if (testMethod == null) {
            for (StackTraceElement stackTraceElement : ste) {
                try {
                    Class<?> clazz = Class.forName(stackTraceElement.getClassName());
                    if (isTestClass(clazz)) {
                        testClass = clazz;
                        break;
                    }
                } catch (NoClassDefFoundError | ClassNotFoundException e) {
                    continue;
                }
            }
            if (testClass == null) {
                throw new IllegalStateException("Test name generator must be called from Test method!");
            }
        }
        return methodToKeyName(testClass, testMethod).concat(key.toString());
    }

    private boolean isTestClass(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            if (isTestMethod(method)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTestMethod(Method method) {
        return method.getAnnotation(TEST_ANNOTATION) != null;
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
        return (method != null) ?
                String.format("%s%s", extractBaseName(clazz), camelToLisp(method.getName()))
                : extractBaseName(clazz);
    }

    private String camelToLisp(String camelCaseName) {
        return camelCaseName.replaceAll("(.)(\\p{Upper})", "$1-$2").toLowerCase().concat("-");
    }

    enum Keys {
        ENTITY("entity"), METRIC("metric"), ENTITY_GROUP("entity-group"), MESSAGE("message"), PROPERTY("property"),
        PROPERTY_TYPE("property-type");

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