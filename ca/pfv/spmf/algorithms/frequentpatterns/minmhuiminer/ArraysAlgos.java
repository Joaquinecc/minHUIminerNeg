package ca.pfv.spmf.algorithms.frequentpatterns.minmhuiminer;

import java.util.Comparator;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
 *
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class provides a set of basic methods that can be used with itemsets
 * represented as arrays of integers.
 * All the methods are static methods so that they can be used in any classes.
 *
 * @author Philippe Fournier-Viger
 */
public class ArraysAlgos {

    /**
     * Check if a sorted itemset is contained in another
     *
     * @param itemset1 the first itemset
     * @param itemset2 the second itemset
     * @return true if yes, otherwise false
     */
    public static boolean includedIn(int[] itemset1, int[] itemset2) {
        int count = 0; // the current position of itemset1 that we want to find in itemset2

        // for each item in itemset2
        for (int i = 0; i < itemset2.length; i++) {
            // if we found the item
            if (itemset2[i] == itemset1[count]) {
                // we will look for the next item of itemset1
                count++;
                // if we have found all items already, return true
                if (count == itemset1.length) {
                    return true;
                }
            }
        }
        // it is not included, so return false!
        return false;
    }

    /**
     * A Comparator for comparing two itemsets having the same size using the lexical order.
     */
    public static Comparator<int[]> comparatorItemsetSameSize = new Comparator<int[]>() {
        @Override
        /*
          Compare two itemsets and return -1,0 and 1 if the second itemset
          is larger, equal or smaller than the first itemset according to the lexical order.
         */
        public int compare(int[] itemset1, int[] itemset2) {
            // for each item in the first itemset
            for (int i = 0; i < itemset1.length; i++) {
                // if the current item is smaller in the first itemset
                if (itemset1[i] < itemset2[i]) {
                    return -1; // than the first itemset is smaller
                    // if the current item is larger in the first itemset
                } else if (itemset2[i] < itemset1[i]) {
                    return 1; // than the first itemset is larger
                }
                // otherwise they are equal so the next item in both itemsets will be compared next.
            }
            return 0; // both itemsets are equal
        }
    };

    /**
     * Append an integer at the end of an array of integers.
     *
     * @param array   the array
     * @param integer the integer
     * @return a new array
     */
    public static int[] appendIntegerToArray(int[] array, int integer) {
        int[] newgen = new int[array.length + 1];
        System.arraycopy(array, 0, newgen, 0, array.length);
        newgen[array.length] = integer;
        return newgen;
    }

    /**
     * Remove the item at the end of the array
     *
     * @param array itemset being evaluated
     * @return a new array without the last item added
     */
    public static int[] removeLastItem(int[] array) {
        int[] newgen = new int[array.length - 1];
        System.arraycopy(array, 0, newgen, 0, array.length - 1);
        return newgen;
    }

}
