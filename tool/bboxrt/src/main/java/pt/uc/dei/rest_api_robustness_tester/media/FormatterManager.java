package pt.uc.dei.rest_api_robustness_tester.media;

import pt.uc.dei.rest_api_robustness_tester.schema.*;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class FormatterManager
{
    public enum MediaType
    {
        JSON("application/json"),
        XML("application/xml"),
        //FIXME: This is a go-to formatter for the Wildcard media type
        WILDCARD("*/*");
        
        private final String mediaType;
        MediaType(String mediaType)
        {
            this.mediaType = mediaType;
        }
        
        public String Value()
        {
            return mediaType;
        }
    }
    
    private static FormatterManager instance = null;
    
    private static final SchemaFormatter DEFAULT = new NoOpFormatter();
    
    private final Map<String, SchemaFormatter<? extends Schema>> formatters;
    private final Set<String> mediaTypes;
    
    private FormatterManager()
    {
        instance = this;
        formatters = new LinkedHashMap<>();
        mediaTypes = new LinkedHashSet<>();
        LoadDefaults();
    }
    
    public FormatterManager RegisterFormatter(SchemaFormatter<? extends Schema> formatter)
    {
        RegisterFormatter(formatter.MediaType(), formatter.SchemaType(), formatter);
        return this;
    }
    
    public FormatterManager RegisterFormatter(String mediaType, Class<? extends Schema> schemaType,
                                              SchemaFormatter<? extends Schema> formatter)
    {
        String cleanedMediaType = CleanMediaTypeString(mediaType);
        formatters.put(KeyFromSchemaFormatter(cleanedMediaType, schemaType), formatter);
        mediaTypes.add(cleanedMediaType);
        return this;
    }
    
    public SchemaFormatter GetFormatter(String mediaType, Class<? extends Schema> schemaType)
    {
        String cleanedMediaType = CleanMediaTypeString(mediaType);
        return formatters.get(KeyFromSchemaFormatter(cleanedMediaType, schemaType));
    }
    
    public boolean HasFormatter(String mediaType, Class<? extends Schema> schemaType)
    {
        String cleanedMediaType = CleanMediaTypeString(mediaType);
        return formatters.containsKey(KeyFromSchemaFormatter(cleanedMediaType, schemaType));
    }
    
    public boolean HasFormatterFor(String mediaType)
    {
        String cleanedMediaType = CleanMediaTypeString(mediaType);
        return mediaTypes.contains(cleanedMediaType);
    }
    
    public SchemaFormatter GetDefaultFormatter()
    {
        return DEFAULT;
    }
    
    public FormatterManager LoadDefaults()
    {
        //Default formatters
        //JSON
        ArrayJsonFormatter jsonArray = new ArrayJsonFormatter();
        GenericPrimitiveFormatter<BooleanSchema> jsonBoolean = new GenericPrimitiveFormatter<>(MediaType.JSON.Value(), BooleanSchema.class);
        GenericPrimitiveFormatter<DoubleSchema> jsonDouble = new GenericPrimitiveFormatter<>(MediaType.JSON.Value(), DoubleSchema.class);
        GenericPrimitiveFormatter<FloatSchema> jsonFloat = new GenericPrimitiveFormatter<>(MediaType.JSON.Value(), FloatSchema.class);
        GenericPrimitiveFormatter<IntegerSchema> jsonInteger = new GenericPrimitiveFormatter<>(MediaType.JSON.Value(), IntegerSchema.class);
        GenericPrimitiveFormatter<LongSchema> jsonLong = new GenericPrimitiveFormatter<>(MediaType.JSON.Value(), LongSchema.class);
        ObjectJsonFormatter jsonObject = new ObjectJsonFormatter();
        StringJsonFormatter jsonString = new StringJsonFormatter();
        
        RegisterFormatter(jsonArray);
        RegisterFormatter(jsonBoolean);
        RegisterFormatter(jsonDouble);
        RegisterFormatter(jsonFloat);
        RegisterFormatter(jsonInteger);
        RegisterFormatter(jsonLong);
        RegisterFormatter(jsonObject);
        RegisterFormatter(jsonString);
        RegisterFormatter(MediaType.JSON.Value(), ByteSchema.class, jsonString);
        RegisterFormatter(MediaType.JSON.Value(), DateSchema.class, jsonString);
        RegisterFormatter(MediaType.JSON.Value(), DateTimeSchema.class, jsonString);
        
        return this;
    }
    
    public FormatterManager Clear()
    {
        formatters.clear();
        mediaTypes.clear();
        return LoadDefaults();
    }
    
    private String KeyFromSchemaFormatter(String mediaType, Class<? extends Schema> schemaType)
    {
        return mediaType + "_" + schemaType.getSimpleName();
    }
    
    private String CleanMediaTypeString(String mediaType)
    {
        if(mediaType.contains(";"))
            return mediaType.split(";")[0].trim();
        return mediaType.trim();
    }
    
    public static FormatterManager Instance()
    {
        return instance == null? new FormatterManager() : instance;
    }
}
