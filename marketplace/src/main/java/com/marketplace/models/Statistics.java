package com.marketplace.models;

import java.util.Map;

public class Statistics {
    private String orderId;
    private Enums.StatsType type;
    private Map<String, Object> data;

    public Statistics(String orderId, Enums.StatsType type, Map<String, Object> data) {
        this.orderId = orderId;
        this.type = type;
        this.data = data;
    }

    public void generateReport() { /* generate */ }
    public void exportData() { /* export */ }
}
