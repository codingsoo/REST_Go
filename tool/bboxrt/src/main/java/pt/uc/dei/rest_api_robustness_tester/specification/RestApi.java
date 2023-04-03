package pt.uc.dei.rest_api_robustness_tester.specification;

import pt.uc.dei.rest_api_robustness_tester.security.AuthHandlers;

public class RestApi
{
    private final String name;
    private final RestApiSpecification specification;
    private final AuthHandlers authHandlers;
    private final RestApiConfig config;
    
    public RestApi(String name, RestApiSpecification specification)
    {
        this(name, specification, new AuthHandlers(), new RestApiConfig());
    }
    
    public RestApi(String name, RestApiSpecification specification, AuthHandlers authHandlers)
    {
        this(name, specification, authHandlers, new RestApiConfig());
    }
    
    public RestApi(String name, RestApiSpecification specification, RestApiConfig config)
    {
        this(name, specification, new AuthHandlers(), config);
    }
    
    public RestApi(String name, RestApiSpecification specification, AuthHandlers authHandlers, RestApiConfig config)
    {
        this.name = name;
        this.specification = specification;
        this.authHandlers = authHandlers;
        this.config = config;
    }
    
    public String Name()
    {
        return name;
    }
    
    public RestApiSpecification Specification()
    {
        return specification;
    }
    
    public AuthHandlers AuthHandlers()
    {
        return authHandlers;
    }
    
    public RestApiConfig Config()
    {
        return config;
    }
}
