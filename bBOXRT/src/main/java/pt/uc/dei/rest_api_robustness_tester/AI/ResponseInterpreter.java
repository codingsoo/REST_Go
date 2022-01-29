package pt.uc.dei.rest_api_robustness_tester.AI;

import pt.uc.dei.rest_api_robustness_tester.response.ResponseInRequest;
import pt.uc.dei.rest_api_robustness_tester.workload.GeneratedWorkloadRequest;

public class ResponseInterpreter {

    public enum ErrorType {NotSet, MajorError, MinorError, Inconclusive, False500, NoContent}

    public void interpreterResponse(GeneratedWorkloadRequest workload){

        ResponseInRequest response;
        if(workload.getResponseInRequest().size() > 0)
            response = workload.getResponseInRequest().get(0);
        else
            return;


        String content = response.getContent().toLowerCase();
        if(content.contains("No content in response")){
            response.setErrorType(ErrorType.NoContent);
        }
        else if(
                    response.getStatusCode().Is5xx()
                    &&  (
                           content.contains(("<head><title>HTTP Status 500 – Internal Server Error</title>").toLowerCase())
                        || content.contains(("Internal Error").toLowerCase())
                        || content.contains(("INTERNAL_SERVER_ERROR").toLowerCase())
                        || content.contains(("<head><title>502 Bad Gateway</title></head>").toLowerCase())
                        || content.contains(("Internal server exception").toLowerCase())
                        || content.contains(("Something went wrong").toLowerCase())
                        || content.contains(("<error>Internal server error</error>").toLowerCase())
                        || content.contains(("Unexpected/unknown exception").toLowerCase())
                        || content.contains(("unknown failure").toLowerCase())
                        || content.contains(("OutOfMemoryError").toLowerCase())
                    )
                )
        {
            response.setErrorType(ErrorType.MajorError);
        }
        else if(
                response.getStatusCode().Is4xx()
                        && (
                            //the status code 400 (client error, not server-side) is accompanied by an NPE error.
                            // This is a serious exception very often related to robustness issues
                            // (i.e., failure to check a variable for nullarity),
                            content.contains(("NullPointerException").toLowerCase())
                        )
        )
        {
            response.setErrorType(ErrorType.MajorError);
        }

    }
}
