package io.resttestgen.requestbuilder;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import io.resttestgen.requestbuilder.parameters.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class RequestParameterExtractorTest {

    private static OpenAPI openAPI;

    @Before
    public void setUp() throws Exception {
        File petstore = new File("src/test/resources/mypetstore.json");
        openAPI = new OpenAPIV3Parser().read(petstore.getPath());
    }

    @Test
    public void getPetByIdTest() throws Exception {
        RequestParameterExtractor requestBuilder = new RequestParameterExtractor(openAPI);
        final String operationId = "getPetById";

        // Ask for parameters
        List<RequestParameter> requestParameters = requestBuilder.getRequestParametersFromOperation(operationId);

        // Fill the parameters' value
        PathParameter pathParameter = (PathParameter) (requestParameters.get(0));
        pathParameter.setParameterValue("1");

        Request build = new io.resttestgen.requestbuilder.Request.Builder(openAPI, operationId).addRequestParameters(requestParameters).build().okHttpRequest;

        // Assertions
        assertEquals(1, requestParameters.size());

        assertEquals("http://localhost:8081/v2/pet/1", build.url().toString());
        assertEquals("GET", build.method());
        String accept = build.header("Accept");

        assertNotNull(accept);
        assertEquals("application/json", accept);
    }

    @Test
    public void addPetTest() throws Exception {
        RequestParameterExtractor requestBuilder = new RequestParameterExtractor(openAPI);
        final String operationId = "addPet";

        // Ask for parameters
        List<RequestParameter> requestParameters = requestBuilder.getRequestParametersFromOperation(operationId);

        // Fill parameters' value
        BodyParameter bodyParameter = (BodyParameter) (requestParameters.get(0));
        String petJson = "{\"id\":0,\"category\":{\"id\":0,\"name\":\"string\"},\"name\":\"doggie\",\"photoUrls\":[\"string\"],\"tags\":[{\"id\":0,\"name\":\"string\"}],\"status\":\"available\"}";
        bodyParameter.setParameterValue(MediaType.parse("application/json"), petJson);
        Request build = new io.resttestgen.requestbuilder.Request.Builder(openAPI, operationId).addRequestParameters(requestParameters).build().okHttpRequest;

        assertEquals(1, requestParameters.size());

        assertEquals("http://localhost:8081/v2/pet", build.url().toString());
        assertEquals("POST", build.method());

        String accept = build.header("Accept");
        assertNotNull(accept);
        assertEquals("application/json", accept);

        String contentType = build.header("Content-Type");
        assertNotNull(contentType);
        assertEquals("application/json", contentType);

        String contentLength = build.header("Content-Length");
        assertNotNull(contentLength);
        assertEquals(String.valueOf(petJson.toCharArray().length), contentLength);
    }

    @Test
    public void updatePetTest() throws Exception {
        RequestParameterExtractor requestBuilder = new RequestParameterExtractor(openAPI);
        final String operationId = "updatePet";

        // Ask for parameters
        List<RequestParameter> requestParameters = requestBuilder.getRequestParametersFromOperation(operationId);

        // Fill parameters' value
        BodyParameter bodyParameter = (BodyParameter) (requestParameters.get(0));
        String petJson = "{\"id\":0,\"category\":{\"id\":0,\"name\":\"string\"},\"name\":\"doggie\",\"photoUrls\":[\"string\"],\"tags\":[{\"id\":0,\"name\":\"string\"}],\"status\":\"available\"}";
        bodyParameter.setParameterValue(MediaType.parse("application/json"), petJson);
        Request build = new io.resttestgen.requestbuilder.Request.Builder(openAPI, operationId).addRequestParameters(requestParameters).build().okHttpRequest;

        assertEquals(1, requestParameters.size());

        assertEquals("http://localhost:8081/v2/pet", build.url().toString());
        assertEquals("PUT", build.method());

        String accept = build.header("Accept");
        assertNotNull(accept);
        assertEquals("application/json", accept);

        String contentType = build.header("Content-Type");
        assertNotNull(contentType);
        assertEquals("application/json", contentType);

        String contentLength = build.header("Content-Length");
        assertNotNull(contentLength);
        assertEquals(String.valueOf(petJson.toCharArray().length), contentLength);
    }

    @Test
    public void deletePetNotRequiredParameterTest() throws Exception {
        RequestParameterExtractor requestBuilder = new RequestParameterExtractor(openAPI);
        final String operationId = "deletePet";

        // Ask for parameters
        List<RequestParameter> requestParameters = requestBuilder.getRequestParametersFromOperation(operationId);

        // Fill parameters' value
        HeaderParameter apiKeyParameter = (HeaderParameter) (requestParameters.get(0)); // do not set
        PathParameter petIdParameter = (PathParameter) (requestParameters.get(1));

        String petId = "0";
        petIdParameter.setParameterValue(petId);
        Request build = new io.resttestgen.requestbuilder.Request.Builder(openAPI, operationId).addRequestParameters(requestParameters).build().okHttpRequest;

        assertEquals(2, requestParameters.size());

        assertEquals("http://localhost:8081/v2/pet/0", build.url().toString());
        assertEquals("DELETE", build.method());

        assertNull(build.header("Accept"));
        assertNull(build.header("api_key"));
    }

    @Test
    public void deletePetWithRequiredParameterTest() throws Exception {
        RequestParameterExtractor requestBuilder = new RequestParameterExtractor(openAPI);
        final String operationId = "deletePet";

        // Ask for parameters
        List<RequestParameter> requestParameters = requestBuilder.getRequestParametersFromOperation(operationId);

        // Fill parameters' value
        HeaderParameter apiKeyParameter = (HeaderParameter) (requestParameters.get(0));
        apiKeyParameter.setParameterValue("password");
        PathParameter petIdParameter = (PathParameter) (requestParameters.get(1));

        String petId = "0";
        petIdParameter.setParameterValue(petId);
        Request build = new io.resttestgen.requestbuilder.Request.Builder(openAPI, operationId).addRequestParameters(requestParameters).build().okHttpRequest;

        assertEquals(2, requestParameters.size());

        assertEquals("http://localhost:8081/v2/pet/0", build.url().toString());
        assertEquals("DELETE", build.method());

        assertNull(build.header("Accept"));
        assertEquals("password", build.header("api_key"));
    }

    @Test
    public void findPetByStatusSingleValueTest() throws Exception {
        RequestParameterExtractor requestBuilder = new RequestParameterExtractor(openAPI);
        final String operationId = "findPetsByStatus";

        // Ask for parameters
        List<RequestParameter> requestParameters = requestBuilder.getRequestParametersFromOperation(operationId);

        // Fill parameters' value
        QueryParameter statusQueryParameter = (QueryParameter) (requestParameters.get(0));
        statusQueryParameter.addParameterValue("available");

        Request build = new io.resttestgen.requestbuilder.Request.Builder(openAPI, operationId).addRequestParameters(requestParameters).build().okHttpRequest;

        assertEquals(1, requestParameters.size());

        assertEquals("http://localhost:8081/v2/pet/findByStatus?status=available", build.url().toString());
        assertEquals("GET", build.method());
        assertEquals("application/json", build.header("Accept"));
    }

    @Test
    public void findPetByStatusMultipleValuesTest() throws Exception {
        RequestParameterExtractor requestBuilder = new RequestParameterExtractor(openAPI);
        final String operationId = "findPetsByStatus";

        // Ask for parameters
        List<RequestParameter> requestParameters = requestBuilder.getRequestParametersFromOperation(operationId);

        // Fill parameters' value
        QueryParameter statusQueryParameter = (QueryParameter) (requestParameters.get(0));
        statusQueryParameter.addParameterValue("available");
        statusQueryParameter.addParameterValue("sold");

        Request build = new io.resttestgen.requestbuilder.Request.Builder(openAPI, operationId).addRequestParameters(requestParameters).build().okHttpRequest;

        assertEquals(1, requestParameters.size());

        assertEquals("http://localhost:8081/v2/pet/findByStatus?status=available&status=sold", build.url().toString());
        assertEquals("GET", build.method());
        assertEquals("application/json", build.header("Accept"));
    }

    @Test
    public void findPetByTagTest() throws Exception {
        RequestParameterExtractor requestBuilder = new RequestParameterExtractor(openAPI);
        final String operationId = "findPetsByTags";

        // Ask for parameters
        List<RequestParameter> requestParameters = requestBuilder.getRequestParametersFromOperation(operationId);

        // Fill parameters' value
        QueryParameter statusQueryParameter = (QueryParameter) (requestParameters.get(0));
        statusQueryParameter.addParameterValue("tag1");

        Request build = new io.resttestgen.requestbuilder.Request.Builder(openAPI, operationId).addRequestParameters(requestParameters).build().okHttpRequest;

        assertEquals(1, requestParameters.size());

        assertEquals("http://localhost:8081/v2/pet/findByTags?tags=tag1", build.url().toString());
        assertEquals("GET", build.method());
        assertEquals("application/json", build.header("Accept"));
    }

    @Test
    public void findPetByTagsTest() throws Exception {
        RequestParameterExtractor requestBuilder = new RequestParameterExtractor(openAPI);
        final String operationId = "findPetsByTags";

        // Ask for parameters
        List<RequestParameter> requestParameters = requestBuilder.getRequestParametersFromOperation(operationId);

        // Fill parameters' value
        QueryParameter statusQueryParameter = (QueryParameter) (requestParameters.get(0));
        statusQueryParameter.addParameterValue("tag1");
        statusQueryParameter.addParameterValue("tag2");

        Request build = new io.resttestgen.requestbuilder.Request.Builder(openAPI, operationId).addRequestParameters(requestParameters).build().okHttpRequest;

        assertEquals(1, requestParameters.size());

        assertEquals("http://localhost:8081/v2/pet/findByTags?tags=tag1&tags=tag2", build.url().toString());
        assertEquals("GET", build.method());
        assertEquals("application/json", build.header("Accept"));
    }
}