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
import ca.pfv.spmf.algorithms.frequentpatterns.minmhuiminerNeg.MinmHUIMinerNeg;
import ca.pfv.spmf.algorithms.frequentpatterns.minmhuiminerNegV1.MinmHUIMinerNegV1;

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
        String inputs_db[]={"accidents_negative.txt","chess_negative.txt","pumsb_negative.txt","mushroom_negative.txt"};
        int min_utility;
		long totalUtility;
        double ratioMin;

        //Write Headers columns
        writer.write("iteratio,db,total_utility,ratio_utilit,minutil,");
        writer.write("fhn,time,memory,");
        //writer.write("HUINIV,time,memory,");
       // writer.write("Min-mHUIminer-NEG,time,memory,");
        writer.write("\n");
        int maxIteration=10;
        for(int iteratio=0;iteratio<maxIteration;iteratio++){
            System.out.println("Iteratio = "+iteratio+"\n\n ---------------------- \n\n");

            for(String input_db:inputs_db){
                String input = fileToPath(input_db);
                totalUtility= getTotalUtility(input);
                System.out.println("  \n\n input = " + input_db+"\nTotal Utility = " +totalUtility + "\n\n" );
                    ratioMin=0.5;
                    min_utility=(int) (ratioMin*totalUtility);
                    //For debugging
                    System.out.println("Iteration = "   +iteratio);
                    System.out.println("ratioMin = "   +ratioMin);
                    System.out.println("min_utility = "   +min_utility+"\n");
                    writer.write(Integer.toString(iteratio)+','+input_db+','+totalUtility+","+ratioMin+","+min_utility+",");
                while(runFHN(input, min_utility) != -1 && ratioMin>0)
                {
                    writer.newLine();
                    writer.flush();//Save
                    ratioMin-=0.05;
                    min_utility=(int) (ratioMin*totalUtility);
                    //For debugging
                    System.out.println("Iteration = "   +iteratio);
                    System.out.println("ratioMin = "   +ratioMin);
                    System.out.println("min_utility = "   +min_utility+"\n");
                    writer.write(Integer.toString(iteratio)+','+input_db+','+totalUtility+","+ratioMin+","+min_utility+",");
                    writer.newLine();
                    writer.flush();//Save
                }
                    
               
    
            }
            
        }
    
        writer.close();
         //Algo test
                    //runTheFinalProduct(input, min_utility);
                     //15 minutos
                    //if(runFHN(input, min_utility) == -1) break;
                               
                    //runHUINIV(input, min_utility);
                    //runMHUIminerNeg(input, min_utility);
                    
                    //Positive only
                    //runMinMHUIminer(input,ratioMin);
                    //runMHUIminer(input, min_utility);
                    
                    //New line for new test results
    
	}
    public static void runTheFinalProduct(String input, int min_utility)  throws IOException{
        // Applying the HUIMiner algorithm
		MinmHUIMinerNeg algo = new MinmHUIMinerNeg();
		algo.runAlgorithm(input, min_utility,".//test_result//runTheFinalProduct.txt" );
		algo.printStats();
        writer.write(algo.getHUI()+","+algo.getTime()+","+algo.getMemory()+",");
    }

    public static void runMinmHUIminerNegV1(String input, int min_utility)  throws IOException{
        // Applying the HUIMiner algorithm
		MinmHUIMinerNegV1 algo = new MinmHUIMinerNegV1();
		algo.runAlgorithm(input,".//test_result//mierdas.txt", min_utility,".//test_result//minmHUIminerNegV1output.txt" );
		algo.printStats();
        writer.write(algo.getHUI()+","+algo.getTime()+","+algo.getMemory()+",");
    }

    public static void runMHUIminerNeg(String input, int min_utility)  throws IOException{
        // Applying the HUIMiner algorithm
		AlgoMHUIMinerNegV2 algo = new AlgoMHUIMinerNegV2();
		algo.runAlgorithm(input,".//test_result//mHUIminerNegV2output.txt" , min_utility);
		algo.printStats();
        writer.write(algo.getHUI()+","+algo.getTime()+","+algo.getMemory()+",");
    }

    public static void runMHUIminerNegV1(String input, int min_utility)  throws IOException{
        // Applying the HUIMiner algorithm
		AlgoMHUIMinerNegV1 algo = new AlgoMHUIMinerNegV1();
		algo.runAlgorithm(input,".//test_result//mHUIminerNegV1output.txt" , min_utility);
		algo.printStats();
        writer.write(algo.getHUI()+","+algo.getTime()+","+algo.getMemory()+",");
    }

	public static int runFHN (String input, int min_utility)  throws IOException{
        // Applying the FHN algorithm
        String output = ".//test_result//fhnoutput.txt";
        AlgoFHN algo = new AlgoFHN();
        int temp= algo.runAlgorithm(input, output, min_utility);
        algo.printStats();
        writer.write((temp == -1? temp : algo.getHUI() )+","+algo.getTime()+","+algo.getMemory()+",");
        return temp;
    }
    public static void runHUINIV(String input, int min_utility)  throws IOException{
        String output = ".//test_result//huinivoutput.txt";
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

    public static void runMHUIminer(String input, int min_utility)  throws IOException{
        String output = ".//test_result//mHUIminer-output.txt";

        // Applying the HUIMiner algorithm
		AlgoMHUIMiner huiminer = new AlgoMHUIMiner();
		huiminer.runAlgorithm(input, output, min_utility);
		huiminer.printStats();
    }
    public static void runMinMHUIminer(String input,Double ratioUserInput ) throws IOException{
        String outputMin = ".//test_result//min-HUIminer-outputMin.txt";
        String allItemsets = ".//test_result//allMinMHUIMitemsets.txt";
        // Applying the minHUIMiner algorithm
        mHUIMiner huiminer = new mHUIMiner();
        huiminer.runAlgorithm(input, allItemsets, ratioUserInput, outputMin);
        huiminer.printStats();
    }
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFHN_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
