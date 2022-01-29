package pt.uc.dei.rest_api_robustness_tester.schema;

import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;

import java.util.Random;

public class DateSchema extends StringSchema
{
    //TODO: Min and Max year should be GLobal constants
    protected static final int DEFAULT_MIN_YEAR = 0;
    protected static final int DEFAULT_MAX_YEAR = 9999;
    protected static final int DEFAULT_MIN_MONTH = 1;
    protected static final int DEFAULT_MAX_MONTH = 12;
    protected static final int DEFAULT_MIN_DAY = 1;
    protected static final int DEFAULT_MAX_DAY = 31;
    
    DateSchema(SchemaBuilder builder)
    {
        super(builder);
    }
    
    @Override
    public Schema<String> New() throws Exception
    {
        int day = RandomUtils.nextInt(DEFAULT_MIN_DAY, DEFAULT_MAX_DAY);
        int month = RandomUtils.nextInt(DEFAULT_MIN_MONTH, DEFAULT_MAX_MONTH);
        int year = RandomUtils.nextInt(DEFAULT_MIN_YEAR, DEFAULT_MAX_YEAR);
    
        value = FixDate(year, month, day).toString(StringPattern());
        return this;
    }
    
//    @Override
//    protected String New(String value, MediaTypeFormatter formatter) throws Exception
//    {
//        DateTime dateTime = formatter.Deserialize(value, DateTime.class);
//        return formatter.Serialize(dateTime.toString(StringPattern()));
//    }
    
    protected DateTime FixDate(int year, int month, int day)
    {
        DateTime dateTime = new DateTime();
        
        if(day == DEFAULT_MAX_DAY)
            if(month == 4 || month == 6 || month == 9 || month == 11)
                day = DEFAULT_MAX_DAY - 1;
        
        if(month == 2)
        {
            if(dateTime.withYear(year).year().isLeap())
                day = 29;
            else
                day = 28;
        }
        
        return dateTime.withYear(year).
                withMonthOfYear(month).
                withDayOfMonth(day).
                withHourOfDay(0).
                withMinuteOfHour(0);
    }
    
    protected String StringPattern()
    {
        return "yyyy-MM-dd";
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
    public Schema<String> copySchema(SchemaBuilder builderInput /*, Schema<String> newSchema*/) throws Exception{
        //Schema<String> newSchema = new DateSchema(builderInput);
        DateSchema newSchema = new DateSchema(builderInput);
        newSchema.value = this.value;
        return newSchema;
    }
}
