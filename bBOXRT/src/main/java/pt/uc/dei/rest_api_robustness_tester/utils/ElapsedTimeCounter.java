package pt.uc.dei.rest_api_robustness_tester.utils;

public class ElapsedTimeCounter
{
    private final static long NOT_STARTED = -1;
    
    private long startMillis = NOT_STARTED;
    
    public boolean HasStarted()
    {
        return startMillis != NOT_STARTED;
    }
    
    public void Start()
    {
        startMillis = System.currentTimeMillis();
    }
    
    public int ElapsedSeconds()
    {
        return (int)(System.currentTimeMillis() - startMillis)/1000;
    }
}
