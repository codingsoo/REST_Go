package pt.uc.dei.rest_api_robustness_tester.media;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFilePrinter;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.joda.time.DateTime;

import java.lang.reflect.Field;
import java.util.*;

public class Test
{
    public static void main(String[] args) throws Exception
    {
//        ClassPool pool = ClassPool.getDefault();
//        CtClass pointClass = pool.makeClass("Point");
//        CtField i = new CtField(CtClass.intType, "i", pointClass);
//        i.setModifiers(Modifier.PUBLIC);
//        pointClass.addField(i);
//
//        CtClass stringClass = pool.get("java.lang.String");
//        CtField str = new CtField(stringClass, "str", pointClass);
//        str.setModifiers(Modifier.PUBLIC);
//        pointClass.addField(str);
//
//        CtClass subpointClass = pool.makeClass("Subpoint");
//        CtField f = new CtField(CtClass.floatType, "f", subpointClass);
//        f.setModifiers(Modifier.PUBLIC);
//        subpointClass.addField(f);
//        CtField subpoint = new CtField(subpointClass, "subpoint", pointClass);
//        subpoint.setModifiers(Modifier.PUBLIC);
//        pointClass.addField(subpoint, "new Subpoint()");
//
//        ConstPool pointConstPool = pointClass.getClassFile().getConstPool();
//        AnnotationsAttribute pointAnnotation = new AnnotationsAttribute(pointConstPool, AnnotationsAttribute.visibleTag);
//        Annotation pointXmlAnnotation = new Annotation(pointConstPool,
//                pool.get("com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement"));
//        pointXmlAnnotation.addMemberValue("localName", new StringMemberValue("PointClass", pointConstPool));
//        pointAnnotation.setAnnotation(pointXmlAnnotation);
//        pointClass.getClassFile().addAttribute(pointAnnotation);
//
//        ClassFilePrinter.print(pointClass.getClassFile());
//        ClassFilePrinter.print(subpointClass.getClassFile());
//
//        Class<?> subpointClazz = subpointClass.toClass();
//        Class<?> pointClazz = pointClass.toClass();
//        Object pointInstance = pointClazz.newInstance();
//        Object subpointInstance = pointClazz.getField("subpoint").get(pointInstance);
//        subpointClazz.getField("f").setFloat(subpointInstance, 3.1415f);
//        pointClazz.getField("i").setInt(pointInstance, 123);
//        pointClazz.getField("str").set(pointInstance, DateTime.now().toString("yyyy-MM-dd'T'HH:mm:ss"));
//        String json = new ObjectMapper().writeValueAsString(pointInstance);
//        String xml = new XmlMapper().writeValueAsString(pointInstance);
//        System.out.println(json);
//        System.out.println(xml);
//
//        Object instanceFromXml = new XmlMapper().readValue(xml, pointClazz);
//        assert pointClazz.getField("i").getInt(instanceFromXml) == 123;
//        Object instanceFromJson = new ObjectMapper().readValue(json, pointClazz);
//        assert pointClazz.getField("i").getInt(instanceFromJson) == 123;
        
        MetaClass baseClass = new CustomMetaClass("com.stuff.BaseClass", null);
        baseClass.AddField("integer", NativeMetaClass.Integer);
        baseClass.AddField("string", NativeMetaClass.String);
    
        MetaClass subClass = new CustomMetaClass("com.stuff.SubClass", null);
        subClass.AddField("float", NativeMetaClass.Float);
        subClass.AddField("string", NativeMetaClass.String);
        //subClass.AddField("otherSub", subClass);
        baseClass.AddField("sub1", subClass);
        baseClass.AddField("sub2", subClass);
        
        baseClass.Build();
    
        System.out.println(new ObjectMapper().writeValueAsString(MetaObjectManager.Initialize(NativeMetaClass.Boolean)));
        System.out.println(new ObjectMapper().writeValueAsString(MetaObjectManager.Initialize(NativeMetaClass.String)));
        System.out.println(new ObjectMapper().writeValueAsString(MetaObjectManager.Initialize(NativeMetaClass.Integer)));
        System.out.println(new ObjectMapper().writeValueAsString(MetaObjectManager.Initialize(NativeMetaClass.Long)));
        System.out.println(new ObjectMapper().writeValueAsString(MetaObjectManager.Initialize(NativeMetaClass.Float)));
        System.out.println(new ObjectMapper().writeValueAsString(MetaObjectManager.Initialize(NativeMetaClass.Double)));
        System.out.println(new ObjectMapper().writeValueAsString(MetaObjectManager.Initialize(subClass)));
        System.out.println(new ObjectMapper().writeValueAsString(MetaObjectManager.Initialize(baseClass)));
    }
}

interface Initializer
{
    Object Initialize() throws ReflectiveOperationException;
}

class MetaObjectManager
{
    private static final Map<String, MetaObjectRecord> registry = new LinkedHashMap<>();
    
    static
    {
        try
        {
            NativeMetaClass.Boolean.Build();
            NativeMetaClass.String.Build();
            NativeMetaClass.Integer.Build();
            NativeMetaClass.Long.Build();
            NativeMetaClass.Float.Build();
            NativeMetaClass.Double.Build();
        } catch (Exception e)
        {
            System.err.println("Failed to build a NativeMetaClass");
            e.printStackTrace();
        }
    }

    public static void Register(MetaObject metaObject, Initializer initializer)
    {
        registry.put(metaObject.metaClass.className, new MetaObjectRecord(metaObject, initializer));
    }
    
    public static Object Initialize(String className) throws ReflectiveOperationException
    {
        return Initializer(className).Initialize();
    }
    
    public static Object Initialize(MetaClass metaClass) throws ReflectiveOperationException
    {
        return Initializer(metaClass).Initialize();
    }
    
    public static boolean Has(String className)
    {
        return registry.containsKey(className);
    }
    
    public static boolean Has(MetaClass metaClass)
    {
        return Has(metaClass.className);
    }
    
    protected static MetaObject MetaObject(String className)
    {
        return registry.get(className).metaObject;
    }
    
    protected static MetaObject MetaObject(MetaClass metaClass)
    {
        return MetaObject(metaClass.className);
    }
    
    protected static Initializer Initializer(String className)
    {
        return registry.get(className).initializer;
    }
    
    protected static Initializer Initializer(MetaClass metaClass)
    {
        return Initializer(metaClass.className);
    }

    private static class MetaObjectRecord
    {
        private final MetaObject metaObject;
        private final Initializer initializer;

        private MetaObjectRecord(MetaObject metaObject, Initializer initializer)
        {
            this.metaObject = metaObject;
            this.initializer = initializer;
        }
    }
}

abstract class MetaClass
{
    protected final String className;
    protected final Stringifier stringifier;
    protected final Map<String, MetaClass> fields;
    
    protected MetaClass(String className, Stringifier stringifier)
    {
        this.className = className;
        this.stringifier = stringifier;
        this.fields = new LinkedHashMap<>();
    }
    
    public abstract MetaClass AddField(String fieldName, MetaClass metaClass);
    public abstract MetaClass RemoveField(String fieldName);
    public abstract MetaClass GetField(String fieldName);
    
    protected abstract void Build() throws NotFoundException, CannotCompileException, ReflectiveOperationException;
}

class NativeMetaClass extends MetaClass
{
    public static NativeMetaClass Boolean = new NativeMetaClass("java.lang.Boolean", new BooleanConverter());
    public static NativeMetaClass String = new NativeMetaClass("java.lang.String", new StringConverter());
    public static NativeMetaClass Integer = new NativeMetaClass("java.lang.Integer", new IntegerConverter());
    public static NativeMetaClass Long = new NativeMetaClass("java.lang.Long", new LongConverter());
    public static NativeMetaClass Float = new NativeMetaClass("java.lang.Float", new FloatConverter());
    public static NativeMetaClass Double = new NativeMetaClass("java.lang.Double", new DoubleConverter());
    
    private NativeMetaClass(String className, Stringifier stringifier)
    {
        super(className, stringifier);
    }
    
    @Override
    public MetaClass AddField(String fieldName, MetaClass metaClass)
    {
        return this;
    }
    
    @Override
    public MetaClass RemoveField(String fieldName)
    {
        return this;
    }
    
    @Override
    public MetaClass GetField(String fieldName)
    {
        return null;
    }
    
    @Override
    protected void Build() throws ReflectiveOperationException
    {
        if(MetaObjectManager.Has(this.className))
            return;
        
        String arg = "";
        switch(className)
        {
            case "java.lang.Boolean":
                arg = "false";
                break;
            case "java.lang.Integer":
            case "java.lang.Long":
                arg = "0";
                break;
            case "java.lang.Float":
            case "java.lang.Double":
                arg = "0.0";
                break;
        }
        String finalArg = arg;
        Class<?> classObject = Class.forName(this.className);
        Initializer init = () -> classObject.getConstructor(java.lang.String.class).newInstance(finalArg);
        MetaObject metaObject = new NativeMetaObject(this);
        MetaObjectManager.Register(metaObject, init);
    }
}

class CustomMetaClass extends MetaClass
{
    public CustomMetaClass(String className, Stringifier stringifier)
    {
        super(className, stringifier);
    }
    
    @Override
    public MetaClass AddField(String fieldName, MetaClass metaClass)
    {
        this.fields.put(fieldName, metaClass);
        return this;
    }
    
    @Override
    public MetaClass RemoveField(String fieldName)
    {
        this.fields.remove(fieldName);
        return this;
    }
    
    @Override
    public MetaClass GetField(String fieldName)
    {
        return this.fields.get(fieldName);
    }
    
    @Override
    protected void Build() throws NotFoundException, CannotCompileException, ReflectiveOperationException
    {
        if(MetaObjectManager.Has(this.className))
            return;
        
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.makeClass(className);

        for (String fieldName : fields.keySet())
        {
            MetaClass fieldMetaClass = fields.get(fieldName);
            String fieldClassName = fieldMetaClass.className;
            if(fieldMetaClass != this)
                fieldMetaClass.Build();
            CtClass fieldCtClass = classPool.get(fieldClassName);
            CtField ctField = new CtField(fieldCtClass, fieldName, ctClass);
            ctField.setModifiers(Modifier.PUBLIC);
            ctClass.addField(ctField);
        }
        
        Class<?> classObject = ctClass.toClass();
        MetaObject metaObject = new CustomMetaObject(this);

        Initializer init = () ->
        {
            Object instance = classObject.newInstance();
            
            for (String fieldName : fields.keySet())
            {
                MetaClass fieldMetaClass = fields.get(fieldName);
                String fieldClassName = fieldMetaClass.className;
                Object fieldInstance = MetaObjectManager.Initializer(fieldClassName).Initialize();
                metaObject.SetFieldValue(instance, fieldInstance, fieldName);
            }
            
            return instance;
        };
    
        MetaObjectManager.Register(metaObject, init);
    }
}

abstract class MetaObject
{
    protected final MetaClass metaClass;
    
    protected MetaObject(MetaClass metaClass)
    {
        this.metaClass = metaClass;
    }
    
    public Object SetValue(String stringValue)
    {
        return metaClass.stringifier.Destringify(stringValue);
    }
    
    public abstract String GetFieldValue(Object instance, String ... hierarchicalFieldName)
            throws ReflectiveOperationException;
    public abstract void SetFieldValue(Object instance, Object value, String ... hierarchicalFieldName)
            throws ReflectiveOperationException;
    public abstract void SetFieldValue(Object instance, String stringValue, String ... hierarchicalFieldName)
            throws ReflectiveOperationException;
}

class NativeMetaObject extends MetaObject
{
    protected NativeMetaObject(MetaClass metaClass)
    {
        super(metaClass);
    }
    
    @Override
    public String GetFieldValue(Object instance, String... hierarchicalFieldName)
    {
        return null;
    }
    
    @Override
    public void SetFieldValue(Object instance, Object value, String... hierarchicalFieldName)
    {
    
    }
    
    @Override
    public void SetFieldValue(Object instance, String stringValue, String... hierarchicalFieldName)
    {
    
    }
}

class CustomMetaObject extends MetaObject
{
    protected CustomMetaObject(MetaClass metaClass)
    {
        super(metaClass);
    }
    
    private MetaField GetField(Object instance, String... hierarchicalFieldName) throws ReflectiveOperationException
    {
        Class<?> enclosingClass = null;
        Object enclosingInstance = null;
        MetaClass fieldMetaClass = metaClass;
        Field field = null;
        
        for(String fieldName : hierarchicalFieldName)
        {
            enclosingClass = enclosingClass == null? Class.forName(metaClass.className) : field.getType();
            enclosingInstance = enclosingInstance == null? instance: field.get(enclosingInstance);
            
            fieldMetaClass = fieldMetaClass.fields.get(fieldName);
            field = enclosingClass.getField(fieldName);
        }
    
        if(fieldMetaClass != null && field != null && enclosingInstance != null)
            return new MetaField(fieldMetaClass, field, enclosingInstance);
        
        StringJoiner stringJoiner = new StringJoiner(".");
        for(String fieldName : hierarchicalFieldName)
            stringJoiner.add(fieldName);
        throw new NoSuchFieldException("The field " + stringJoiner.toString() + " does not exist in the class of " +
                "type " + this.metaClass.className);
    }
    
    @Override
    public String GetFieldValue(Object instance, String... hierarchicalFieldName) throws ReflectiveOperationException
    {
        MetaField f = GetField(instance, hierarchicalFieldName);
        return f.fieldMetaClass.stringifier.Stringify(f.field.get(f.enclosingInstance));
    }
    
    @Override
    public void SetFieldValue(Object instance, Object value, String... hierarchicalFieldName) throws ReflectiveOperationException
    {
        MetaField f = GetField(instance, hierarchicalFieldName);
        f.field.set(f.enclosingInstance, value);
    }
    
    @Override
    public void SetFieldValue(Object instance, String stringValue, String... hierarchicalFieldName) throws ReflectiveOperationException
    {
        MetaField f = GetField(instance, hierarchicalFieldName);
        f.field.set(f.enclosingInstance, f.fieldMetaClass.stringifier.Destringify(stringValue));
    }
    
    private static class MetaField
    {
        final MetaClass fieldMetaClass;
        final Field field;
        final Object enclosingInstance;
        
        private MetaField(MetaClass fieldMetaClass, Field field, Object enclosingInstance)
        {
            this.fieldMetaClass = fieldMetaClass;
            this.field = field;
            this.enclosingInstance = enclosingInstance;
        }
    }
}

abstract class Stringifier<V>
{
    public String Stringify(Object value)
    {
        return "" + value;
    }
    
    public abstract V Destringify(String value);
}

class BooleanConverter extends Stringifier<Boolean>
{
    @Override
    public Boolean Destringify(String value)
    {
        return Boolean.valueOf(value);
    }
}

class StringConverter extends Stringifier<String>
{
    @Override
    public String Destringify(String value)
    {
        return value;
    }
}

class IntegerConverter extends Stringifier<Integer>
{
    @Override
    public Integer Destringify(String value)
    {
        return Integer.valueOf(value);
    }
}

class LongConverter extends Stringifier<Long>
{
    @Override
    public Long Destringify(String value)
    {
        return Long.valueOf(value);
    }
}

class FloatConverter extends Stringifier<Float>
{
    @Override
    public Float Destringify(String value)
    {
        return Float.valueOf(value);
    }
}

class DoubleConverter extends Stringifier<Double>
{
    @Override
    public Double Destringify(String value)
    {
        return Double.valueOf(value);
    }
}
