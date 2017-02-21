
package com.axibase.tsd.api.model.version;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Version {

    private BuildInfo buildInfo;
    private License license;
    private Date date;
    @JsonIgnore
    private Map<String, java.lang.Object> additionalProperties = new HashMap<>();


    public BuildInfo getBuildInfo() {
        return buildInfo;
    }


    public void setBuildInfo(BuildInfo buildInfo) {
        this.buildInfo = buildInfo;
    }


    public License getLicense() {
        return license;
    }


    public void setLicense(License license) {
        this.license = license;
    }


    public Date getDate() {
        return date;
    }


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
