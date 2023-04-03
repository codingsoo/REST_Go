package pt.uc.dei.rest_api_robustness_tester.utils;

public interface Writer
{
    Writer Add(String columnName, String value);
    Writer Write(String collectionName);
}
