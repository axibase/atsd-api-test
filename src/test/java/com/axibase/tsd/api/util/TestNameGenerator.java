package com.axibase.tsd.api.util;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TestNameGenerator {
    private static final String API_METHODS_PACKAGE_NAME = "com.axibase.tsd.api";
    private Map<Key, Integer> dictionary;
    private Class<org.testng.annotations.Test> TEST_ANNOTATION = org.testng.annotations.Test.class;

    public TestNameGenerator() {
        this.dictionary = new HashMap<>();
    }

    public String newEntityName() {
        return newTestName(Key.ENTITY);
    }

    public String newMetricName() {
        return newTestName(Key.METRIC);
    }

    String newTestName(Key key) {
        String keyName = getPrefix(key);
        Integer testNumber = dictionary.containsKey(key) ? dictionary.get(key) + 1 : 0;
        dictionary.put(key, testNumber);
        return String.format("%s-%d", keyName, testNumber);
    }

    public String getPrefix(Key key) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        Method testMethod = null;
        Class testClass = null;
        for (StackTraceElement stackTraceElement : ste) {
            try {
                Class<?> clazz = Class.forName(stackTraceElement.getClassName());
                Method[] methods = clazz.getDeclaredMethods();

                // Search for the method with same name by hand,
                // because we don't know it's actual arguments list
                for (Method method : methods) {
                    if (method.getName().equals(stackTraceElement.getMethodName()) &&
                            isTestMethod(method)) {
                        testMethod = method;
                        testClass = clazz;
                        break;
                    }
                }
            } catch (NoClassDefFoundError | ClassNotFoundException e) {
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
                    break;
                }
            }
            if (testClass == null) {
                throw new IllegalStateException("Test name generator must be called from Test method!");
            }
        }
        return methodToKeyName(testClass, testMethod) + "-" + key.toString();
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
        return camelCaseName.replaceAll("(.)(\\p{Upper})", "$1-$2").toLowerCase();
    }

    public enum Key {
        ENTITY("entity"),
        METRIC("metric"),
        ENTITY_GROUP("entity-group"),
        MESSAGE("message"),
        PROPERTY("property"),
        PROPERTY_TYPE("property-type");

        private String textValue;

        Key(String textValue) {
            this.textValue = textValue;
        }

        @Override
        public String toString() {
            return this.textValue;
        }
    }
}
