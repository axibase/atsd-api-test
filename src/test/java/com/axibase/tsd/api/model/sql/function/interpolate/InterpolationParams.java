package com.axibase.tsd.api.model.sql.function.interpolate;

import com.axibase.tsd.api.model.Interval;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class InterpolationParams {
    private Interval interval;
    private InterpolateFunction function;
    private Boundary boundary;
    private FillMode fillMode;
    private Alignment alignment;


    public InterpolationParams(Interval interval, InterpolateFunction function, Boundary boundary) {
        this.interval = interval;
        this.function = function;
        this.boundary = boundary;
    }

    public InterpolationParams(Interval interval, InterpolateFunction function) {

        this.interval = interval;
        this.function = function;
    }

    public InterpolationParams(Interval interval, InterpolateFunction function, Boundary boundary, FillMode fillMode, Alignment alignment) {
        this.interval = interval;
        this.function = function;
        this.boundary = boundary;
        this.fillMode = fillMode;
        this.alignment = alignment;
    }

    public InterpolationParams(Interval interval, InterpolateFunction function, Boundary boundary, FillMode fillMode) {
        this.interval = interval;
        this.function = function;
        this.boundary = boundary;
        this.fillMode = fillMode;

    }

    public InterpolationParams(Interval interval) {

        this.interval = interval;

    }

    public Interval getInterval() {
        return interval;
    }

    public InterpolateFunction getFunction() {
        return function;
    }

    public Boundary getBoundary() {
        return boundary;
    }

    public FillMode getFillMode() {
        return fillMode;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public String toString() {
        Object[] fields = {
                interval, function, boundary, fillMode, alignment
        };
        return StringUtils.join(objectsToString(fields), ", ");
    }


    private List<String> objectsToString(Object[] objects) {
        List<String> resultList = new ArrayList<>();
        for (Object o : objects) {
            if (o != null) {
                resultList.add(o.toString());
            }
        }
        return resultList;
    }
}
