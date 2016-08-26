
package com.axibase.tsd.api.method.version;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
public class Licence {

    @JsonProperty("foreCastEnabled")
    private Boolean foreCastEnabled;
    @JsonProperty("hbaseServers")
    private Integer hbaseServers;
    @JsonProperty("remoteHbase")
    private Object remoteHbase;
    @JsonProperty("dataVersioningExpirationTime")
    private Object dataVersioningExpirationTime;
    @JsonProperty("forecastExpirationTime")
    private Object forecastExpirationTime;
    @JsonProperty("productVersion")
    private String productVersion;
    @JsonProperty("dataVersioningEnabled")
    private Boolean dataVersioningEnabled;

    /**
     * @return The foreCastEnabled
     */
    @JsonProperty("foreCastEnabled")
    public Boolean getForeCastEnabled() {
        return foreCastEnabled;
    }

    /**
     * @param foreCastEnabled The foreCastEnabled
     */
    @JsonProperty("foreCastEnabled")
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
    public void setRemoteHbase(Object remoteHbase) {
        this.remoteHbase = remoteHbase;
    }

    /**
     * @return The dataVersioningExpirationTime
     */
    @JsonProperty("dataVersioningExpirationTime")
    public Object getDataVersioningExpirationTime() {
        return dataVersioningExpirationTime;
    }

    /**
     * @param dataVersioningExpirationTime The dataVersioningExpirationTime
     */
    @JsonProperty("dataVersioningExpirationTime")
    public void setDataVersioningExpirationTime(Object dataVersioningExpirationTime) {
        this.dataVersioningExpirationTime = dataVersioningExpirationTime;
    }

    /**
     * @return The forecastExpirationTime
     */
    @JsonProperty("forecastExpirationTime")
    public Object getForecastExpirationTime() {
        return forecastExpirationTime;
    }

    /**
     * @param forecastExpirationTime The forecastExpirationTime
     */
    @JsonProperty("forecastExpirationTime")
    public void setForecastExpirationTime(Object forecastExpirationTime) {
        this.forecastExpirationTime = forecastExpirationTime;
    }

    /**
     * @return The productVersion
     */
    @JsonProperty("productVersion")
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * @param productVersion The productVersion
     */
    @JsonProperty("productVersion")
    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
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
}
