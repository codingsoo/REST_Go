package pt.uc.dei.rest_api_robustness_tester.specification;

public interface RestApiSetup
{
    RestApi Load(String apiYamlPath);
}
