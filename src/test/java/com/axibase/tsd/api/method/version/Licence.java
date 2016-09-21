
package com.axibase.tsd.api.method.version;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Licence {
    @JsonProperty("forecastEnabled")
    private Boolean foreCastEnabled;
    @JsonProperty("hbaseServers")
    private Integer hbaseServers;
    @JsonProperty("remoteHbase")
    private Boolean remoteHbase;
    @JsonProperty("productVersion")
    private ProductVersion productVersion;
    @JsonProperty("dataVersioningEnabled")
    private Boolean dataVersioningEnabled;
    @JsonProperty("dataVersioningExpirationTime")
    private Long dataVersioningExpirationTime;
    @JsonProperty("forecastExpirationTime")
    private Long forecastExpirationTime;

    /**
     * @return The foreCastEnabled
     */
    @JsonProperty("forecastEnabled")
    public Boolean getForeCastEnabled() {
        return foreCastEnabled;
    }

    /**
     * @param foreCastEnabled The foreCastEnabled
     */
    @JsonProperty("forecastEnabled")
    public void setForeCastEnabled(Boolean foreCastEnabled) {
        this.foreCastEnabled = foreCastEnabled;
    }

    /**
     * @return The hbaseServers
     */
    @JsonProperty("hbaseServers")
    public Integer getHbaseServers() {
        return hbaseServers;
    }

    /**
     * @param hbaseServers The hbaseServers
     */
    @JsonProperty("hbaseServers")
    public void setHbaseServers(Integer hbaseServers) {
        this.hbaseServers = hbaseServers;
    }

    /**
     * @return The remoteHbase
     */
    @JsonProperty("remoteHbase")
    public Object getRemoteHbase() {
        return remoteHbase;
    }

    /**
     * @param remoteHbase The remoteHbase
     */
    @JsonProperty("remoteHbase")
    public void setRemoteHbase(Boolean remoteHbase) {
        this.remoteHbase = remoteHbase;
    }

    /**
     * @return The productVersion
     */
    @JsonProperty("productVersion")
    public ProductVersion getProductVersion() {
        return productVersion;
    }

    /**
     * @param productVersion The productVersion
     */
    @JsonProperty("productVersion")
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

    /**
     * @return The dataVersioningEnabled
     */
    @JsonProperty("dataVersioningEnabled")
    public Boolean getDataVersioningEnabled() {
        return dataVersioningEnabled;
    }

    /**
     * @param dataVersioningEnabled The dataVersioningEnabled
     */
    @JsonProperty("dataVersioningEnabled")
    public void setDataVersioningEnabled(Boolean dataVersioningEnabled) {
        this.dataVersioningEnabled = dataVersioningEnabled;
    }

    @JsonProperty("dataVersioningExpirationTime")
    public Long getDataVersioningExpirationTime() {
        return dataVersioningExpirationTime;
    }

    @JsonProperty("dataVersioningExpirationTime")
    public void setDataVersioningExpirationTime(Long dataVersioningExpirationTime) {
        this.dataVersioningExpirationTime = dataVersioningExpirationTime;
    }

    @JsonProperty("forecastExpirationTime")
    public Long getForecastExpirationTime() {
        return forecastExpirationTime;
    }

    @JsonProperty("forecastExpirationTime")
    public void setForecastExpirationTime(Long forecastExpirationTime) {
        this.forecastExpirationTime = forecastExpirationTime;
    }
}
