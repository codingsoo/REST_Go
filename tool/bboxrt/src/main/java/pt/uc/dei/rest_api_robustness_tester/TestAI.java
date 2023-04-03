package pt.uc.dei.rest_api_robustness_tester;


public class TestAI {

    public static void main(String[] args){
        try
        {
            RestApiRobustnessTester tool = new RestApiRobustnessTester();
            tool.controlerAI = true;
            tool.Init(args);
            //tool.runAI();

            for(int i = 0; i < 30 ; i++){
                tool.runIS(i);
            }
        }
        catch(Exception e )
        {
            System.err.println("Error: " + e);
            e.printStackTrace();
        }
    }
}
