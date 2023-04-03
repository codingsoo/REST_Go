import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;
import java.util.ArrayList; 

public class ReadLines{

	public static void main(String[] args) throws IOException {

		FileWriter myWriter;
		String filePath = "/Users/thedemean/Documents/blackbox_git_dei_inv/bBOXRT/data_for_graphs/case5/";
		String fileName = "output.txt";

		ArrayList<Integer> somaContIF = new ArrayList<>();
		ArrayList<Integer> somaContBS = new ArrayList<>();

		int controlo = 0;
		for(int i = 0; i < 2; i++){

			if(i == 0)
				myWriter = new FileWriter(filePath + "linesFilteresIF.txt");
			else
				myWriter = new FileWriter(filePath + "linesFilteresBS.txt");
			try (BufferedReader br = new BufferedReader(new FileReader(filePath + fileName))) {
	    		String line;
			    while ((line = br.readLine()) != null) {
			       // process the line.
			    	if(controlo == 0 && line.toLowerCase().contains("informative searching pass counter:")){
			    		System.out.println(line);
			    		controlo = 1;
			    	}
			    	if(i == 0 && line.toLowerCase().contains("informative searching pass counter:")){
			    		String[] stringArrayFiltered = line.split(":",2);
			    		int filteredNumber = Integer.parseInt(stringArrayFiltered[1].trim());
			    		somaContIF.add(filteredNumber);
			    		myWriter.write(String.valueOf(filteredNumber) + "\n");
			    	}
			    	if(i ==  1 && line.toLowerCase().contains("random searching(base line) pass counter:")){
			    		String[] stringArrayFiltered = line.split(":",2);
			    		int filteredNumber = Integer.parseInt(stringArrayFiltered[1].trim());
			    		somaContBS.add(filteredNumber);
			    		myWriter.write(String.valueOf(filteredNumber) + "\n");
			    	}
			    }
			    int sumNumbers = 0;

			    if(i == 0){
			    	for(Integer number : somaContIF){
			    		sumNumbers += number;
			    	}
			    }
			    else{
			    	for(Integer number : somaContBS){
			    		sumNumbers += number;
			    	}
			    }
			    myWriter.write(String.valueOf(sumNumbers) + "\n");
			    myWriter.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

		
	}


}