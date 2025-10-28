package edu.drexel.se320;
import java.util.NoSuchElementException;
public class BinarySearch {

    // DO NOT MODIFY THIS SIGNATURE
    // This includes the protected modifier; the autograder currently relies
    // on a combination of overloading and visibility hacks to swap out your
    // code at runtime to test your test suite.
    protected static <T extends Comparable<T>> int binarySearchImplementation(T[] array, T elem) {
        
        // Input validation
        if (array == null) throw new IllegalArgumentException("array");
        if (elem == null) throw new IllegalArgumentException("elem");
        if (array.length == 0) throw new IllegalArgumentException("Input array is empty");

        // Binary search implementation
        int left = 0;
        int right = array.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            int compare =  elem.compareTo(array[mid]);

            if  (compare == 0) {
                return mid; // Element was found
            }
            else if (compare < 0) {
                right = mid - 1; // Search the left half
            }
            else {
                left = mid + 1; // search the right half
            }
        }
        throw new NoSuchElementException("Element was not in array: " + elem);
    }
}