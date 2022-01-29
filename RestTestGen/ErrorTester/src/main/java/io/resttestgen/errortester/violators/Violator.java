package io.resttestgen.errortester.violators;

import io.resttestgen.requestbuilder.parameters.RequestParameter;

public interface Violator {
    RequestParameter applyViolation(RequestParameter requestParameter);
}
