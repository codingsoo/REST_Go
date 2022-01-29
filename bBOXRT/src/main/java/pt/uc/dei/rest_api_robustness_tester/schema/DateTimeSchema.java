package pt.uc.dei.rest_api_robustness_tester.schema;

import org.apache.commons.lang3.RandomUtils;
import org.joda.time.LocalDateTime;

import java.util.Random;

public class DateTimeSchema extends DateSchema
{
    protected static final int DEFAULT_MIN_HOUR = 0;
    protected static final int DEFAULT_MAX_HOUR = 23;
    protected static final int DEFAULT_MIN_MINUTE = 0;
    protected static final int DEFAULT_MAX_MINUTE = 59;
    protected static final int DEFAULT_MIN_SECOND = 0;
    protected static final int DEFAULT_MAX_SECOND = 59;
    
    DateTimeSchema(SchemaBuilder builder)
    {
        super(builder);
    }
    
    @Override
    public Schema<String> New() throws Exception
    {
        int day = RandomUtils.nextInt(DEFAULT_MIN_DAY, DEFAULT_MAX_DAY);
        int month = RandomUtils.nextInt(DEFAULT_MIN_MONTH, DEFAULT_MAX_MONTH);
        int year = RandomUtils.nextInt(DEFAULT_MIN_YEAR, DEFAULT_MAX_YEAR);
    
        int hour = RandomUtils.nextInt(DEFAULT_MIN_HOUR, DEFAULT_MAX_HOUR);
        int minute = RandomUtils.nextInt(DEFAULT_MIN_MINUTE, DEFAULT_MAX_MINUTE);
        int second = RandomUtils.nextInt(DEFAULT_MIN_SECOND, DEFAULT_MAX_SECOND);
    
        LocalDateTime localDateTime = FixDate(year, month, day).
                toLocalDateTime().
                withHourOfDay(hour).
                withMinuteOfHour(minute).
                withSecondOfMinute(second);
    
        value = localDateTime.toString(StringPattern());
        return this;
    }
    
//    @Override
//    protected String New(String value, MediaTypeFormatter formatter) throws Exception
//    {
//        DateTime dateTime = formatter.Deserialize(value, DateTime.class);
//        return formatter.Serialize(dateTime.toString(StringPattern()));
//    }
    
    @Override
    protected String StringPattern()
    {
        return "yyyy-MM-dd'T'HH:mm:ss";
    }

    @Override
    public Schema<String> randomizeWithCurrentValue(int iteration, int totalIterations) throws Exception{
        //search space not that big so just create a new random value
        return this.New();
    }


    @Override
    public Schema<String> mutation(int iteration, int totalIterations, int mutationProb) throws Exception{
        if(new Random().nextDouble() <= mutationProb)
            return this.New();  //search space not that big so just create a new random value
        return this;
    }

    @Override
    public void New(String value) throws Exception{
        this.value = value;
    }

    @Override
    public Schema<String> copySchema(SchemaBuilder builderInput/*,Schema<String> newSchema*/) throws Exception{
        //Schema<String> newSchema = new DateTimeSchema(builderInput);
        DateTimeSchema newSchema = new DateTimeSchema(builderInput);
        newSchema.value = this.value;
        return newSchema;
    }
}
