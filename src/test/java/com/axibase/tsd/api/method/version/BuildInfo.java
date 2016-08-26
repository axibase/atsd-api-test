
package com.axibase.tsd.api.method.version;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuildInfo {

    @JsonProperty("revisionNumber")
    private String revisionNumber;
    @JsonProperty("buildNumber")
    private String buildNumber;
    @JsonProperty("buildId")
    private String buildId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The revisionNumber
     */
    @JsonProperty("revisionNumber")
    public String getRevisionNumber() {
        return revisionNumber;
    }

    /**
     * @param revisionNumber The revisionNumber
     */
    @JsonProperty("revisionNumber")
    public void setRevisionNumber(String revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    /**
     * @return The buildNumber
     */
    @JsonProperty("buildNumber")
    public String getBuildNumber() {
        return buildNumber;
    }

    /**
     * @param buildNumber The buildNumber
     */
    @JsonProperty("buildNumber")
    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    /**
     * @return The buildId
     */
    @JsonProperty("buildId")
    public String getBuildId() {
        return buildId;
    }

    /**
     * @param buildId The buildId
     */
    @JsonProperty("buildId")
    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
