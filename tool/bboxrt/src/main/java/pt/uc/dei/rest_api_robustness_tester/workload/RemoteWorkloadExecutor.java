package pt.uc.dei.rest_api_robustness_tester.workload;

import pt.uc.dei.rest_api_robustness_tester.ProxyServer;
import pt.uc.dei.rest_api_robustness_tester.specification.RestApi;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class RemoteWorkloadExecutor extends WorkloadExecutor
{
    public RemoteWorkloadExecutor(RestApi restAPI, WorkloadExecutorConfig config)
    {
        super(null, restAPI, config);
    }
    
    @Override
    public void Execute()
    {
        Scanner scan = new Scanner(System.in);
        
        ProxyServer proxyServer = new ProxyServer(config.GetProxyHost().getPort(),
                config.GetFaultloadExecutorHook(), config.GetWriter());
        Thread proxyThread = new Thread(() ->
        {
            try
            {
                proxyServer.Start();
            }
            catch(Exception e)
            {
                System.err.println("Error: " + e);
            }
        });
    
        System.out.println("(Or enter \"stop\" to terminate the proxy server and dump the results)");
        
        proxyThread.start();
    
        Timer timer = new Timer();
        if(config.GetStoppingCondition() == WorkloadExecutorConfig.StoppingCondition.TimeBased)
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    try
                    {
                        proxyServer.Stop();
                        proxyThread.interrupt();
                        System.out.write("stop".getBytes());
                    }
                    catch(Exception e)
                    {
                        System.err.println("Error: " + e);
                    }
                }
            }, config.GetDurationMillis());
        else
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {

                    //System.out.println("left:" + config.GetFaultloadExecutorHook().FaultsLeft());
                    try
                    {
                        if(config.GetFaultloadExecutorHook().FaultsLeft() == 0)
                        {
                            proxyServer.Stop();
                            proxyThread.interrupt();
                            System.out.write("stop".getBytes());
                        }
                    }
                    catch(Exception e)
                    {
                        System.err.println("Error: " + e);
                    }
                }
            }, 0, 5000);
        
        String line = "";
        try
        {
            while (!line.equalsIgnoreCase("stop"))
                line = scan.nextLine();

        }
        catch(Exception ignored) {}
        
        try
        {
            proxyServer.Stop();
            proxyThread.interrupt();
            proxyThread.join();
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e);
        }


        timer.cancel();


        // proxyServer.writer.Write(restAPI.Name());
    }
}
