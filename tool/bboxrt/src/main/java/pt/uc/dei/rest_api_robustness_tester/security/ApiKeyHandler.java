package pt.uc.dei.rest_api_robustness_tester.security;

import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

public class ApiKeyHandler implements AuthHandler
{
    private final ApiKey apiKey;
    private final String key;
    
    public ApiKeyHandler(ApiKey apiKey, String key)
    {
        this.apiKey = apiKey;
        this.key = key;
    }
    
    @Override
    public void HandleAuth(RequestBuilder requestBuilder)
    {
        switch (apiKey.location)
        {
            case Header:
                requestBuilder.addHeader(apiKey.name, key);
                break;
            case Cookie:
                System.out.println("Cookie auth handling not supported");
                break;
            case Query:
                requestBuilder.addParameter(apiKey.name, key);
                break;
        }
    }
}
