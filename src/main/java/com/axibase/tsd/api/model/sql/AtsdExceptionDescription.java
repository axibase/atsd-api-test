package com.axibase.tsd.api.model.sql;


import java.util.List;

public class AtsdExceptionDescription {
    private List<Error> errors;

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }
}
