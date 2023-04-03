package pt.uc.dei.rest_api_robustness_tester.security;

public class Http extends SecurityScheme
{
    public String scheme = null;
    public String bearerFormat = null;
    
    public Http()
    {
        super(Type.HTTP);
    }
}
