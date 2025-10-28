package edu.drexel.se320;

// Hamcrest
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.lessThan;

// Core JUnit 5
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

// Jqwik
import net.jqwik.api.*;
import net.jqwik.api.Tuple.Tuple2;
import net.jqwik.api.constraints.*;
import net.jqwik.api.statistics.Statistics;


public class PropertyTests extends BinarySearchBase {
    @Property
    Arbitrary<Integer[]> sortedIntArrays() {
        return Arbitraries.integers().between(0, 100)
            .array(Integer[].class)
            .ofMinSize(0).ofMaxSize(30)
            .map(arr -> {
                java.util.Arrays.sort(arr);
                return arr;
            });
    }

    @Provide
    Arbitrary<Integer[]> sortedIntArraysNonEmpty() {
        return sortedIntArrays().filter(arr -> arr.length > 0);
    }

    
    

    @Property
    public boolean checkSingleArrayElt(@ForAll int x) {
	    Integer[] arr = { x };
	    return find(arr, x) == 0;
    }


}

