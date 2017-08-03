package com.axibase.tsd.api.model.sql;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/* Ignoring unknown errors allows to fetch error from
   response with OK status, which contains other fields */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtsdExceptionDescription {
    private List<Error> errors;

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }
}
