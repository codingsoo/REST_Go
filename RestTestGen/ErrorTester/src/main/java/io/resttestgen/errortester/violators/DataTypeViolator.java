package io.resttestgen.errortester.violators;

import io.resttestgen.errortester.mutator.ParameterMutationHelper;
import io.resttestgen.requestbuilder.parameters.RequestParameter;
import io.resttestgen.nominaltester.fieldgenerator.RandomGenerator;
import org.apache.commons.lang3.math.NumberUtils;

// TODO: change PathParameters to QueryParameters where needed

public class DataTypeViolator implements Violator {
    public RequestParameter applyViolation(RequestParameter requestParameter) {
        String value = ParameterMutationHelper.getParameterValue(requestParameter);
        if (value == null) return null;

        RandomGenerator randomGenerator = new RandomGenerator();
        if (value.contains("&SEPARATOR&")) {
            String[] split = value.split("&SEPARATOR&");
            for (int i = 0; i < split.length; i++) {
                // Mutate the type
                if (NumberUtils.isParsable(value)) {
                    split[i] = randomGenerator.getRandomString(5, 10, true, false);
                } else {
                    split[i] = String.valueOf(randomGenerator.getRandomInteger(-100, 100));
                }
            }
            value = String.join("&SEPARATOR&", split);
        } else {
            // Mutate the type
            if (NumberUtils.isParsable(value)) {
                value = randomGenerator.getRandomString(5, 10, true, false);
            } else {
                value = String.valueOf(randomGenerator.getRandomInteger(-100, 100));
            }
        }

        // Set the new value
        ParameterMutationHelper.setParameterValue(requestParameter, value);

        return requestParameter;
    }
}
