package io.resttestgen.requestbuilder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import io.resttestgen.requestbuilder.parameters.*;
import io.resttestgen.swaggerschema.SchemaExtractor;
import io.resttestgen.swaggerschema.models.HTTPMethod;
import io.resttestgen.swaggerschema.models.SwaggerOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Request {

    public SwaggerOperation operation;
    public String operationId;
    public String requestId;
    public com.squareup.okhttp.Request okHttpRequest;
    public List<String> traces;

    public Request() {
        traces = new ArrayList<>();
    }

    public static class Builder {

        private final String FIREFOX_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0";
        private com.squareup.okhttp.Request.Builder builder = new com.squareup.okhttp.Request.Builder();
        private com.squareup.okhttp.RequestBody requestBody = null;
        private String operationId;
        private SwaggerOperation operation;
        private String requestUrl;
        public String requestId;
        private List<String> traces;

        public Builder(OpenAPI openAPI, String operationId) {
            this.traces = new ArrayList<>();
            this.operationId = operationId;
            this.operation = getOperationSchema(openAPI, operationId);
            this.requestUrl = openAPI.getServers().get(0).getUrl() + operation.getPath();

            // add initial "Accept" and "User-Agent" headers
            HashSet<String> acceptedTypes = getAcceptedTypes(operation.getOperationSchema());
            if (acceptedTypes.size() > 0){
                builder.header("Accept", String.join(", ", acceptedTypes));
            }
            builder.header("User-Agent", FIREFOX_USER_AGENT);

            // add constructor trace
            String constructorTrace = String.format("Request %s = new Request.Builder(openAPI, \"%s\")", operationId + "_req", operationId);
            this.traces.add(constructorTrace);
        }

        public Builder(OpenAPI openAPI, String operationId, String requestId) {
            this.traces = new ArrayList<>();
            this.operationId = operationId;
            this.operation = getOperationSchema(openAPI, operationId);
            this.requestUrl = openAPI.getServers().get(0).getUrl() + operation.getPath();
            this.requestId = requestId;

            // add initial "Accept" and "User-Agent" headers
            HashSet<String> acceptedTypes = getAcceptedTypes(operation.getOperationSchema());
            if (acceptedTypes.size() > 0){
                builder.header("Accept", String.join(", ", acceptedTypes));
            }
            builder.header("User-Agent", FIREFOX_USER_AGENT);

            // add constructor trace
            String constructorTrace = String.format("Request %s = new Request.Builder(openAPI, \"%s\")", operationId + "_req_" + requestId, operationId);
            this.traces.add(constructorTrace);
        }

        /**
         * Add a RequestParameter to request
         * @param requestParameter request parameter to add (should have a value set)
         * @return Builder
         */
        public Builder addRequestParameter(RequestParameter requestParameter) {
            if (!requestParameter.isValueSet()) {
                return this;
            }

            String parameterIn = requestParameter.getParameterIn();
            switch (parameterIn) {
                case "body":
                    BodyParameter bodyParameter = (BodyParameter) requestParameter;
                    String mediaType = bodyParameter.getMediaType().toString();
                    if (mediaType.contains("json")) {
                        String value;
                        Object parameterValue = bodyParameter.getParameterValue();
                        if (parameterValue instanceof JsonObject) {
                            value = parameterValue.toString();
                        } else {
                            value = (String) parameterValue;
                        }
                        addJSONRequestBody(value);
                    } else if (mediaType.contains("xml")) {
                        addXMLRequestBody((String)bodyParameter.getParameterValue());
                    } else if (mediaType.contains("multipart") || mediaType.contains("form")) {
                        addMultipartFormRequestBody((Map<String, String>)bodyParameter.getParameterValue());
                    }
                    break;
                case "header":
                    String headerValue = ((HeaderParameter) requestParameter).getParameterValue();
                    addHeaderParameter(requestParameter.getParameterName(), headerValue);
                    break;
                case "query":
                    QueryParameter queryParameter = (QueryParameter) requestParameter;
                    addQueryParameter(queryParameter.getParameterName(), queryParameter.getParameterValue());
                    break;
                default:
                    PathParameter pathParameter = (PathParameter) requestParameter;
                    addPathParameter(requestParameter.getParameterName(), pathParameter.getParameterValue());
            }

            return this;
        }

        /**
         * Add a set of RequestParameters to the request
         * @param requestParameters list of request parameters to add
         * @return Builder
         */
        public Builder addRequestParameters(List<RequestParameter> requestParameters) {
            for (RequestParameter requestParameter : requestParameters) {
                addRequestParameter(requestParameter);
            }
            return this;
        }

        /**
         * Get a list of accepted content-types for a current operations
         * @param operationSchema target operation
         * @return set of accepted content-types
         */
        private HashSet<String> getAcceptedTypes(Operation operationSchema) {
            Set<Map.Entry<String, ApiResponse>> responseEntries = operationSchema.getResponses().entrySet();
            HashSet<String> acceptedTypes = new HashSet<>();
            for (Map.Entry<String, ApiResponse> responseEntry : responseEntries) {
                if (responseEntry.getValue().getContent() != null) {
                    acceptedTypes.addAll(responseEntry.getValue().getContent().keySet());
                }
            }
            return acceptedTypes;
        }

        /**
         * Create a map (OperationId -> SwaggerOperation)
         * @return  Map linking OperationId with SwaggerOperation Object
         */
        private static SwaggerOperation getOperationSchema(OpenAPI openAPI, String targetOperationId) {
            return SchemaExtractor.getOperationsMap(openAPI).get(targetOperationId);
        }

        /**
         * Add a new header field
         * @param headerField header field name
         * @param value header field value
         * @return Builder
         */
        public Builder addHeaderParameter(String headerField, Object value) {
            String valueStr = String.valueOf(value);
            builder.header(headerField, valueStr);

            // add header trace
            String headerParameterTrace = String.format("\t.addHeaderParameter(\"%s\", \"%s\")", headerField, value);
            this.traces.add(headerParameterTrace);

            return this;
        }

        /**
         * Add a new path parameter
         * @param parameterField path parameter field name
         * @param value path parameter field value
         * @return Builder
         */
        public Builder addPathParameter(String parameterField, Object value) {
            String valueStr = String.valueOf(value);
            String toReplace = String.format("{%s}", parameterField);
            this.requestUrl = this.requestUrl.replace(toReplace, valueStr);

            // add header trace
            String pathParameterTrace = String.format("\t.addPathParameter(\"%s\", \"%s\")", parameterField, value);
            this.traces.add(pathParameterTrace);

            return this;
        }

        /**
         * Add a new query parameter
         * @param parameterField query parameter field name
         * @param values array of values for the query parameter
         * @return Builder
         */
        public Builder addQueryParameter(String parameterField, ArrayList<String> values) {
            for (Object parameterValue : values) {
                String parameterValueStr = String.valueOf(parameterValue);
                HttpUrl.Builder httpBuider = HttpUrl.parse(this.requestUrl).newBuilder();
                httpBuider.addQueryParameter(parameterField, parameterValueStr);
                this.requestUrl = httpBuider.build().toString();
            }

            // Traces
            String valuesTraces = values.stream().map(x -> String.format("\"%s\"", x))
                    .collect( Collectors.joining( "," ));
            String arrayInitialization = String.format("new ArrayList<String>(Arrays.asList(%s))", valuesTraces);
            String queryParameterTraces = String.format("\t.addQueryParameter(\"%s\", %s)", parameterField, arrayInitialization);
            this.traces.add(queryParameterTraces);

            return this;
        }

        /**
         * Add a new JSON Request Body
         * @param jsonContent JSON body content
         * @return Builder
         */
        public Builder addJSONRequestBody(String jsonContent) {
            MediaType mediaType = MediaType.parse("application/json");
            requestBody = com.squareup.okhttp.RequestBody.create(mediaType, jsonContent);
            builder.header("Content-Type", mediaType.toString());
            try {
                builder.header("Content-Length", String.valueOf(requestBody.contentLength()));
            } catch (IOException e) {
                // do not add header
            }

            String escapedJsonContent = StringEscapeUtils.escapeJson(jsonContent);
            escapedJsonContent = escapedJsonContent.replace("\\/", "/"); // in case of url

            // Traces
            String jsonBodyTrace = String.format("\t.addJSONRequestBody(\"%s\")", escapedJsonContent);
            this.traces.add(jsonBodyTrace);

            return this;
        }

        /**
         * Add a new JSON Request Body
         * @param complexObj JSON body content
         * @return Builder
         */
        public Builder addJSONRequestBody(Object complexObj) {
            String s = new Gson().toJson(complexObj);
            return addJSONRequestBody(s);
        }

        /**
         * Add a new XML Request Body
         * @param xmlContent XML body content
         * @return Builder
         */
        public Builder addXMLRequestBody(String xmlContent) {
            MediaType mediaType = MediaType.parse("text/xml");
            requestBody = com.squareup.okhttp.RequestBody.create(mediaType, xmlContent);
            builder.header("Content-Type", mediaType.toString());
            try {
                builder.header("Content-Length", String.valueOf(requestBody.contentLength()));
            } catch (IOException e) {
                // do not add header
            }

            // Traces
            String xmlBodyTraces = String.format("\t.addXMLRequestBody(\"%s\")", xmlContent);
            this.traces.add(xmlBodyTraces);

            return this;
        }

        /**
         * Add a new MultipartForm Request Body
         * @param formValues Multipart Form body content
         * @return Builder
         */
        public Builder addMultipartFormRequestBody(Map<String, String> formValues) {
            MediaType mediaType = MediaType.parse("text/xml");
            MultipartBuilder multipartBuilder = new MultipartBuilder();
            multipartBuilder.type(MultipartBuilder.FORM);
            for (Map.Entry<String, String> entry : formValues.entrySet()) {
                multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
            requestBody = multipartBuilder.build();
            builder.header("Content-Type", mediaType.toString());
            try {
                builder.header("Content-Length", String.valueOf(requestBody.contentLength()));
            } catch (IOException e) {
                // do not add header
            }

            // Trace
            String json = new Gson().toJson(formValues);
            json = json.replace("\"", "\\\"");
            String gsonDeserialization = String.format("new Gson().fromJson(\"%s\", new TypeToken<HashMap<String, String>>(){}.getType())", json);
            String multipartTrace = String.format("\t.addMultipartFormRequestBody(%s)\n", gsonDeserialization);
            this.traces.add(multipartTrace);

            return this;
        }


        /**
         * Build a new request
         * @return requestbuilder.Request object from the builder parameters
         */
        public Request build(){
            Request request = new Request();

            this.traces.add("\t.build();");

            request.operation = this.operation;
            request.operationId = this.operationId;
            request.traces = this.traces;
            request.requestId = this.requestId;

            // set url
            builder.url(this.requestUrl);

            if ((operation.getHttpMethod().equals(HTTPMethod.POST) || operation.getHttpMethod().equals(HTTPMethod.PUT) ||
                    operation.getHttpMethod().equals(HTTPMethod.PATCH)) && requestBody == null) {
                requestBody = com.squareup.okhttp.RequestBody.create(null, new byte[0]);
            }

            // set http method and reqBody
            if (!operation.getHttpMethod().equals(HTTPMethod.GET))
                builder.method(operation.getHttpMethod().toString(), requestBody);

            request.okHttpRequest = builder.build();

            return request;
        }

    }

    public void setRequestId(String requestId) {
        String currentName = operationId + "_req";
        if (this.requestId != null && !this.requestId.isEmpty()) currentName += "_" + this.requestId;

        List<String> oldTraces = traces.subList(1, traces.size());
        String newCustructorTrace = traces.get(0).replace(currentName, operationId + "_req_" + requestId);
        List<String> newTraces = new ArrayList<>();

        newTraces.add(newCustructorTrace);
        newTraces.addAll(oldTraces);

        this.traces = newTraces;
        this.requestId = requestId;
    }

}
