package pt.uc.dei.rest_api_robustness_tester.specification;

import java.util.ArrayList;
import java.util.List;

//TODO: this class should be able to dynamically load .class files containing implementations of RestApiSetup
//      this way, new API specs can be added (together with Config and AuthHandlers) without changing any code
public class RestApiLoader
{
    public List<RestApi> Load()
    {
        List<RestApi> restApis = new ArrayList<>();
    
//        //1st tier
//        restApis.add(new Giphy().Load());
//        FIXME: Loading the Github API is causing a StackOverflow
//        restApis.add(new GitHub().Load());
//        restApis.add(new Slack().Load());
//        restApis.add(new Shutterstock().Load());
        
//        //2nd tier
//        restApis.add(new Api2Pdf().Load());
//        restApis.add(new BikeWiseAPIv2().Load());
//        restApis.add(new Brex().Load());
//        restApis.add(new Browshot().Load());
//        restApis.add(new BulkSMSJSON().Load());
//        restApis.add(new ElmahIo().Load());
//        restApis.add(new GreenwirePublic().Load());
//        restApis.add(new HHSMediaServices().Load());
//        restApis.add(new HighwaysEnglandAPI().Load());
//        restApis.add(new LaunchDarkly().Load());
//        restApis.add(new Miataru().Load());
//        restApis.add(new MoonByAiWeiweiOlafurEliasson().Load());
//        restApis.add(new OpenSkills().Load());
//        restApis.add(new RatGenomeDatabase().Load());
//        restApis.add(new USEPAECHO_AllData().Load());
//        restApis.add(new USEPAECHO_DFR().Load());
//        restApis.add(new USEPAECHO_ECR().Load());
//        restApis.add(new USEPAECHO_RCRA().Load());
//        restApis.add(new USEPAECHO_SDWA().Load());
//        FIXME: Loading API2Cart is causing a StackOverflow
//        restApis.add(new API2Cart().Load());
//        restApis.add(new AppVeyor().Load());

//        //container
//        FIXME: Some NPEs are thrown @line 193 in FaultloadExecutor.FindFaultloadRequest
//        restApis.add(new DockerEngineAPI().Load());

//        //tpc
//        restApis.add(new TPCRestAPI().Load());
        
        return restApis;
    }
}
