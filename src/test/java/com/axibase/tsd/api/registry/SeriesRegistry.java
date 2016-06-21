package com.axibase.tsd.api.registry;

import com.axibase.tsd.api.model.series.Series;

import java.util.Set;
import java.util.TreeSet;

public class SeriesRegistry {

    private static SeriesRegistry instance = null;

    private Set<String> entitySet = new TreeSet<>();
    private Set<String> metricSet = new TreeSet<>();

    public static SeriesRegistry getInstance() {
        if (null == instance) {
            instance = new SeriesRegistry();
        }
        return instance;
    }

    public void registerSeries(Series series) {
        String entity = series.getEntity();
        String metric = series.getMetric();
        if (entitySet.contains(entity) && metricSet.contains(metric)) {
            throw new IllegalArgumentException("Series already registered: " + series);
        }
        entitySet.add(entity);
        metricSet.add(metric);
    }
}
