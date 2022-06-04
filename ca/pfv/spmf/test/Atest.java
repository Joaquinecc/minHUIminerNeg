package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFHN;
import ca.pfv.spmf.algorithms.frequentpatterns.mHUIMineNegV1.AlgoMHUIMinerNegV1;
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
    public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("DB_retail_negative.txt");
		int min_utility = 100000;  
		
        runFHN(input, min_utility);
        runMHUIminerNegV1(input, min_utility);

        //runHUINIV(input, output, min_utility);
        // runMHUIminer(input, output, min_utility);
        // runMinMHUIminer(input);
	}
    public static void runMHUIminerNegV1(String input, int min_utility)  throws IOException{
        // Applying the HUIMiner algorithm
		AlgoMHUIMinerNegV1 huiminer = new AlgoMHUIMinerNegV1();
		huiminer.runAlgorithm(input,".//test_result//mHUIminerNegV1output.txt" , min_utility);
		huiminer.printStats();
    }

	public static void runFHN(String input, int min_utility)  throws IOException{
        // Applying the FHN algorithm
        String output = ".//test_result//fhnoutput.txt";
        AlgoFHN algo = new AlgoFHN();
        algo.runAlgorithm(input, output, min_utility);
        algo.printStats();
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
