package io.resttestgen.nominaltester.testers;

import io.resttestgen.nominaltester.fieldgenerator.FieldsGenerator;
import io.resttestgen.nominaltester.fieldgenerator.exceptions.FieldGenerationException;
import io.resttestgen.nominaltester.fieldgenerator.exceptions.TypeNotHandledException;
import io.resttestgen.nominaltester.fieldgenerator.manufacturetraces.ManufactureTraces;
import io.resttestgen.nominaltester.helper.ResponseValidator;
import io.resttestgen.nominaltester.helper.TestStepExecutor;
import io.resttestgen.nominaltester.helper.exceptions.ApiResponseParsingException;
import io.resttestgen.nominaltester.models.*;
import io.resttestgen.nominaltester.models.exceptions.ParametersMismatchException;
import io.resttestgen.nominaltester.testers.exceptions.OperationExecutionException;
import io.resttestgen.swaggerschema.models.ValidationError;
import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Tester class contains methods and fields required to test operations
 * E.g. It has a response dictionary which is used during the parameter generation and
 * the method testOperation to execute the operation, getting the coverage
 */
public abstract class OperationTester extends Tester {

    static final Logger logger = LogManager.getLogger(OperationTester.class);

    protected ResponseValidator responseValidator;
    protected FieldsGenerator fieldsGenerator;

    public OperationTester(OpenAPI openAPI, Map<String, List<OperationInfo>> operationsPerApiClass) throws SchemaValidationException {
        super(openAPI, operationsPerApiClass);
        responseValidator = new ResponseValidator(openAPI);
        fieldsGenerator = new FieldsGenerator(openAPI);
    }

    /**
     * Generates the parameter and call the operation's function
     * @param operation operation to execute
     * @param operationResult operation result (to add exception then)
     * @return executed test step
     * @throws ApiResponseParsingException error during parsing of the server's response
     * @throws OperationExecutionException error during operation execution
     * @throws TypeNotHandledException error during field generation
     * @throws FieldGenerationException error during parameter generation
     */
    public TestStep execOperation(OperationInfo operation, OperationResult operationResult) throws ApiResponseParsingException, OperationExecutionException, TypeNotHandledException, FieldGenerationException, ParametersMismatchException {
        List<ExecutionParameter> generatedActualParameters = getActualParameters(operation);
        TestStep testStep = new TestStep(operation, generatedActualParameters);
        ExecutionResult executionResult = TestStepExecutor.execute(testStep);
        testStep.setExecutionResult(executionResult);
        try {
            List<ValidationError> errors = responseValidator.checkResponseSchemaValidity(testStep);
            executionResult.setValidationErrors(errors);
        } catch (Exception e) {
            logger.error("Error during operation execution");
            operationResult.getExceptions().add(e.getClass().getName() + ":" + e.getMessage());
        }
        return testStep;
    }

    /**
     * Extracts and generates values for input parameter required for the target operation
     * @param operation operation from which extract actual parameter
     * @return List of parameters (filled) ready to be executed
     * @throws TypeNotHandledException error during the field generation
     * @throws FieldGenerationException error during parameter generation
     */
    public List<ExecutionParameter> getActualParameters(OperationInfo operation) throws TypeNotHandledException, FieldGenerationException, ParametersMismatchException {

        // Use the set of required parameter if exists, otherwise use the whole set of parameters
        List<ExecutionParameter> allParameters = operation.getExecutionParameters();
        List<ExecutionParameter> requiredParameters = allParameters.stream().filter(ExecutionParameter::isRequired).collect(Collectors.toList());

        // Demand the choice of using just required parameter or all the parameters to the randomness
        // 3/4 use just required
        // 1/4 use all parameters
        boolean useJustRequiredParameters = new Random().nextInt(4) <= 2; // [0, 1, 2,] 3
        List<ExecutionParameter> actualParameters = useJustRequiredParameters ? requiredParameters : allParameters;

        // Create values
        for (ExecutionParameter requestParameter : actualParameters) {
            // Get target class type
            Type parameterizedType = requestParameter.getParameter().getParameterizedType();
            Class<?> targetClass = requestParameter.getParameter().getType();
            if (parameterizedType instanceof ParameterizedType) {
                targetClass = (Class<?>) ((ParameterizedType)parameterizedType).getActualTypeArguments()[0];
            }

            // Get information from parameters
            Schema swaggerSchema = requestParameter.getParameterSchema();
            String parameterName = requestParameter.getSanitizedName();
            ManufactureTraces manufactureTraces = new ManufactureTraces();

            // Create a new object and add it as a value inside the ExecutionParameter
            Object value = this.fieldsGenerator.manufacturePojo(targetClass, swaggerSchema, manufactureTraces, parameterName);
            requestParameter.setValue(value);
            requestParameter.setManufactureTraces(manufactureTraces);

        }
        return actualParameters;
    }
}
