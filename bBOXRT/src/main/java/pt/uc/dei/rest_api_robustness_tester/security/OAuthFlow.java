package pt.uc.dei.rest_api_robustness_tester.security;

import java.util.ArrayList;
import java.util.List;

public class OAuthFlow
{
    public String authorizationUrl = null;
    public String tokenUrl = null;
    public String refreshUrl = null;
    public List<String> scopes = new ArrayList<>();
}
