package com.axibase.tsd.api.transport.tcp;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
class TcpParsingTestData {
    @Setter(AccessLevel.PRIVATE)
    private String name;
    private String commandsText = "";
    private List<String> metricsJsonList = new ArrayList<>();
    private List<String> seriesJsonList = new ArrayList<>();

    TcpParsingTestData(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
