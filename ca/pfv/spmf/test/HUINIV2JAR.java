package ca.pfv.spmf.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.AlgoHUINIVMine;
import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.ItemsetsTP;
import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.UtilityTransactionDatabaseTP;

/**
 * Example of how to use the FHN algorithm 
 * from the source code.
 * @author Philippe Fournier-Viger, 2014
 */
public class HUINIV2JAR {
    static BufferedWriter writer = null; 
	
	public static void main(String [] arg) throws IOException{
		writer = new BufferedWriter(new FileWriter("resulthuiniv.csv"));
		int maxIteration= 10;
		String inputs_db[]={"accidents_negative.txt","chess_negative.txt","pumsb_negative.txt","mushroom_negative.txt"};
		String data_dir=System.getProperty("user.dir")+"/";
		try {
			 maxIteration = Integer.parseInt(arg[0]);
			 data_dir = arg[1]+"/";
		}
		catch (ArrayIndexOutOfBoundsException e){
			System.out.println("No params");
		}catch(Exception e){
			System.out.println("Invalid params");

		}
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
        int min_utility;
		long totalUtility;
        double ratioMin;

		//Write Headers columns
		writer.write("iteratio,db,total_utility,ratio_utilit,minutil,");
		writer.write("huiniv,time,memory,");
        writer.write("\n");

		for(int iteratio=0;iteratio<maxIteration;iteratio++){
            System.out.println("Iteratio = "+iteratio+"\n\n ---------------------- \n\n");

            for(String input_db:inputs_db){
                String input = data_dir+input_db;
                totalUtility= getTotalUtility(input);
                System.out.println("  \n\n input = " + input_db+"\nTotal Utility = " +totalUtility + "\n\n" );
                    ratioMin=0.5;
                    min_utility=(int) (ratioMin*totalUtility);
                    //For debugging
                    System.out.println("Iteration = "   +iteratio);
                    System.out.println("ratioMin = "   +ratioMin);
                    System.out.println("min_utility = "   +min_utility+"\n");
                    writer.write(Integer.toString(iteratio)+','+input_db+','+totalUtility+","+ratioMin+","+min_utility+",");
                while(runHUINIV(input, min_utility)  && ratioMin>0)
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

	}
    public static boolean runHUINIV(String input, int min_utility)  throws IOException{
        String output = ".//huinivoutput.txt";
        // Loading the database into memory
		UtilityTransactionDatabaseTP database = new UtilityTransactionDatabaseTP();
		database.loadFile(input);
		
		// Applying the Two-Phase algorithm
        try{
            AlgoHUINIVMine algo = new AlgoHUINIVMine();
            ItemsetsTP highUtilityItemsets = algo.runAlgorithm(database, min_utility);
            
            highUtilityItemsets.saveResultsToFile(output, database.getTransactions().size());
    
            algo.printStats();
			writer.write((highUtilityItemsets.fail? -1 : algo.getHUI() )+","+algo.getTime()+","+algo.getMemory()+",");

            return !highUtilityItemsets.fail;
        }catch(OutOfMemoryError  e){
            System.out.println("Memorry ERROR");
            return false;
        }
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


	
}
