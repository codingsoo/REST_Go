package io.resttestgen.nominaltester.models;

import io.resttestgen.nominaltester.fieldgenerator.manufacturetraces.ManufactureTraces;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Objects;

/**
 * Class ExecutionParameter links the Swagger parameter schema and the swagger code-gen method parameter
 */
public class ExecutionParameter {

    private Schema parameterSchema;
    protected Object value;
    private String sanitizedName; // sanitize by codegen to use it as variable name
    private String parameterName; // original name inside swagger file
    private java.lang.reflect.Parameter parameter;
    private ManufactureTraces manufactureTraces;
    private String parameterClassName;
    private boolean required;

    public ExecutionParameter(String parameterName){
        this.parameterName = parameterName;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    public Schema getParameterSchema() {
        return parameterSchema;
    }

    public void setParameterSchema(Schema parameterSchema) {
        this.parameterSchema = parameterSchema;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getSanitizedName() {
        return sanitizedName;
    }

    public void setSanitizedName(String sanitizedName) {
        this.sanitizedName = sanitizedName;
    }

    public java.lang.reflect.Parameter getParameter() {
        return parameter;
    }

    public void setParameter(java.lang.reflect.Parameter parameter) {
        this.parameter = parameter;
    }

    public ManufactureTraces getManufactureTraces() {
        return manufactureTraces;
    }

    public void setManufactureTraces(ManufactureTraces manufactureTraces) {
        this.manufactureTraces = manufactureTraces;
    }

    public String getParameterClassName() {
        return parameter.getType().getName();
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterClassName(String parameterClassName) {
        this.parameterClassName = parameterClassName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterName);
    }
}


