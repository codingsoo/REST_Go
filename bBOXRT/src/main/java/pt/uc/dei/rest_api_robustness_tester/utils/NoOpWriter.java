package pt.uc.dei.rest_api_robustness_tester.utils;

public class NoOpWriter implements Writer
{
    @Override
    public Writer Add(String columnName, String value)
    {
        return this;
    }
    
    @Override
    public Writer Write(String collectionName)
    {
        return this;
    }
}
