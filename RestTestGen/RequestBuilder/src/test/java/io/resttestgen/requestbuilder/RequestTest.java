package io.resttestgen.requestbuilder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class RequestTest {

    private static OpenAPI openAPI;

    @BeforeClass
    public static void beforeClass(){
        File petstore = new File("src/test/resources/mypetstore.json");
        openAPI = new OpenAPIV3Parser().read(petstore.getPath());
    }

    @Test
    public void completeBuildTest() {
        Request request = new Request.Builder(openAPI, "getPetById")
                .addPathParameter("petId", 5)
                .build();

        assertEquals("http://localhost:8081/v2/pet/5", request.okHttpRequest.httpUrl().toString());
    }

    @Test
    public void completeBuildTest_HeaderTrace() {
        Request request = new Request.Builder(openAPI, "getPetById")
                .addHeaderParameter("petId", 5)
                .build();

        assertEquals(3, request.traces.size());
        Assert.assertTrue(request.traces.get(1).contains(".addHeaderParameter(\"petId\", \"5\")"));
    }

    @Test
    public void completeBuildTest_PathTrace() {
        Request request = new Request.Builder(openAPI, "getPetById")
                .addPathParameter("petId", 5)
                .build();

        assertEquals(3, request.traces.size());
        Assert.assertTrue(request.traces.get(1).contains(".addPathParameter(\"petId\", \"5\")"));
    }

    @Test
    public void completeBuildTest_QueryTrace() {
        ArrayList<String> values = new ArrayList<>();
        values.add("value1");
        values.add("value2");

        Request request = new Request.Builder(openAPI, "getPetById")
                .addQueryParameter("petId", values)
                .build();

        assertEquals(3, request.traces.size());
        Assert.assertTrue(request.traces.get(1).contains(".addQueryParameter(\"petId\", new ArrayList<String>(Arrays.asList(\"value1\",\"value2\")))"));
    }

    @Test
    public void completeBuildTest_JSONBodyTrace() {
        Request request = new Request.Builder(openAPI, "getPetById")
                .addJSONRequestBody("{\"petId\": \"23\"}")
                .build();

        assertEquals(3, request.traces.size());
        Assert.assertTrue(request.traces.get(1).contains(".addJSONRequestBody("));
    }

    @Test
    public void completeBuildTest_MultipartBodyTrace() {
        Request request = new Request.Builder(openAPI, "getPetById")
                .addMultipartFormRequestBody(new Gson().fromJson("{\"key1\":\"value1\",\"key2\":\"value2\"}", new TypeToken<HashMap<String, String>>(){}.getType()))
                .build();

        assertEquals(3, request.traces.size());
        Assert.assertTrue(request.traces.get(1).contains(".addMultipartFormRequestBody(new Gson().fromJson(\"{\\\"key1\\\":\\\"value1\\\",\\\"key2\\\":\\\"value2\\\"}\", new TypeToken<HashMap<String, String>>(){}.getType()))"));
    }

    @Test
    public void uncompletedBuildRequest() {
        Request request = new Request.Builder(openAPI, "getPetById")
                .build();

        assertEquals("http://localhost:8081/v2/pet/%7BpetId%7D", request.okHttpRequest.httpUrl().toString());
    }

}