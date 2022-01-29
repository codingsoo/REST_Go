package io.resttestgen.errortester.violators;

import io.resttestgen.errortester.mutator.ParameterMutationHelper;
import io.resttestgen.requestbuilder.parameters.RequestParameter;
import io.resttestgen.nominaltester.fieldgenerator.RandomGenerator;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.Arrays;

public class UniqueItemsViolator implements Violator {

    @Override
    public RequestParameter applyViolation(RequestParameter requestParameter) {
        Schema parameterSchema = requestParameter.getParameterSchema();
        Boolean uniqueItems = parameterSchema.getUniqueItems();
        if (uniqueItems == null) return null;

        String value = ParameterMutationHelper.getParameterValue(requestParameter);
        ArrayList<String> items = new ArrayList<>(Arrays.asList(value.split("&SEPARATOR&")));

        if (uniqueItems) {
            String randomItem = items.get(new RandomGenerator().getRandomInteger(0, items.size() - 1));
            items.add(randomItem);
            value = String.join("&SEPARATOR&", items);

            ParameterMutationHelper.setParameterValue(requestParameter, value);
        }

        return requestParameter;
    }

}
