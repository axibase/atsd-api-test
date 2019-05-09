package com.axibase.tsd.api.util;

import com.axibase.tsd.api.model.alert.Alert;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.series.Series;
import lombok.experimental.UtilityClass;

import javax.ws.rs.core.GenericType;
import java.util.List;


/**
 * Factories of types used to deserialize JSON response.
 * Must not subtype because of type erasure.
 */
@UtilityClass
public class ResponseAsList {
    public static GenericType<List<Message>> ofMessages() {
        return new GenericType<List<Message>>(){};
    }

    public static GenericType<List<Alert>> ofAlerts() {
        return new GenericType<List<Alert>>(){};
    }

    public static GenericType<List<Series>> ofSeries() {
        return new GenericType<List<Series>>(){};
    }

    public static GenericType<List<Metric>> ofMetrics() {
        return new GenericType<List<Metric>>(){};
    }

    public static GenericType<List<Property>> ofProperties() {
        return new GenericType<List<Property>>(){};
    }
}
