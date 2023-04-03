package pt.uc.dei.rest_api_robustness_tester.AI;
import java.io.*;

public class UtilsFile {

    public String fileName;
    public String filePath;

    public UtilsFile(){}
    public UtilsFile(String fileName, String filePath){
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public void writeFitnessValues(IndividualForIS[][] matrix){
        try {
            System.out.println("Writing fitness values to file...");
            File f = new File(filePath);
            FileWriter myWriter;

            if(f.getAbsolutePath().endsWith(".txt"))
                myWriter = new FileWriter(f.getAbsolutePath());
            else if(f.getAbsolutePath().endsWith("/"))
                myWriter = new FileWriter(f.getAbsolutePath() + fileName);
            else
                myWriter = new FileWriter(f.getAbsolutePath() + "/" +fileName);

            //matrix.length = number of rows in matrix
            for(int i = 0 ; i < matrix.length ; i++){
                for(int j = 0; j < matrix[i].length ; j++){
                    if(j < matrix[i].length - 1)
                        myWriter.write(String.valueOf(matrix[i][j].getFitnessValue()) + "\t");
                    else
                        myWriter.write(String.valueOf(matrix[i][j].getFitnessValue()) + "\n");
                }
            }

            for(int i = 0 ; i < matrix.length ; i++){
                for(int j = 0; j < matrix[i].length ; j++){
                    if(j < matrix[i].length - 1)
                        myWriter.write(String.valueOf(matrix[i][j].getFitnessStatusCode()) + "\t");
                    else
                        myWriter.write(String.valueOf(matrix[i][j].getFitnessStatusCode()) + "\n");
                }
            }

            for(int i = 0 ; i < matrix.length ; i++){
                for(int j = 0; j < matrix[i].length ; j++){
                    if(j < matrix[i].length - 1)
                        myWriter.write(String.valueOf(matrix[i][j].getFitnessDistance()) + "\t");
                    else
                        myWriter.write(String.valueOf(matrix[i][j].getFitnessDistance()) + "\n");
                }
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");

        } catch (IOException e) {
            System.out.println("An error occurred while trying to write in file.");
            e.printStackTrace();
        }
    }

}
