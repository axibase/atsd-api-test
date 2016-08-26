
package com.axibase.tsd.api.method.version;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Version {

    @JsonProperty("buildInfo")
    private BuildInfo buildInfo;
    @JsonProperty("licence")
    private Licence licence;
    @JsonProperty("date")
    private Date date;
    @JsonIgnore
    private Map<String, java.lang.Object> additionalProperties = new HashMap<String, java.lang.Object>();

    /**
     * @return The buildInfo
     */
    @JsonProperty("buildInfo")
    public BuildInfo getBuildInfo() {
        return buildInfo;
    }

    /**
     * @param buildInfo The buildInfo
     */
    @JsonProperty("buildInfo")
    public void setBuildInfo(BuildInfo buildInfo) {
        this.buildInfo = buildInfo;
    }

    /**
     * @return The licence
     */
    @JsonProperty("licence")
    public Licence getLicence() {
        return licence;
    }

    /**
     * @param licence The licence
     */
    @JsonProperty("licence")
    public void setLicence(Licence licence) {
        this.licence = licence;
    }

    /**
     * @return The date
     */
    @JsonProperty("date")
    public Date getDate() {
        return date;
    }

    /**
     * @param date The date
     */
    @JsonProperty("date")
    public void setDate(Date date) {
        this.date = date;
    }

    @JsonAnyGetter
    public Map<String, java.lang.Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, java.lang.Object value) {
        this.additionalProperties.put(name, value);
    }

}
