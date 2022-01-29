package pt.uc.dei.rest_api_robustness_tester.schema;

import java.util.LinkedHashMap;
import java.util.Map;

//TODO: add support for other, dynamically loaded, Schema types (i.e., extensions of Schema)
public class TypeManager
{
    public enum Type
    {
        Number("number"),
        Integer("integer"),
        String("string"),
        Boolean("boolean"),
        Array("array"),
        Object("object"),
        Any("*");
        
        private final String type;
        Type(String type)
        {
            this.type = type;
        }
        
        public String Value()
        {
            return type;
        }
    }
    
    public enum Format
    {
        Int32(Type.Integer, "int32"),
        Int64(Type.Integer, "int64"),
        Float(Type.Number, "float"),
        Double(Type.Number, "double"),
        Byte(Type.String, "byte"),
        //TODO: binary probably cannot be represent as a simple Unicode String
        Binary(Type.String, "binary"),
        Date(Type.String, "date"),
        DateTime(Type.String, "date-time"),
        //Some Swagger (V2) APIs prefer this over date-time
        DateTimeV2(Type.String, "dateTime"),
        Password(Type.String, "password");
        
        private final Type type;
        private final String format;
        Format(Type type, String format)
        {
            this.type = type;
            this.format = format;
        }
        
        public Type Type()
        {
            return type;
        }
        
        public String Value()
        {
            return format;
        }
    }
    
    private static TypeManager instance = null;
    
    private final Map<String, Class<? extends Schema>> types;
    private final Map<String, Class<? extends Schema>> formats;
    private final Map<String, String> formatTypes;
    
    private TypeManager()
    {
        instance = this;
        types = new LinkedHashMap<>();
        formats = new LinkedHashMap<>();
        formatTypes = new LinkedHashMap<>();
        LoadDefaults();
    }
    
    public TypeManager RegisterType(String type, Class<? extends Schema> schemaClass)
    {
        types.put(type, schemaClass);
        return this;
    }
    
    public TypeManager RegisterFormat(String type, String format, Class<? extends Schema> schemaClass)
    {
        formats.put(format, schemaClass);
        formatTypes.put(format, type);
        return this;
    }
    
    public Class<? extends Schema> GetType(String type)
    {
        return types.get(type);
    }
    
    public boolean HasType(String type)
    {
        return types.containsKey(type);
    }
    
    public Class<? extends Schema> GetFormat(String format)
    {
        return formats.get(format);
    }
    
    public boolean HasFormat(String format)
    {
        return formats.containsKey(format) && formatTypes.containsKey(format);
    }
    
    public Class<? extends Schema> GetTypeFromFormat(String format)
    {
        if(formatTypes.containsKey(format))
            return types.get(formatTypes.get(format));
        
        return null;
    }
    
    public TypeManager LoadDefaults()
    {
        //Default types
        RegisterType(Type.Number.Value(), DoubleSchema.class);
        RegisterType(Type.Integer.Value(), LongSchema.class);
        RegisterType(Type.String.Value(), StringSchema.class);
        RegisterType(Type.Boolean.Value(), BooleanSchema.class);
        RegisterType(Type.Array.Value(), ArraySchema.class);
        RegisterType(Type.Object.Value(), ObjectSchema.class);
    
        //Default formats and respective base types
        RegisterFormat(Type.Integer.Value(), Format.Int32.Value(), IntegerSchema.class);
        RegisterFormat(Type.Integer.Value(), Format.Int64.Value(), LongSchema.class);
        RegisterFormat(Type.Number.Value(), Format.Float.Value(), FloatSchema.class);
        RegisterFormat(Type.Number.Value(), Format.Double.Value(), DoubleSchema.class);
        RegisterFormat(Type.String.Value(), Format.Byte.Value(), ByteSchema.class);
        RegisterFormat(Type.String.Value(), Format.Binary.Value(), StringSchema.class);
        RegisterFormat(Type.String.Value(), Format.Date.Value(), DateSchema.class);
        RegisterFormat(Type.String.Value(), Format.DateTime.Value(), DateTimeSchema.class);
        RegisterFormat(Type.String.Value(), Format.DateTimeV2.Value(), DateTimeSchema.class);
        RegisterFormat(Type.String.Value(), Format.Password.Value(), StringSchema.class);
        
        return this;
    }
    
    public TypeManager Clear()
    {
        types.clear();
        formats.clear();
        formatTypes.clear();
        return LoadDefaults();
    }
    
    public static TypeManager Instance()
    {
        return instance == null? new TypeManager() : instance;
    }
}
