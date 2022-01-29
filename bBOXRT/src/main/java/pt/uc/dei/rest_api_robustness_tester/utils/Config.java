package pt.uc.dei.rest_api_robustness_tester.utils;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;

import java.util.Map;

public final class Config
{
    public static final int UNKNOWN_PORT = -1;
    
    private static Config instance = null;
    
    public static Config Instance() {return instance;}
    
    public static boolean IsInitialized()
    {
        return instance != null;
    }
    
    public Config(Map<String, String> args) throws Exception
    {
        String currentDir = System.getProperty("user.dir") + File.separator ;

        apiClassFile = args.get("--api-file");
        apiYamlFile = args.get("--api-yaml-file");
        workloadResultsFile = args.get("--wl-results");
        faultloadResultsFile = args.get("--fl-results");

        WL_REP = Integer.parseInt(args.getOrDefault("--wl-rep", "10"));
        WL_RETRY = Integer.parseInt(args.getOrDefault("--wl-retry", "1"));
        WL_MAX_TIME = Integer.parseInt(args.getOrDefault("--wl-max-time", "0"));
        FL_REP = Integer.parseInt(args.getOrDefault("--fl-rep", "3"));
        FL_MAX_TIME = Integer.parseInt(args.getOrDefault("--fl-max-time", "0"));

        out = args.get("--out");
        keepFailedWorkload = args.containsKey("--keep-failed-wl");
        proxyPort = Integer.parseInt(args.getOrDefault("--proxy-port", "" + UNKNOWN_PORT));
        connTimeout = Integer.parseInt(args.getOrDefault("--conn-timeout", "" + 60));
        
        if(apiClassFile == null)
            throw new Exception("Parameter --api-file is mandatory");
        if(apiYamlFile == null)
            throw new Exception("Parameter --api-yaml-file is mandatory");
        if(workloadResultsFile == null){
            //throw new Exception("Parameter --wl-results is mandatory");
            String pathToWLfile = currentDir + "workloadResultsFile.xlsx";
            workloadResultsFile = pathToWLfile;
            //this.createXlsxFiles(pathToWLfile);
        }
        if(faultloadResultsFile == null){
            //throw new Exception("Parameter --fl-results is mandatory");
            String pathToFLfile = currentDir + "faultloadResultsFile.xlsx";
            faultloadResultsFile = pathToFLfile;
            //this.createXlsxFiles(pathToFLfile);
        }

        
        if(!apiClassFile.endsWith(".java"))
            throw new Exception("Value for parameter --api-file must follow the pattern *.java");

        if(!apiYamlFile.endsWith(".yaml"))
            throw new Exception("Value for parameter --api-yaml-file must follow the pattern *.yaml");
    
        if(!workloadResultsFile.endsWith(".xlsx"))
            throw new Exception("Value for parameter --wl-results must follow the pattern *.xlsx");
        if(!faultloadResultsFile.endsWith(".xlsx"))
            throw new Exception("Value for parameter --fl-results must follow the pattern *.xlsx");
        
        instance = this;
    }


    public void createXlsxFiles(String pathToFile){
        File file = new File(pathToFile);
        if(!file.exists()){
            XSSFWorkbook workbook = new XSSFWorkbook();
            try (FileOutputStream outputStream = new FileOutputStream(pathToFile)) {
                workbook.write(outputStream);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public String getWorkloadResultsFile() {
        return workloadResultsFile;
    }

    public final String apiClassFile;
    public final String apiYamlFile;

    public String workloadResultsFile;
    public String faultloadResultsFile;
    
    public final int WL_REP;
    public int WL_RETRY;
    public final int WL_MAX_TIME;
    public final int FL_REP;
    public final int FL_MAX_TIME;
    public final String out;
    public final boolean keepFailedWorkload;
    public int proxyPort;
    public final int connTimeout;
}
