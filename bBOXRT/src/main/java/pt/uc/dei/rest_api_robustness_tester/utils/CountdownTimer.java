package pt.uc.dei.rest_api_robustness_tester.utils;

public class CountdownTimer
{
    private final Object lock = new Object();
    
    public boolean Wait(long waitMillis)
    {
        try
        {
            synchronized (lock)
            {
                long initMillis = System.currentTimeMillis();
                while (System.currentTimeMillis() < initMillis + waitMillis)
                    lock.wait(waitMillis);
            }
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
}
