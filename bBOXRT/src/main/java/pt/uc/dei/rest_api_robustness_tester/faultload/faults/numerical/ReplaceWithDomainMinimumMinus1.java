package pt.uc.dei.rest_api_robustness_tester.faultload.faults.numerical;

import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class ReplaceWithDomainMinimumMinus1 implements Fault
{
    @Override
    public String FaultName()
    {
        return "Replace with domain minimum - 1";
    }
    
    @Override
    public String[] AcceptedTypes()
    {
        return new String[]
                {
                        TypeManager.Type.Integer.Value(),
                        TypeManager.Type.Number.Value()
                };
    }
    
    @Override
    public String[] AcceptedFormats()
    {
        return new String[]
                {
                        TypeManager.Format.Int32.Value(),
                        TypeManager.Format.Int64.Value(),
                        TypeManager.Format.Float.Value(),
                        TypeManager.Format.Double.Value()
                };
    }
    
    @Override
    public boolean IsPreconditionRespected(SchemaBuilder schema)
    {
        return (Arrays.asList(AcceptedTypes()).contains(schema.type) ||
                Arrays.asList(AcceptedFormats()).contains(schema.format)) &&
                schema.minimum != null;
    }
    
    @Override
    public String Inject(String valueRepresentation, SchemaBuilder schema)
    {
        try
        {
            int value = Integer.parseInt(valueRepresentation);
            BigInteger bigValue = new BigInteger(schema.minimum);
            return "" + bigValue.subtract(BigInteger.ONE);
        }
        catch(Exception ignored) {}
    
        try
        {
            float value = Float.parseFloat(valueRepresentation);
            BigDecimal bigValue = new BigDecimal(schema.minimum);
            return "" + bigValue.subtract(BigDecimal.ONE);
        }
        catch(Exception ignored) {}
    
        try
        {
            long value = Long.parseLong(valueRepresentation);
            BigInteger bigValue = new BigInteger(schema.minimum);
            return "" + bigValue.subtract(BigInteger.ONE);
        }
        catch(Exception ignored) {}
    
        try
        {
            double value = Double.parseDouble(valueRepresentation);
            BigDecimal bigValue = new BigDecimal(schema.minimum);
            return "" + bigValue.subtract(BigDecimal.ONE);
        }
        catch(Exception ignored) {}
    
        return valueRepresentation;
    }
}
