package ca.pfv.spmf.algorithms.frequentpatterns.minmhuiminerNegV1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This is an implementation of the mHUIMiner algorithm. The algorithm takes transaction
 * data in SPMF format and a user specified minUtility, and outputs all the high utility itemsets.
 * <p>
 * The algorithm is a combination of the IHUP algorithm and the HUI-Miner algorithm
 * The details of these two algorithms are described in the following two papers:
 * <p>
 * Chowdhury Farhan Ahmed, Syed Khairuzzaman Tanbeer, Byeong-Soo Jeong,
 * Young-Koo Lee: Efficient Tree Structures for High Utility Pattern Mining
 * in Incremental Databases. IEEE Trans. Knowl. Data Eng. 21(12): 1708-1721 (2009)
 * <p>
 * Liu, M., Qu, J. (2012). Mining High Utility Itemsets without Candidate Generation.
 * Proc. of CIKM 2012. pp.55-64.<\br><\br>
 *
 * @author Yuxuan(Alex) Peng
 * @see IHUPTreeMod
 * @see UtilityTuple
 * @see UtilityList
 * @see Node
 * @see Item
 */

public class MinmHUIMinerNegV1 {
    // variable for statistics
    private double maxMemory = 0; // the maximum memory usage
    private long startTimestamp = 0; // the time the algorithm started
    private long endTimestamp = 0; // the time the algorithm terminated
    private int huiCount = 0; // the number of HUIs generated
    private long totalUtility = 0; // sum of all transaction utilities
    private int minUtility = 0; // threshold
    private int joinCount = 0; // number of times the construct method is called
    //NEW
    /** Store single items with negative utility */
	Set<Integer> negativeItems = null;

    // Map TWU to each item
    private Map<Integer, Integer> mapItemToTWU;
    // Map utilityList to each item
    private Map<Integer, UtilityList> mapItemToUtilityList;

    /**
     * The structure called the "itemset store" in the paper
     */
    List<List<Itemset>> listItemsetsBySize = null;

    private BufferedWriter allWriter = null;
    private BufferedWriter writerMin = null;

    // To activate debug mode
    private final boolean DEBUG = false;

    /**
     * Method to run the algorithm
     *
     * @param input       path to an input file
     * @param allItemsets path for writing the output file
     * @param minimumUtility       the minimum utility threshold as a ratio
     * @throws IOException exception if error while reading or writing the file
     */
    public void runAlgorithm(String input, String allItemsets, int minimumUtility, String outputMin) throws IOException {

        maxMemory = 0;

        startTimestamp = System.currentTimeMillis();

        allWriter = new BufferedWriter(new FileWriter(allItemsets));
        writerMin = new BufferedWriter(new FileWriter(outputMin));

        //NEW
        negativeItems = new HashSet<Integer>();

        // create a map to store the TWU of each item
        mapItemToTWU = new HashMap<Integer, Integer>();

        listItemsetsBySize = new ArrayList<List<Itemset>>();

        // ******************************************
        // first database scan to calculate the TWU of each item.
        BufferedReader myInput = null;
        String thisLine;
        try {
            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
            // for each line (transaction) until the end of file
            while ((thisLine = myInput.readLine()) != null) {
                // if the line is a comment, is empty or is a kind of metadata
                if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
                        || thisLine.charAt(0) == '@') {
                    continue;
                }

                // split the transaction according to the : separator
                String split[] = thisLine.split(":");
                // the first part is the list of items
                String items[] = split[0].split(" ");
                // the second part is the transaction utility
                int transactionUtility = Integer.parseInt(split[1]);
                totalUtility += transactionUtility;

                // for each item, we add the transaction utility to its TWU
                for (int i = 0; i < items.length; i++) {
                    Integer item = Integer.parseInt(items[i]);
                    Integer twu = mapItemToTWU.get(item);
                    twu = (twu == null) ? transactionUtility : twu + transactionUtility;
                    mapItemToTWU.put(item, twu);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (myInput != null) {
                myInput.close();
            }
        }
        checkMemory();

        // ******************************************
        // second database scan generates revised transaction and global IHUP-Tree
        // start mining once the IHUP-Tree is built
        try {

            // calculate minUtility threshold
            //Double temp = totalUtility * ratio;
            minUtility = minimumUtility;
            System.out.println("minUtility: " + minUtility);

            IHUPTreeMod tree = new IHUPTreeMod();

            // create the global hash table to store utilityList
            mapItemToUtilityList = new HashMap<Integer, UtilityList>();
            for (Integer itemID : mapItemToTWU.keySet()) {
                if (mapItemToTWU.get(itemID) >= minUtility) {
                    UtilityList uList = new UtilityList(itemID);
                    mapItemToUtilityList.put(itemID, uList);
                }
            }

            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));

            // transaction ID to track transactions
            int tid = 0;

            // for each line (transaction) until the end of file
            while ((thisLine = myInput.readLine()) != null) {
                // if the line is a comment, is empty or is a kind of metadata
                if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
                        || thisLine.charAt(0) == '@') {
                    continue;
                }

                // split the line according to the separator
                String split[] = thisLine.split(":");
                // get the list of items
                String items[] = split[0].split(" ");
                // get the list of utility values corresponding to each item
                // for that transaction
                String utilityValues[] = split[2].split(" ");

                // create revised transaction
                List<Item> revisedTransaction = new ArrayList<Item>();
                // for each item in the original transaction
                for (int i = 0; i < items.length; i++) {
                    // convert values to integers
                    int item = Integer.parseInt(items[i]);
                    int utility = Integer.parseInt(utilityValues[i]);
                    Item element = new Item(item, utility);

                    // we remove unpromising items from the tree
                    if (mapItemToTWU.get(item) >= minUtility) {
                        revisedTransaction.add(element);
                        //NEW
                        if(utility> 0){
                            negativeItems.add(item);
                        }
                    }
                }

                // increment transaction ID
                tid++;

                // revised transaction in ascending order of TWU
                Collections.sort(revisedTransaction, new Comparator<Item>() {
                    public int compare(Item o1, Item o2) {
                        return compareItemsAsc(o1.getItemID(), o2.getItemID(), mapItemToTWU);
                    }
                });

                // populate the hash table for utilityLists
                // the last item in the transaction has 0 remainingUtility
                int remainingUtility = 0;
                for (int i = revisedTransaction.size() - 1; i >= 0; i--) {
                    Item item = revisedTransaction.get(i);
                    UtilityTuple uTuple = new UtilityTuple(tid, item.getUtility(), remainingUtility);
                    mapItemToUtilityList.get(item.getItemID()).addTuple(uTuple);
                    remainingUtility += item.getUtility()>0?item.getUtility():0;
                }

                // add transaction to the global IHUP-Tree
                tree.addTransaction(revisedTransaction, tid);
            } // end while, finished building tree and utilityLists

            // we create the header table for the global IHUP-Tree
            tree.createHeaderList(mapItemToTWU,negativeItems);
            checkMemory();

            for (int i = tree.headerList.size() - 1; i >= 0; i--) {
                Integer itemID = tree.headerList.get(i);

                // initial itemset contains only single item
                int[] itemset = ArraysAlgos.appendIntegerToArray(new int[0], itemID);
                UtilityList ulist = mapItemToUtilityList.get(itemID);

                // if sumIutils >= minUtility, the itemset is a minHUI
                if (ulist.sumIutils >= minUtility) {
                    registerItemsetAndRemoveLarger(itemset, ulist.sumIutils, ulist.uLists.size());
//                    if (DEBUG) {
//                        System.out.println("First for " + Arrays.toString(itemset));
//                    }
                }
            }

            // start mining over the tree
            // for each item from the bottom of the header table list of the tree
            for (int i = tree.headerList.size() - 1; i >= 0; i--) {
                Integer itemID = tree.headerList.get(i);

                // initial itemset contains only single item
                int[] itemset = ArraysAlgos.appendIntegerToArray(new int[0], itemID);
                UtilityList ulist = mapItemToUtilityList.get(itemID);

                allWriteOut(itemset, ulist.sumIutils);

                // if sumIutils >= minUtility, the itemset is a minHUI
                if (ulist.sumIutils < minUtility) {

                    // we expand current itemset (build local tree)
                    if ((ulist.sumIutils + ulist.sumRutils) >= minUtility) {
//                        if (DEBUG) {
//                            System.out.println("ELSE cond" + Arrays.toString(itemset));
//                        }
                        // ===== CREATE THE LOCAL TREE =====
                        IHUPTreeMod localTree = createLocalTree(tree, itemID);
                        checkMemory();

                        // call the mining procedure to explore
                        // itemsets that are extensions of the current itemset
                        if (localTree.headerList.size() > 0) {
                            miner(localTree, minUtility, itemset, ulist.uLists);
                            checkMemory();
                        }
                    }
                }
            } // end for
            // save all min itemsets
            for (List<Itemset> listItemsets : listItemsetsBySize) {
                for (Itemset itemset : listItemsets) {
                    writeOutMin(itemset);
                }
            }
        } catch (Exception e) {
            // catches exception if error while reading the input file
            e.printStackTrace();
        } finally {
            if (myInput != null) {
                myInput.close();
            }
        }
        checkMemory();

        // record end time
        endTimestamp = System.currentTimeMillis();
        allWriter.close();
        writerMin.close();
    }

    private int compareItemsAsc(int item1, int item2, Map<Integer, Integer> mapItemEstimatedUtility) {
        //new
        Boolean item1IsNegative = negativeItems.contains(item1);
		Boolean item2IsNegative = negativeItems.contains(item2);
		if(!item1IsNegative && item2IsNegative) {
			return -1;
		}else if (item1IsNegative && !item2IsNegative)  {
			return 1;
		}
        int compare = mapItemEstimatedUtility.get(item1) - mapItemEstimatedUtility.get(item2);
        // if the same, use the lexical order otherwise use the TWU
        return (compare == 0) ? item1 - item2 : compare;
    }


    /**
     * Mine UP Tree recursively
     *
     * @param tree       IHUPTree to mine
     * @param minUtility minimum utility threshold
     * @param itemset    the prefix itemset
     * @param pTuples    a list of UtilityTuples of the current itemset
     */
    private void miner(IHUPTreeMod tree, int minUtility, int[] itemset, List<UtilityTuple> pTuples)
            throws IOException {
        // from the bottom of the header list
        for (int i = tree.headerList.size() - 1; i >= 0; i--) {

            Integer itemID = tree.headerList.get(i);

            // extend current itemset p by item x
            itemset = ArraysAlgos.appendIntegerToArray(itemset, itemID);

            // if there aren't subsets
            if (!isSubsumingAFoundItemset(itemset)) {

                UtilityList xUL = mapItemToUtilityList.get(itemID);

                // construct new utility list pxTuples
                UtilityList pxTuples = construct(pTuples, xUL.uLists);
                checkMemory();
                joinCount++;

                allWriteOut(itemset, pxTuples.sumIutils);

                // we create new local prefix tree
                // and call miner
                if (pxTuples.sumIutils + pxTuples.sumRutils >= minUtility) {

                    if (pxTuples.sumIutils >= minUtility) {
                        registerItemsetAndRemoveLarger(itemset, pxTuples.sumIutils, pxTuples.uLists.size());
                    } else {
                        // ===== CREATE THE LOCAL TREE =====
                        IHUPTreeMod localTree = createLocalTree(tree, itemID);
                        checkMemory();

                        // recursively call the miner procedure to
                        // explore other itemsets that are extensions of the current one
                        if (localTree.headerList.size() > 0) {
                            miner(localTree, minUtility, itemset, pxTuples.uLists);
                            checkMemory();
                        }
                    }

                } // end if
            }
            itemset = ArraysAlgos.removeLastItem(itemset);
        } // end for
    }


    private IHUPTreeMod createLocalTree(IHUPTreeMod tree, Integer itemID) {

        // It consists of the set of prefix paths
        List<List<Integer>> prefixPaths = new ArrayList<List<Integer>>();

        Node pathStart = tree.mapItemNodes.get(itemID);

        while (pathStart != null) {

            // if the path is not just the root node
            if (pathStart.parent.itemID != -1) {

                List<Integer> prefixPath = new ArrayList<Integer>();

                // add all the parents of this node to the current prefixPath
                Node parentnode = pathStart.parent;
                while (parentnode.itemID != -1) {
                    prefixPath.add(parentnode.itemID);
                    parentnode = parentnode.parent;
                }
                // add the prefixPath to the list of prefixPaths
                prefixPaths.add(prefixPath);
            }
            // We will look for the next prefixpath
            pathStart = pathStart.nodeLink;
        }

        // Create localTree
        IHUPTreeMod localTree = new IHUPTreeMod();

        // for each prefixpath ( partial transaction )
        for (List<Integer> prefixPath : prefixPaths) {
            // add partial transaction to local tree
            localTree.addLocalTransaction(prefixPath);
        }

        // create the local header table
        localTree.createHeaderList(mapItemToTWU,negativeItems);
        return localTree;
    }


    /**
     * This method constructs the utility list of pX
     *
     * @param pUL :  the list of utilityTuples of prefix P.
     * @param xUL : the list of utilityTuples of itemX
     * @return the utility list of pxUL
     */
    private UtilityList construct(List<UtilityTuple> pUL, List<UtilityTuple> xUL) {
        // create an empty utility list for pX
        UtilityList pxUL = new UtilityList();

        for (UtilityTuple ep : pUL) {
            // do a binary search to find element ex in xUL with ep.tid = ex.tid
            UtilityTuple ex = findElementWithTID(xUL, ep.getTid());
            if (ex == null) {
                continue;
            }
            UtilityTuple ePX = new UtilityTuple(ep.getTid(), ep.getIutils() + ex.getIutils(), ex.getRutils());
            // add the new UtilityTuple to the list pxUL
            pxUL.addTuple(ePX);
        }
        // return the utility list of pXY.
        return pxUL;
    }

    /**
     * Do a binary search to find the UtilityTuple with a given tid in a list of utility tuples
     * It assumes the list of tuples are ordered based on tid in ascending order
     *
     * @param ulist the list of utility tuples
     * @param tid   the tid
     * @return the UtilityTuple or null if none has the tid.
     */
    private UtilityTuple findElementWithTID(List<UtilityTuple> ulist, int tid) {
        int first = 0;
        int last = ulist.size() - 1;

        // the binary search
        while (first <= last) {
            int middle = (first + last) >>> 1; // divide by 2

            if (ulist.get(middle).getTid() < tid) {
                first = middle + 1;
            } else if (ulist.get(middle).getTid() > tid) {
                last = middle - 1;
            } else {
                return ulist.get(middle);
            }
        }
        return null;
    }

    /**
     * This method insert an itemset in the store and remove its supersets
     *
     * @param itemset the itemset
     * @param utility its utility
     * @param support its support
     */
    public void registerItemsetAndRemoveLarger(int[] itemset, long utility, int support) {

//        if (DEBUG) {
//            System.out.println(Arrays.toString(itemset));
//        }

        // create the lists for storing itemsets in the HUI-store structure if they
        // don't exist already
        if (itemset.length >= listItemsetsBySize.size()) {
            int i = listItemsetsBySize.size();
            while (i < itemset.length) {
                listItemsetsBySize.add(new ArrayList<Itemset>());
                i++;
            }
        }

        // add the itemset in the list of itemsets having the same size
        List<Itemset> listToAdd = listItemsetsBySize.get(itemset.length - 1);
        listToAdd.add(new Itemset(itemset, utility, support));

        // remove all supersets of the inserted itemset
        for (int i = itemset.length; i < listItemsetsBySize.size(); i++) {    // IMPORTANT -1
            List<Itemset> list = listItemsetsBySize.get(i);
            if (list.size() > 0) {
                Iterator<Itemset> iter = list.iterator();
                while (iter.hasNext()) {
                    Itemset itemset2 = iter.next();
                    if (ArraysAlgos.includedIn(itemset, itemset2.itemset)) {
                        iter.remove();
                    }
                }
            }
        }

    }

    /**
     * Check if there exists an itemset smaller than a given itemset in the MinHUI-Store
     *
     * @param itemset the given itemset
     * @return true if there is a smaller itemset
     */
    public boolean isSubsumingAFoundItemset(int[] itemset) {

        // we check starting from smallest items
        for (int i = 0; i < itemset.length && i < listItemsetsBySize.size(); i++) {    // IMPORTANT -1
            List<Itemset> list = listItemsetsBySize.get(i);
            if (list.size() > 0) {
                for (Itemset itemsetInList : list) {
                    if (ArraysAlgos.includedIn(itemsetInList.itemset, itemset)) {
                        return true;
                    }
                }
            }
        }
        return false;

    }

    /**
     * Write a HUI to the output file
     *
     * @param HUI
     * @param utility
     * @throws IOException
     */
    private void allWriteOut(int[] HUI, long utility) throws IOException {
        huiCount++; // increment the number of high utility itemsets found

        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < HUI.length; i++) {
            buffer.append(HUI[i]);
            buffer.append(' ');
        }
        buffer.append("#UTIL: ");
        buffer.append(utility);

        allWriter.write(buffer.toString());
        allWriter.newLine();
    }

    private void writeOutMin(Itemset itemset) throws IOException {
//        huiCount++; // increase the number of high utility itemsets found

        //Create a string buffer
        StringBuilder buffer = new StringBuilder();
        // append the prefix
        for (int i = 0; i < itemset.itemset.length; i++) {
            buffer.append(itemset.itemset[i]);
            if (i != itemset.itemset.length - 1) {
                buffer.append(' ');
            }
        }
        // append the utility value
        buffer.append(" #UTIL: ");
        buffer.append(itemset.utility);
        // write to file
        writerMin.write(buffer.toString());
        writerMin.newLine();

    }

    /**
     * Method to check the memory usage and keep the maximum memory usage.
     */
    private void checkMemory() {
        // get the current memory usage
        double currentMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d;
        // if higher than the maximum until now  replace the maximum with the current memory usage
        if (currentMemory > maxMemory) {
            maxMemory = currentMemory;
        }
    }

    /**
     * Print statistics about the latest execution to System.out.
     */
    public void printStats() {
        System.out.println("=============  MinmHUIMinerNegV1 - STATS =============");
        //System.out.println(" Total utility: " + totalUtility);
        //System.out.println(" Minimum utility: " + minUtility);
        System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
        System.out.println(" Memory ~ " + maxMemory + " MB");
        //System.out.println(" Join count: " + joinCount);
        System.out.println(" HUIs count : " + huiCount);
        System.out.println("===================================================");
    }
	public int getHUI(){
		return  listItemsetsBySize.size();
	}
	public long getTime(){
		return endTimestamp - startTimestamp;
	}
	public double getMemory(){
         return maxMemory;
	}

}

/**
 * Cambios
 * Add Negative set, to identife singe items with negative utility
 * Change comparateive sort in the IHUPTREE and local compare
 * change remaininig utility
 * add setter and getter
 * change min_utility param
 * 
 * New changes
 */
