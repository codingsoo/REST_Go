package io.resttestgen.errortester.mutator;

import io.resttestgen.requestbuilder.parameters.HeaderParameter;
import io.resttestgen.requestbuilder.parameters.PathParameter;
import io.resttestgen.requestbuilder.parameters.QueryParameter;
import io.resttestgen.requestbuilder.parameters.RequestParameter;

import java.util.ArrayList;
import java.util.Arrays;

public class ParameterMutationHelper {
    public static void setParameterValue(RequestParameter requestParameter, String value) {
        switch (requestParameter.getParameterType()) {
            case QUERY:
                QueryParameter queryParameter = (QueryParameter) requestParameter;
                if (value == null) {
                    queryParameter.setParameterValues(new ArrayList<>());
                } else {
                    String[] split = value.split("&SEPARATOR&");
                    queryParameter.setParameterValues(new ArrayList<>(Arrays.asList(split)));
                }
                break;
            case PATH:
                PathParameter pathParameter = (PathParameter) requestParameter;
                if (value == null)
                    value = "";
                pathParameter.setParameterValue(value);
                break;
            case HEADER:
                HeaderParameter headerParameter = (HeaderParameter) requestParameter;
                if (value == null)
                    value = "";
                headerParameter.setParameterValue(value);
                break;
        }
    }

    public static String getParameterValue(RequestParameter requestParameter) {
        String value = null;
        switch (requestParameter.getParameterType()) {
            case QUERY:
                QueryParameter queryParameter = (QueryParameter) requestParameter;
                ArrayList<String> queryParameterValues = queryParameter.getParameterValue();
                value = String.join("&SEPARATOR&", queryParameterValues);
                break;
            case PATH:
                PathParameter pathParameter = (PathParameter) requestParameter;
                value = pathParameter.getParameterValue();
                break;
            case HEADER:
                HeaderParameter headerParameter = (HeaderParameter) requestParameter;
                value = headerParameter.getParameterValue();
                break;
        }

        return value;
    }
}
