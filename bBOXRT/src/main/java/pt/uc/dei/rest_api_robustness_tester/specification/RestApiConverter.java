package pt.uc.dei.rest_api_robustness_tester.specification;

import pt.uc.dei.rest_api_robustness_tester.utils.Converter;

public interface RestApiConverter<X> extends Converter<X, RestApiSpecification>
{
    public RestApiSpecification Convert(X obj);
}
