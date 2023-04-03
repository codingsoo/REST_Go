package pt.uc.dei.rest_api_robustness_tester.utils;

public interface Converter<X, Y>
{
    public Y Convert(X obj);
}
