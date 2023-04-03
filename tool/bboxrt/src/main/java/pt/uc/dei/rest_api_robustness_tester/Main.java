package pt.uc.dei.rest_api_robustness_tester;


public class Main {

    public static void main(String[] args){
        try
        {
            RestApiRobustnessTester tool = new RestApiRobustnessTester();
            tool.Init(args);
            tool.Run();
            //tool.generateAndRunWorkload();
        }
        catch(Exception e )
        {
            System.err.println("Error: " + e);
            e.printStackTrace();
        }
    }
}
