package io.resttestgen.errortester.violators;

import io.resttestgen.requestbuilder.parameters.RequestParameter;
import io.resttestgen.nominaltester.fieldgenerator.RandomGenerator;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.Arrays;

import static io.resttestgen.errortester.mutator.ParameterMutationHelper.getParameterValue;
import static io.resttestgen.errortester.mutator.ParameterMutationHelper.setParameterValue;

public class MaxItemsViolator implements Violator {

    @Override
    public RequestParameter applyViolation(RequestParameter requestParameter) {
        RandomGenerator randomGenerator = new RandomGenerator();

        Schema parameterSchema = requestParameter.getParameterSchema();
        Integer maxItems = parameterSchema.getMaxItems();
        if (maxItems == null) return null;

        String value = getParameterValue(requestParameter);
        ArrayList<String> items = new ArrayList<>(Arrays.asList(value.split("&SEPARATOR&")));

        if (maxItems < 0) {
            value = null;
        } else {
            ArrayList<String> itemList = new ArrayList<>(items);
            while (itemList.size() <= maxItems) {
                itemList.add(items.get(randomGenerator.getRandomInteger(0, items.size() - 1)));
            }
            value = String.join("&SEPARATOR&", itemList);
        }

        setParameterValue(requestParameter, value);
        return requestParameter;
    }

}
