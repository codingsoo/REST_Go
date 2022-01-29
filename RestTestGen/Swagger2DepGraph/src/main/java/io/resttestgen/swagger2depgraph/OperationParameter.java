package io.resttestgen.swagger2depgraph;

import opennlp.tools.stemmer.PorterStemmer;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * OperationParameter it is wrapper around the parameterName
 * which overrides the equals method to compare the names also
 * in their object-parameter-notation
 */
public class OperationParameter {
    private final String parameterName;

    public OperationParameter(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * Gets the parameter name
     * @return string representing parameter name
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Check if two OperationParameter are equals
     * 1. It compares the two raw parameters' name
     * 2. If are different, it compares the parameter names in object-parameter-notation
     * @param o other object to compare
     * @return true if they refers to the same parameter name, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationParameter otherParameter = (OperationParameter) o;
        String otherParameterName = otherParameter.getParameterName();

        if (otherParameterName.equals(parameterName)) return true;

        // compare field in object notation petId to pet:id
        String parameterObjectNotation = convertToObjectParameterNotation(parameterName);
        String otherParameterObjectNotation = convertToObjectParameterNotation(otherParameterName);
        if (otherParameterObjectNotation.equals(parameterObjectNotation)) return true;

        if (parameterObjectNotation.endsWith(otherParameterObjectNotation)
                || otherParameterObjectNotation.endsWith(parameterObjectNotation)) return true;

        // apply stemming
        PorterStemmer porterStemmer = new PorterStemmer();
        String stemParameter = porterStemmer.stem(parameterObjectNotation);
        String stemOtherParameter = porterStemmer.stem(otherParameterObjectNotation);

        return stemOtherParameter.equals(stemParameter);
    }

    /**
     * Converts a string to object parameter notation
     * Camel case or '_' are converted to ':'
     * do nothing if input string contains already ":"
     * @param parameter String to convert to object parameter notation
     * @return string in object parameter notation
     */
    private String convertToObjectParameterNotation(String parameter) {
        parameter = StringUtils.join(
                StringUtils.splitByCharacterTypeCamelCase(parameter),
                ':'
        );
        parameter = parameter.replace(":_:", ":");
        parameter = parameter.replace(":-:", ":");
        parameter = parameter.replace(": :", ":");
        parameter = parameter.replace(":::", ":");
        return parameter.toLowerCase();
    }

    @Override
    public int hashCode() {
        // hash of the last two parts of the parameter object notation
        String parameterObjectNotation = convertToObjectParameterNotation(parameterName);
        String[] split = parameterObjectNotation.split(":");
        String toHash = parameterObjectNotation;
        if (split.length > 2) {
            toHash = split[split.length - 2] + ":" +  split[split.length - 1];
        }
        return Objects.hash(toHash);
    }
}
