package ca.pfv.spmf.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFHN;
import ca.pfv.spmf.algorithms.frequentpatterns.mHUIMineNegV1.AlgoMHUIMinerNegV1;
import ca.pfv.spmf.algorithms.frequentpatterns.mHUIMineNegV2.AlgoMHUIMinerNegV2;
//HUINIV
import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.AlgoHUINIVMine;
import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.ItemsetsTP;
import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.UtilityTransactionDatabaseTP;

import ca.pfv.spmf.algorithms.frequentpatterns.mHUIMiner.AlgoMHUIMiner;
import ca.pfv.spmf.algorithms.frequentpatterns.minmhuiminer.mHUIMiner;

/**
 * Example of tet negative frequent pattern
 * @author Joaquin CAballero, 2022
 */
public class Atest {
    /** Object for writing the output to a file */
    static BufferedWriter writer = null; 
    public static void main(String [] arg) throws IOException{
		writer = new BufferedWriter(new FileWriter("C:\\Users\\Euli\\Documents\\uca\\tesis\\SMPF\\test_result\\result.csv"));
		//String input = fileToPath("DB_retail_negative.txt");
        String inputs_db[]={"DB_retail_negative.txt","DB_kosarak_negative.txt"};
        int min_utility;
		long totalUtility;
        
    
        //Write Headers
        writer.write("db,total_utility,ratio_utilit,minutil,fhn,t,m,mHuiminerNegV1,t,m,mHuiminerNegV2,t,m\n");
        
        for(String input_db:inputs_db){

            String input = fileToPath(input_db);
            totalUtility= getTotalUtility(input);
            System.out.println(" input = " + input+"Total Utility = " +totalUtility );

            for(double ratioMin=0.001;ratioMin<0.2;ratioMin+=0.001){

                min_utility=(int) (ratioMin*totalUtility);
                System.out.println("ratioMin = "   +ratioMin);
                writer.write(input+','+totalUtility+","+ratioMin+","+min_utility+",");
    
                //Algo test
                runFHN(input, min_utility);
                runMHUIminerNegV1(input, min_utility);
                runMHUIminerNegV2(input, min_utility);
    
                //New line for new test results
                writer.newLine();
    
            }
           

        }
         writer.close();
        //runHUINIV(input, output, min_utility);
        // runMHUIminer(input, output, min_utility);
        // runMinMHUIminer(input);
	}
    public static void runMHUIminerNegV1(String input, int min_utility)  throws IOException{
        // Applying the HUIMiner algorithm
		AlgoMHUIMinerNegV1 algo = new AlgoMHUIMinerNegV1();
		algo.runAlgorithm(input,".//test_result//mHUIminerNegV1output.txt" , min_utility);
		algo.printStats();
        writer.write(algo.getHUI()+","+algo.getTime()+","+algo.getMemory()+",");
    }

    public static void runMHUIminerNegV2(String input, int min_utility)  throws IOException{
        // Applying the HUIMiner algorithm
		AlgoMHUIMinerNegV2 algo = new AlgoMHUIMinerNegV2();
		algo.runAlgorithm(input,".//test_result//mHUIminerNegV2output.txt" , min_utility);
		algo.printStats();
        writer.write(algo.getHUI()+","+algo.getTime()+","+algo.getMemory()+",");
    }

	public static void runFHN(String input, int min_utility)  throws IOException{
        // Applying the FHN algorithm
        String output = ".//test_result//fhnoutput.txt";
        AlgoFHN algo = new AlgoFHN();
        algo.runAlgorithm(input, output, min_utility);
        algo.printStats();
        writer.write(algo.getHUI()+","+algo.getTime()+","+algo.getMemory()+",");
    }
    public static void runHUINIV(String input,String output, int min_utility)  throws IOException{
        		// Loading the database into memory
		UtilityTransactionDatabaseTP database = new UtilityTransactionDatabaseTP();
		database.loadFile(input);
		
		// Applying the Two-Phase algorithm
		AlgoHUINIVMine algo = new AlgoHUINIVMine();
		ItemsetsTP highUtilityItemsets = algo.runAlgorithm(database, min_utility);
		
		highUtilityItemsets.saveResultsToFile(output, database.getTransactions().size());

		algo.printStats();
    }

    public static long getTotalUtility(String input) throws IOException {
        // We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
        long totalUtility = 0;
        try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// split the transaction according to the : separator
				String split[] = thisLine.split(":");
				// the second part is the transaction utility
				int transactionUtility = Integer.parseInt(split[1]);  
                totalUtility += transactionUtility;
				
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
        return totalUtility;

    }

    public static void runMHUIminer(String input,String output, int min_utility)  throws IOException{
        // Applying the HUIMiner algorithm
		AlgoMHUIMiner huiminer = new AlgoMHUIMiner();
		huiminer.runAlgorithm(input, output, min_utility);
		huiminer.printStats();
    }
    public static void runMinMHUIminer(String input) throws IOException{
        String outputMin = ".//test_result//outputMin.txt";
        String allItemsets = ".//test_result//allMinMHUIMitemsets.txt";

        Double ratioUserInput = 0.16;
//        Double ratioUserInput = 0.05;

        // Applying the HUIMiner algorithm
        mHUIMiner huiminer = new mHUIMiner();
        huiminer.runAlgorithm(input, allItemsets, ratioUserInput, outputMin);
        huiminer.printStats();
    }
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFHN_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
