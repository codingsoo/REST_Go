package io.resttestgen.errortester.violators;

import io.resttestgen.requestbuilder.parameters.RequestParameter;

public class RequiredMissingViolator implements Violator {
    @Override
    public RequestParameter applyViolation(RequestParameter requestParameter) {
        if (requestParameter == null)
            return null;

        if (requestParameter.getRequired() != null && requestParameter.getRequired()) {
            return null;
        }

        return requestParameter;
    }
}
