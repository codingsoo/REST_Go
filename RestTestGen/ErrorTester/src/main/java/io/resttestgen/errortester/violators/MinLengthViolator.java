package io.resttestgen.errortester.violators;

import io.resttestgen.errortester.mutator.ParameterMutationHelper;
import io.resttestgen.requestbuilder.parameters.RequestParameter;
import io.swagger.v3.oas.models.media.Schema;

public class MinLengthViolator implements Violator {

    @Override
    public RequestParameter applyViolation(RequestParameter requestParameter) {
        Schema parameterSchema = requestParameter.getParameterSchema();
        Integer minLength = parameterSchema.getMinLength();
        if (minLength == null) return null;

        String value = ParameterMutationHelper.getParameterValue(requestParameter);
        if (minLength == 1) {
            value = "";
        }
        else {
            value = value.substring(0, minLength - 1);
        }

        ParameterMutationHelper.setParameterValue(requestParameter, value);
        return requestParameter;
    }
}
