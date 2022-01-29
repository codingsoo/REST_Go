package pt.uc.dei.rest_api_robustness_tester.media;

import pt.uc.dei.rest_api_robustness_tester.schema.Schema;

public interface SchemaFormatter<S extends Schema>
{
    String MediaType();
    Class<? extends Schema> SchemaType();
    String Serialize(S schema);
    
    String GetElementValue(String value, String ... nameHierarchy) throws Exception;
    String SetElementValue(String value, String newValue, String ... nameHierarchy) throws Exception;
}

