package io.resttestgen.requestbuilder.parameters;

import io.resttestgen.swaggerschema.models.ParameterType;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Objects;

public class RequestParameter {

    public RequestParameter() {
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterIn() {
        return parameterIn;
    }

    public void setParameterIn(String parameterIn) {
        this.parameterIn = parameterIn;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
        if (this.required == null) this.required = false;
    }

    public Schema getParameterSchema() {
        return parameterSchema;
    }

    public void setParameterSchema(Schema parameterSchema) {
        this.parameterSchema = parameterSchema;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(ParameterType parameterType) {
        this.parameterType = parameterType;
    }

    public boolean isValueSet() {
        return false;
    }

    public Object getParameterValue() {
        return null;
    }

    private String parameterName;
    private String parameterIn;
    private Schema parameterSchema;
    private Boolean required;
    private ParameterType parameterType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestParameter that = (RequestParameter) o;
        return Objects.equals(parameterName, that.parameterName) && parameterType == that.parameterType;
    }
}
