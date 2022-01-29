package io.resttestgen.nominaltester.helper;

import io.resttestgen.nominaltester.models.ExecutionParameter;
import io.resttestgen.nominaltester.models.HTTPMethod;
import io.resttestgen.nominaltester.models.OperationInfo;
import io.resttestgen.nominaltester.models.TestStep;
import io.resttestgen.nominaltester.testers.OperationTester;
import io.resttestgen.nominaltester.testers.operationtestingstrategies.BruteForceDependenciesOperationTester;
import io.resttestgen.swagger2depgraph.InputDependencyGraph;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * NOTE: not used
 */
public class CleaningOperationHelper {

    static final Logger logger = LogManager.getLogger(CleaningOperationHelper.class);

    private Map<String, List<OperationInfo>> operationsPerApiClass;
    private InputDependencyGraph inputDependencyGraph;
    private OpenAPI openAPI;

    /**
     * CleaningOperationHelper's constructor
     * @param operationsPerApiClass Map< ApiClassName, List of Operation Info>
     * @param idg Instance of the input dependency graph
     */
    public CleaningOperationHelper(Map<String, List<OperationInfo>> operationsPerApiClass, InputDependencyGraph idg, OpenAPI openAPI) {
        this.operationsPerApiClass = operationsPerApiClass;
        this.inputDependencyGraph = idg;
        this.openAPI = openAPI;
    }

    /**
     * Get a queue of Test Step (operation, parameters) necessary for the cleaning of the target operations
     * @param target test step we want to clean up
     * @return Queue of cleaning test steps
     */
    public Queue<TestStep> getCleaningOperations(TestStep target) throws Exception {
        // Get target operation Info and check for matching cleaning operationInfo
        // (if POST -> matching is a DELETE)
        HTTPMethod targetHttpMethod = target.getTargetOperation().getHttpMethod();
        String invocationClassName = target.getTargetOperation().getInvocationClassName();
        String classModelName = invocationClassName.replace("Api", "").toLowerCase(); // PetApi -> pet
        List<OperationInfo> operationInTheSameClass = this.operationsPerApiClass.get(invocationClassName);

        LinkedList<TestStep> cleaningSteps = new LinkedList<>();
        switch (targetHttpMethod) {
            case POST:
                // Try to search for the field "model + id" (es. petId)
                // If not try to search just for the first field with "id"
                Object fieldValue = getValueByFirstMatchingFieldName(target.getExecutionParameters(), classModelName + "Id");
                if (fieldValue == null) fieldValue = getValueByFirstMatchingFieldName(target.getExecutionParameters(), "id");
                if (fieldValue == null) return cleaningSteps;

                // Get list of matching cleaning operations
                List<OperationInfo> deleteOperations = operationInTheSameClass.stream()
                        .filter(o -> o.getHttpMethod().equals(HTTPMethod.DELETE))
                        .collect(Collectors.toList());

                // For each matching operations
                // Check if their input contains the target id-field-name
                OperationTester tester = new BruteForceDependenciesOperationTester(openAPI, operationsPerApiClass);
                for (OperationInfo deleteOperation : deleteOperations) {
                    List<ExecutionParameter> actualParameters = tester.getActualParameters(deleteOperation);

                    // Replace first occurrences of "model" + "id"
                    // if does not apply, try to replace first occurrences of "id"
                    boolean successful = setFirstFieldOccurrence(actualParameters, classModelName + "Id", fieldValue);
                    if (!successful) successful = setFirstFieldOccurrence(actualParameters, "id", fieldValue);
                    if (successful) {
                        TestStep testStep = new TestStep(deleteOperation,actualParameters);
                        cleaningSteps.add(testStep);
                    }
                }
                return cleaningSteps;
            default:
                return cleaningSteps;
        }
    }

    private boolean setFirstFieldOccurrence(List<ExecutionParameter> actualParameters, String fieldname, Object newValue) throws InvocationTargetException, IllegalAccessException {
        for (ExecutionParameter executionParameter : actualParameters) {
            String parameterName = executionParameter.getSanitizedName();
            Schema parameterSchema = executionParameter.getParameterSchema();

            // Check if matching parameters
            boolean matchingFieldName = parameterName.equalsIgnoreCase(fieldname);
            if (matchingFieldName) {
                executionParameter.setValue(newValue);
                return true;
            }

            // If not a matching parameter and it is an object, check recursively inside the object's properties
            if (parameterSchema.getType() != null && parameterSchema.getType().equals("object") || parameterSchema.get$ref() != null) {
                Method setter = ReflectionHelper.getMethodByName(executionParameter.getValue().getClass(), "set" + fieldname);
                if (setter != null) {
                    Object value = setter.invoke(newValue);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the first field by name
     *
     * @param executionParameters List of executionParameters
     * @return Matching parameter Id
     */
    private Object getValueByFirstMatchingFieldName(List<ExecutionParameter> executionParameters, String fieldname) throws InvocationTargetException, IllegalAccessException {
        for (ExecutionParameter executionParameter : executionParameters) {
            String parameterName = executionParameter.getSanitizedName();
            Schema parameterSchema = executionParameter.getParameterSchema();
            Object parameterValue = executionParameter.getValue();

            // Check if matching parameters
            boolean matchingFieldName = parameterName.equalsIgnoreCase(fieldname);
            if (matchingFieldName) return parameterValue;

            // If not a matching parameter and it is an object, check recursively inside the object's properties
            if (parameterSchema.getType() != null && parameterSchema.getType().equals("object") || parameterSchema.get$ref() != null) {
                Method getter = ReflectionHelper.getMethodByName(executionParameter.getValue().getClass(), "get" + fieldname);
                if (getter != null) {
                    return getter.invoke(executionParameter.getValue());
                }
            }
        }
        return null; // no matching fields
    }
}
