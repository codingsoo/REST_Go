package pt.uc.dei.rest_api_robustness_tester.faultload.faults;

import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;

public interface Fault
{
    String FaultName();
    String[] AcceptedTypes();
    String[] AcceptedFormats();
    boolean IsPreconditionRespected(SchemaBuilder schema);
    String Inject(String value, SchemaBuilder schema);
}
