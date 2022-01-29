package pt.uc.dei.rest_api_robustness_tester.security;

import java.util.ArrayList;
import java.util.List;

public abstract class SecurityScheme
{
    //TODO: add support for other, dynamically loaded, auth types
    public enum Type
    {
        ApiKey("apiKey"),
        HTTP("http"),
        OAuth2("oauth2"),
        OpenIdConnect("openIdConnect");
        
        private final String value;
        
        private Type(String value)
        {
            this.value = value;
        }
        
        public String Value()
        {
            return this.value;
        }
    
        public static Type GetTypeForValue(String value)
        {
            for(Type t : Type.values())
                if(value.equalsIgnoreCase(t.Value()))
                    return t;
            return null;
        }
    }
    
    public final Type type;
    
    public SecurityScheme(Type type)
    {
        this.type = type;
    }
}

