package io.resttestgen.nominaltester.fieldgenerator;

import io.swagger.v3.oas.models.media.Schema;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetDateTime;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Extends to RandomGenerator to support constrained random generated value using swagger schema
 */
public class SwaggerRandomGenerator extends RandomGenerator {

    public SwaggerRandomGenerator(int seed) {
        super(seed);
    }

    public SwaggerRandomGenerator() {
        super();
    }

    /**
     * Creates a random value based on schema constraints
     * @param schema parameter schema
     * @return newly created object
     */
    protected Object createRandomObjectFromSchema(Schema schema) {
        String type = schema.getType();
        String format = schema.getFormat();
        Integer[] range = selectRandomRange();
        switch (type){
            case "string":
                if (format != null && format.equals("date-time")) {
                    return createRandomDate(schema);
                }
                if (format != null && format.equals("date")) {
                    return createLocalDate(schema);
                }
                if (format != null && format.equals("uuid")) {
                    return UUID.randomUUID();
                }
                if (format != null && format.equals("email")) {
                    return getRandomString(5, 10, true, false) + "@gmail.com";
                }
                return createStringGivenSwaggerSchema(schema);
            case "number":
                if (format == null || format.equals("integer"))
                    return createBigDecimalGivenSwaggerSchema(schema, BigDecimal.valueOf(range[0]), BigDecimal.valueOf(range[1]));
                if (format.equals("float")) {
                    BigDecimal d = createBigDecimalGivenSwaggerSchema(schema, BigDecimal.valueOf(range[0]), BigDecimal.valueOf(range[1]));
                    return d.floatValue();
                }
                return createDoubleGivenSwaggerSchema(schema, Double.valueOf(range[0]), Double.valueOf(range[1]));
            case "integer":
                if (format != null && format.contains("64")){
                    return createLongGivenSwaggerSchema(schema, Long.valueOf(range[0]), Long.valueOf(range[1]));
                }
                return createIntegerGivenSwaggerSchema(schema, range[0], range[1]);
            case "boolean":
                return 1 == getRandomInteger(0, 2); // 0 or 1
            default:
                return null;
        }
    }

    /**
     * Create a new OffsetDateTime
     * @param schema swagger schema with type string
     * @return new Date, now, now - 30 days or now + 30 days
     */
    private OffsetDateTime createRandomDate(Schema schema) {
        int timeChoice = getRandomInteger(0, 3); // 0, 1, 2
        int daysOffset = getRandomInteger(1, 30);
        switch (timeChoice) {
            case 0:
                // past
                return OffsetDateTime.now().minusDays(daysOffset);
            case 1:
                // future
                return OffsetDateTime.now().plusDays(daysOffset);
            default:
                // present
                return OffsetDateTime.now();
        }
    }

    /**
     * Create a new LocalDate
     * @param schema swagger schema with type string
     * @return new Date, now, now - 30 days or now + 30 days
     */
    private LocalDate createLocalDate(Schema schema) {
        int timeChoice = getRandomInteger(0, 3); // 0, 1, 2
        int daysOffset = getRandomInteger(1, 30);
        switch (timeChoice) {
            case 0:
                // past
                return LocalDate.now().minusDays(daysOffset);
            case 1:
                // future
                return LocalDate.now().plusDays(daysOffset);
            default:
                // present
                return LocalDate.now();
        }
    }

    /**
     * Returns an array identifying a [MIN_VALUE, MAX_VALUE] using the following criteria:
     * 1/3 -> [0, 10[
     * 1/3 -> [10, 100[
     * 1/3 -> [Integer.MIN_VALUE, Integer.MAX_VALUE[
     * @return array with two integers, identifier lower and upper bound
     */
    private Integer[] selectRandomRange() {
        int choice = getRandomInteger(0, 3); // {0, 1, 2}
        switch (choice) {
            case 0:
                return new Integer[]{0, 10};
            case 1:
                return new Integer[]{10, 100};
            default:
                return new Integer[]{Integer.MIN_VALUE, Integer.MAX_VALUE};
        }
    }

    /**
     * Create a new String given the constraints in the schema
     * @param schema swagger schema with type string
     * @return new random string
     */
    private String createStringGivenSwaggerSchema(Schema schema) {
        int maxLength = (schema.getMaxLength() != null) ? schema.getMaxLength() : 20;
        int minLength = (schema.getMinLength() != null) ? schema.getMinLength() : 2;
        return getRandomString(minLength, maxLength, true, false);
    }

    /**
     * Create a new Double given the constraints in the schema
     * @param schema swagger schema with type number
     * @return new random Double
     */
    private Double createDoubleGivenSwaggerSchema(Schema schema) {
        return createDoubleGivenSwaggerSchema(schema, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    /**
     * Create a new Double given the constraints in the schema
     * @param schema swagger schema with type number
     * @param min lower bound to use if not specified in swagger schema
     * @param max upper bound to use if not specified in the swagger schema
     * @return new random Double between the specified range
     */
    private Double createDoubleGivenSwaggerSchema(Schema schema, Double min, Double max) {
        BigDecimal bigMinValue = (schema.getMinimum() != null) ? schema.getMinimum() : BigDecimal.valueOf(min);
        BigDecimal bigMaxValue = (schema.getMaximum() != null) ? schema.getMaximum() : BigDecimal.valueOf(max);
        double selectedMinimum = bigMinValue.doubleValue();
        double selectedMaximum = bigMaxValue.doubleValue();
        return getRandomDouble(selectedMinimum, selectedMaximum);
    }

    /**
     * Create a new BigDecimal given the constraints in the schema
     * @param schema swagger schema with type number
     * @return new random Double
     */
    private BigDecimal createBigDecimalGivenSwaggerSchema(Schema schema) {
        return BigDecimal.valueOf(createDoubleGivenSwaggerSchema(schema));
    }

    /**
     * Create a new BigDecimal given the constraints in the schema
     * @param schema swagger schema with type number
     * @param min lower bound to use if not specified in swagger schema
     * @param max upper bound to use if not specified in the swagger schema
     * @return new random Double
     */
    private BigDecimal createBigDecimalGivenSwaggerSchema(Schema schema, BigDecimal min, BigDecimal max) {
        double minDouble = min.doubleValue();
        double maxDouble = max.doubleValue();
        return BigDecimal.valueOf(createDoubleGivenSwaggerSchema(schema, minDouble, maxDouble));
    }

    /**
     * Create a new Long given the constraints in the schema
     * @param schema swagger schema with type integer (uint64)
     * @return new random Long
     */
    private Long createLongGivenSwaggerSchema(Schema schema) {
        return createLongGivenSwaggerSchema(schema, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     * Create a new Long given the constraints in the schema
     * @param schema swagger schema with type integer (uint64)
     * @param min lower bound to use if not specified in swagger schema
     * @param max upper bound to use if not specified in the swagger schema
     * @return new random Long
     */
    private Long createLongGivenSwaggerSchema(Schema schema, Long min, Long max) {
        BigDecimal bigMinValue = (schema.getMinimum() != null) ? schema.getMinimum() : BigDecimal.valueOf(min);
        BigDecimal bigMaxValue = (schema.getMaximum() != null) ? schema.getMaximum() : BigDecimal.valueOf(max);
        long minValue = bigMinValue.longValue();
        long maxValue = bigMaxValue.longValue();
        return getRandomLong(minValue, maxValue);
    }

    /**
     * Create a new Integer given the constraints in the schema
     * @param schema swagger schema with type integer
     * @return new random Integer
     */
    private Integer createIntegerGivenSwaggerSchema(Schema schema) {
        return createIntegerGivenSwaggerSchema(schema, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Create a new Integer given the constraints in the schema
     * @param schema swagger schema with type integer
     * @param min lower bound to use if not specified in swagger schema
     * @param max upper bound to use if not specified in the swagger schema
     * @return new random Integer between the specified range
     */
    private Integer createIntegerGivenSwaggerSchema(Schema schema, Integer min, Integer max) {
        BigDecimal bigMinValue = (schema.getMinimum() != null) ? schema.getMinimum() : BigDecimal.valueOf(min);
        BigDecimal bigMaxValue = (schema.getMaximum() != null) ? schema.getMaximum() : BigDecimal.valueOf(max);
        int minValue = bigMinValue.toBigInteger().intValueExact();
        int maxValue = bigMaxValue.toBigInteger().intValueExact();
        return getRandomInteger(minValue, maxValue);
    }
}