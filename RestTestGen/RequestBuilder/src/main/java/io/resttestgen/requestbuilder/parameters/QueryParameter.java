package io.resttestgen.requestbuilder.parameters;

import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;

public class QueryParameter extends RequestParameter {

    private ArrayList<String> parameterValues = new ArrayList<>();
    private Schema parameterSchema = null;

    @Override
    public ArrayList<String> getParameterValue() {
        return parameterValues;
    }

    public void addParameterValue(String parameterValue) {
        this.parameterValues.add(parameterValue);
    }

    public void setParameterValues(ArrayList<String> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public Schema getParameterSchema() {
        return parameterSchema;
    }

    public void setParameterSchema(Schema parameterSchema) {
        this.parameterSchema = parameterSchema;
    }

    @Override
    public boolean isValueSet() {
        return getParameterValue() != null && parameterValues.size() > 0;
    }
}
