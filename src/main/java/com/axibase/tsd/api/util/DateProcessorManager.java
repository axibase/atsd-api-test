package com.axibase.tsd.api.util;

import com.axibase.date.DatetimeProcessor;
import com.axibase.date.NamedPatterns;
import com.axibase.date.PatternResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DateProcessorManager {
    public static final DatetimeProcessor ISO = PatternResolver.createNewFormatter(NamedPatterns.ISO);
    private static final Map<String, DatetimeProcessor> cache = new ConcurrentHashMap<>();

    public static DatetimeProcessor getTimeProcessor(String format) {
        return NamedPatterns.ISO.equals(format) ? ISO : cache.computeIfAbsent(format, PatternResolver::createNewFormatter);
    }
}
