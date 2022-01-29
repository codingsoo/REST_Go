package pt.uc.dei.rest_api_robustness_tester.utils;

public enum TimeUnit
{
    Second(1),
    Minute(60),
    Hour(60*60),
    Day(60*60*24);
    
    private final int multiplierToSeconds;
    
    TimeUnit(int multiplierToSeconds)
    {
        this.multiplierToSeconds = multiplierToSeconds;
    }
    
    public int ToSeconds(int timeValue)
    {
        return timeValue * this.multiplierToSeconds;
    }
}
