package com.axibase.tsd.api.model.alert;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlertUpdateQuery {
    private final int id;
    private final boolean acknowledged;
}
