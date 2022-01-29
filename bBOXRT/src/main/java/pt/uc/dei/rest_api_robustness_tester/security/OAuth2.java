package pt.uc.dei.rest_api_robustness_tester.security;

public class OAuth2 extends SecurityScheme
{
    public OAuthFlow implicit = null;
    public OAuthFlow password = null;
    public OAuthFlow clientCredentials = null;
    public OAuthFlow authorizationCode = null;
    
    public OAuth2()
    {
        super(Type.OAuth2);
    }
}
