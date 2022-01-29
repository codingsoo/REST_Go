package io.resttestgen.errortester.violators;

import io.resttestgen.requestbuilder.parameters.RequestParameter;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.resttestgen.errortester.mutator.ParameterMutationHelper.getParameterValue;
import static io.resttestgen.errortester.mutator.ParameterMutationHelper.setParameterValue;

public class MinItemsViolator implements Violator {

    @Override
    public RequestParameter applyViolation(RequestParameter requestParameter) {
        Schema parameterSchema = requestParameter.getParameterSchema();
        Integer minItems = parameterSchema.getMinItems();
        if (minItems == null) return null;

        String value = getParameterValue(requestParameter);
        ArrayList<String> items = new ArrayList<>(Arrays.asList(value.split("&SEPARATOR&")));

        if (minItems == 1) {
            value = null;
        } else {
            List<String> itemList = items.subList(0, minItems - 1);
            value = String.join("&SEPARATOR&", itemList);
        }

        setParameterValue(requestParameter, value);
        return requestParameter;
    }

}
