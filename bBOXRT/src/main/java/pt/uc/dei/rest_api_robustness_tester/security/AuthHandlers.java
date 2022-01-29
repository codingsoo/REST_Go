package pt.uc.dei.rest_api_robustness_tester.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AuthHandlers
{
    private final Map<String, AuthHandler> authHandlers = new HashMap<>();
    
    public AuthHandlers AddAuthHandler(String authName, AuthHandler authHandler)
    {
        this.authHandlers.put(authName, authHandler);
        return this;
    }
    
    public AuthHandler GetAuthHandler(String authName)
    {
        return authHandlers.get(authName);
    }
    
    public Set<String> GetAllAuthNames()
    {
        return authHandlers.keySet();
    }
}
