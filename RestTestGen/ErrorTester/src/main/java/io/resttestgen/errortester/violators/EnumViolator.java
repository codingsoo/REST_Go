package io.resttestgen.errortester.violators;

import io.resttestgen.requestbuilder.parameters.RequestParameter;
import io.resttestgen.nominaltester.fieldgenerator.RandomGenerator;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;

import static io.resttestgen.errortester.mutator.ParameterMutationHelper.getParameterValue;
import static io.resttestgen.errortester.mutator.ParameterMutationHelper.setParameterValue;

public class EnumViolator implements Violator {

    @Override
    public RequestParameter applyViolation(RequestParameter requestParameter) {
        RandomGenerator randomGenerator = new RandomGenerator();

        Schema parameterSchema = requestParameter.getParameterSchema();
        List enumList;
        if ((enumList = getEnumList(parameterSchema)) == null)
            return null;

        String value = getParameterValue(requestParameter);
        if (value.contains("&SEPARATOR&")) {
            ArrayList<String> randomStrings = new ArrayList<>();
            for (int i = 0; i < randomGenerator.getRandomInteger(1, 10); i++) {
                String tmp = null;
                do {
                    tmp = randomGenerator.getRandomString(1, 10, true, true);
                } while (enumList.contains(tmp));
                randomStrings.add(tmp);
            }
            value = String.join("&SEPARATOR&", randomStrings);
        } else {
            while (enumList.contains(value)) {
                value = randomGenerator.getRandomString(1, 10, true, true);
            }
        }

        setParameterValue(requestParameter, value);
        return requestParameter;
    }

    private List getEnumList(Schema parameterSchema) {
        List enumList = parameterSchema.getEnum();

        if (enumList == null || enumList.isEmpty()) {
            if (parameterSchema.getType().equals("array")) {
                ArraySchema arraySchema = (ArraySchema) parameterSchema;
                enumList = arraySchema.getItems().getEnum();
            }
        }

        return enumList;
    }

}
