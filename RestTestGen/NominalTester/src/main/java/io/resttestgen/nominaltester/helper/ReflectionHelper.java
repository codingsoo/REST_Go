package io.resttestgen.nominaltester.helper;

import io.resttestgen.nominaltester.fieldgenerator.RandomGenerator;
import org.apache.commons.lang3.ClassUtils;
import org.threeten.bp.format.DateTimeParseException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains useful methods to simplify method extractions from a class
 */
public class ReflectionHelper {

    private static RandomGenerator randomGenerator = new RandomGenerator();

    /**
     * Get Method from its name and its parameters
     * @param methodName name of the method to search
     * @param myClass class containing the method to search
     * @param paramsClasses classes of the parameters
     * @return matching method or null if not found
     */
    public static Method getMethod(Class<?> myClass, String methodName, Class... paramsClasses) {
        if (myClass == null) return null;
        try {
            return myClass.getMethod("valueOf", paramsClasses);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     *
     * Try to cast target instance to targetClass
     * 1) try to cast directly
     * 2) try to use valueOf method if exists
     * 3) try to look for a random matching targetClass's fields
     * If none of these methods work, return null
     *
     * @param targetClass target class
     * @param instance target object to cast
     * @return casted object or null
     */
    public static Object tryToCastInstance(Class targetClass, Object instance) {
        if (instance == null) return null;

        // Check if instance is on the same type of the target class
        // If it is, cast it and return
        try {
            return targetClass.cast(instance);
        } catch (ClassCastException ignored) {
        }

        // Try to cast from string to any
        try {
            String s = (String)instance;
            String classname = targetClass.getSimpleName().toLowerCase();
            switch (classname) {
                case "offsetdatetime":
                    return org.threeten.bp.OffsetDateTime.parse(s);
                case "localdate":
                    return org.threeten.bp.LocalDate.parse(s);
                case "boolean":
                    return Boolean.valueOf(s);
                case "integer":
                case "int":
                case "long":
                case "double":
                    instance = Double.valueOf(s);
            }
        } catch (ClassCastException | NumberFormatException ignored) {
            // It is not a string
        }
        catch (DateTimeParseException e ) {
            int a = 3;
        }

        // Try to cast to double to see if it is a number
        try {
            Double d = (Double)instance;
            String classname = targetClass.getSimpleName().toLowerCase();
            switch (classname) {
                case "long":
                    return d.longValue();
                case "integer":
                case "int":
                    return d.intValue();
                case "bigdecimal":
                    return new BigDecimal(d);
                case "biginteger":
                    return BigInteger.valueOf(d.longValue());
                case "string":
                    String doubleStr = d.toString();
                    if (doubleStr.endsWith(".0"))
                        doubleStr = doubleStr.replace(".0", "");
                    return doubleStr;
                case "double":
                    return d;
            }
        } catch (ClassCastException ignored) {
            // It is not a number
        }

        // Try to cast to string
        try {
            Boolean b = (Boolean)instance;
            String classname = targetClass.getSimpleName().toLowerCase();
            switch (classname) {
                case "string":
                    return b.toString();
            }
        } catch (ClassCastException ignored) {
            // It is not a string
        }

        // If instance is a complex object, try to look for a random field with matching type
        if (!ReflectionHelper.isPrimitiveOrWrapped(instance)) {
            try {
                return ReflectionHelper.getRandomFieldValueFromGetter(instance, targetClass);
            } catch (InvocationTargetException | IllegalAccessException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Get Method given his name (case insensitive)
     * @param myClass target class
     * @param methodName methd name
     * @return Matching Method object
     */
    public static Method getMethodByName(Class<?> myClass, String methodName) {
        if (myClass == null) return null;
        Method[] declaredMethods = myClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().equalsIgnoreCase(methodName)) return declaredMethod;
        }
        return null;
    }

    /**
     * Get all the methods which name contains a specific substring
     *
     * @param myClass target class
     * @param substring string to search inside methods' name
     * @return Matching list of methods containing that substring
     */
    public static List<Method> getMethodsThatContain(Class<?> myClass, String substring) {
        Method[] declaredMethods = myClass.getDeclaredMethods();
        return Arrays.stream(declaredMethods).filter(m -> m.getName().contains(substring)).collect(Collectors.toList());
    }

    /**
     * Get all the methods which name starts with a specific substring
     *
     * @param myClass target class
     * @param startingSubString starting method's name substring
     * @return List of matching methods
     */
    public static List<Method> getMethodsThatStartWith(Class<?> myClass, String startingSubString) {
        Method[] declaredMethods = myClass.getDeclaredMethods();
        return Arrays.stream(declaredMethods).filter(m -> m.getName().startsWith(startingSubString)).collect(Collectors.toList());
    }

    /**
     * @param instance complex object containing different fields
     * @param requestedReturnType class type which must returned by the getter
     * @param <T> type must be returned by the getter
     * @return value of the field from the getter
     * @throws InvocationTargetException error during getter invocation
     * @throws IllegalAccessException error during getter invocation
     */
    public static <T> T getRandomFieldValueFromGetter(Object instance, Class<T> requestedReturnType) throws InvocationTargetException, IllegalAccessException {
        List<Method> getters = ReflectionHelper.getMethodsThatStartWith(instance.getClass(), "get");
        List<Method> gettersWithMatchingReturnType = getters.stream().
                filter(g -> g.getReturnType().equals(requestedReturnType)).collect(Collectors.toList());
        Method selectedGetter = randomGenerator.getRandomElementFromCollection(gettersWithMatchingReturnType);
        if (selectedGetter == null) return null;
        return (T) selectedGetter.invoke(instance);
    }

    /**
     * Get all getters methods from class
     *
     * @param myClass target class
     * @return List of matching methods
     */
    public static List<Method> getGetters(Class<?> myClass) {
        List<Method> getters = ReflectionHelper.getMethodsThatStartWith(myClass, "get");
        return getters.stream().filter(g -> g.getParameterCount() == 0).collect(Collectors.toList());
    }


    /**
     * Check if the object is from primitive type (double, int, Integer etc)
     * @param value target object to check
     * @return true if primitive or wrapper or null
     */
    public static boolean isPrimitiveOrWrapped(Object value) {
        if (value == null) return true;
        return ClassUtils.isPrimitiveOrWrapper(value.getClass())
                || value.getClass() == String.class;
    }

    /**
     * Check if the class is a primitive or wrapper class (double, int, Integer etc)
     * @param clazz target object to check
     * @return true if primitive or wrapper or null
     */
    public static boolean isPrimitiveOrWrapped(Class clazz) {
        if (clazz == null) return true;
        return ClassUtils. isPrimitiveOrWrapper(clazz)
                || clazz == String.class;
    }

}
