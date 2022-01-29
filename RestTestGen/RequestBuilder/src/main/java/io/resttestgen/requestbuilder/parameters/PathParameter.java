package io.resttestgen.requestbuilder.parameters;

import io.swagger.v3.oas.models.media.Schema;

public class PathParameter extends RequestParameter {

    private String parameterValue;
    private Schema parameterSchema;

    @Override
    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    public Schema getParameterSchema() {
        return parameterSchema;
    }

    public void setParameterSchema(Schema parameterSchema) {
        this.parameterSchema = parameterSchema;
    }

    @Override
    public boolean isValueSet() {
        return parameterValue != null;
    }
}
