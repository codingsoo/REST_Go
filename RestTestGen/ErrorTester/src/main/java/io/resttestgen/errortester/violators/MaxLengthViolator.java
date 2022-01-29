package io.resttestgen.errortester.violators;

import io.resttestgen.requestbuilder.parameters.RequestParameter;
import io.resttestgen.nominaltester.fieldgenerator.RandomGenerator;
import io.swagger.v3.oas.models.media.Schema;

import static io.resttestgen.errortester.mutator.ParameterMutationHelper.getParameterValue;
import static io.resttestgen.errortester.mutator.ParameterMutationHelper.setParameterValue;

public class MaxLengthViolator implements Violator {

    @Override
    public RequestParameter applyViolation(RequestParameter requestParameter) {
        RandomGenerator randomGenerator = new RandomGenerator();

        Schema parameterSchema = requestParameter.getParameterSchema();
        Integer maxLength = parameterSchema.getMaxLength();
        if (maxLength == null) return null;

        String value = getParameterValue(requestParameter);
        int length = value.length();
        value = value.concat(randomGenerator.getRandomString(
                maxLength - length + 1, maxLength + randomGenerator.getRandomInteger(1, 10),
                true, true));

        setParameterValue(requestParameter, value);
        return requestParameter;
    }
}
