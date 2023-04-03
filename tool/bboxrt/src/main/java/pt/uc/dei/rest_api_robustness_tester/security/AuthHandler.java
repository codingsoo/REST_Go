package pt.uc.dei.rest_api_robustness_tester.security;

import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

//TODO: eventually, this should be better though-out
public interface AuthHandler
{
    void HandleAuth(RequestBuilder requestBuilder);
}
