package io.resttestgen.errortester.violators;

import io.resttestgen.errortester.mutator.ParameterMutationHelper;
import io.resttestgen.requestbuilder.parameters.RequestParameter;
import io.resttestgen.nominaltester.fieldgenerator.RandomGenerator;
import io.swagger.v3.oas.models.media.Schema;

import java.math.BigDecimal;

public class MaximumViolator implements Violator {

    @Override
    public RequestParameter applyViolation(RequestParameter requestParameter) {
        int randomInteger = new RandomGenerator().getRandomInteger(1, 100);

        Schema parameterSchema = requestParameter.getParameterSchema();
        BigDecimal maximum = parameterSchema.getMaximum();
        if (maximum == null) return null;

        String value = maximum.add(new BigDecimal(randomInteger)).toString();
        ParameterMutationHelper.setParameterValue(requestParameter, value);
        return requestParameter;
    }
}
