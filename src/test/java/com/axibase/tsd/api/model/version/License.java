
package com.axibase.tsd.api.model.version;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class License {
    private Boolean forecastEnabled;
    private Integer hbaseServers;
    private Boolean remoteHbase;
    private ProductVersion productVersion;
    private Boolean dataVersioningEnabled;
    private Long dataVersioningExpirationTime;
    private Long forecastExpirationTime;

    public Boolean getForecastEnabled() {
        return forecastEnabled;
    }

    public void setForecastEnabled(Boolean forecastEnabled) {
        this.forecastEnabled = forecastEnabled;
    }

    public Integer getHbaseServers() {
        return hbaseServers;
    }

    public void setHbaseServers(Integer hbaseServers) {
        this.hbaseServers = hbaseServers;
    }

    public Object getRemoteHbase() {
        return remoteHbase;
    }

    public void setRemoteHbase(Boolean remoteHbase) {
        this.remoteHbase = remoteHbase;
    }

    public ProductVersion getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        if (ProductVersion.COMMUNITY.toString().equals(productVersion)) {
            this.productVersion = ProductVersion.COMMUNITY;
        } else {
            if (ProductVersion.ENTERPRISE.toString().equals(productVersion)) {
                this.productVersion = ProductVersion.ENTERPRISE;
            } else {
                throw new IllegalStateException("Incorrect Product version");
            }
        }
    }

    public Boolean getDataVersioningEnabled() {
        return dataVersioningEnabled;
    }

    public void setDataVersioningEnabled(Boolean dataVersioningEnabled) {
        this.dataVersioningEnabled = dataVersioningEnabled;
    }

    public Long getDataVersioningExpirationTime() {
        return dataVersioningExpirationTime;
    }

    public void setDataVersioningExpirationTime(Long dataVersioningExpirationTime) {
        this.dataVersioningExpirationTime = dataVersioningExpirationTime;
    }

    public Long getForecastExpirationTime() {
        return forecastExpirationTime;
    }

    public void setForecastExpirationTime(Long forecastExpirationTime) {
        this.forecastExpirationTime = forecastExpirationTime;
    }
}
