package pt.uc.dei.rest_api_robustness_tester.security;

public class OpenIdConnect extends SecurityScheme
{
    public String openIdConnectUrl = null;
    
    public OpenIdConnect()
    {
        super(Type.OpenIdConnect);
    }
}
