package pt.uc.dei.rest_api_robustness_tester.workload;

import pt.uc.dei.rest_api_robustness_tester.utils.CountdownTimer;
import pt.uc.dei.rest_api_robustness_tester.utils.ElapsedTimeCounter;
import pt.uc.dei.rest_api_robustness_tester.utils.TimeUnit;

public class RateLimiter
{
    private final int requestLimit;
    private final int timeValue;
    private final TimeUnit timeUnit;
    
    private int ticks = 0;
    private final CountdownTimer timer;
    private final ElapsedTimeCounter elapsedTime;
    
    public RateLimiter(int requestLimit, int timeValue, TimeUnit timeUnit)
    {
        this.requestLimit = requestLimit;
        this.timeValue = timeValue;
        this.timeUnit = timeUnit;
        this.timer = new CountdownTimer();
        this.elapsedTime = new ElapsedTimeCounter();
    }
    
    public void Tick()
    {
        ticks++;
        if(ticks == 1)
            elapsedTime.Start();
        
    }
    
    //FIXME: should NOT be done on the main thread!!!
    public void WaitAndResetIfNecessary()
    {
        int remainingSeconds = timeUnit.ToSeconds(timeValue) - elapsedTime.ElapsedSeconds();
        
        if(ticks >= requestLimit && remainingSeconds > 0)
        {
            System.out.println("[RateLimiter] Request rate limit reached - waiting " + remainingSeconds + " seconds");
            timer.Wait(remainingSeconds * 1000);
            ticks = 0;
        }
        else if(ticks < requestLimit && remainingSeconds <= 0)
        {
            ticks = 0;
        }
    }
}
