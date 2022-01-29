import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIResolver;
import io.swagger.v3.parser.OpenAPIV3Parser;
import pt.uc.dei.rest_api_robustness_tester.specification.OpenApiToRestApi;
import pt.uc.dei.rest_api_robustness_tester.specification.RestApi;
import pt.uc.dei.rest_api_robustness_tester.specification.RestApiConverter;
import pt.uc.dei.rest_api_robustness_tester.specification.RestApiSpecification;
import pt.uc.dei.rest_api_robustness_tester.specification.RestApiSetup;

import java.io.File;

public class test implements RestApiSetup
{
    @Override
    public RestApi Load(String apiYamlPath)
    {

        File file = new File(apiYamlPath);
        OpenAPI openAPI = new OpenAPIV3Parser().read(file.getAbsolutePath());
        OpenAPIResolver res = new OpenAPIResolver(openAPI);
        openAPI = res.resolve();

        RestApiConverter<OpenAPI> converter = new OpenApiToRestApi();
        RestApiSpecification restAPISpecification = converter.Convert(openAPI);

        return new RestApi("Test API", restAPISpecification);
    }
}
