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
    @Provide
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

    @Provide
    Arbitrary<Integer[]> singletonArrays() {
        return Arbitraries.integers().between(0, 100)
            .map(v -> new Integer[]{ v });
    }

    @Provide
    Arbitrary<Tuple2<Integer[], Integer>> presentPairs() {
        return sortedIntArraysNonEmpty.flatMap(
            arr -> {
                int idx = Arbitraries.integers().between(0, arr.length - 1).sample();
                return Arbitraries.just(Tuple.of(arr, arr[idx]));
            }
        );
    }

    @Provide
    Arbitrary<Tuple2<Integer[], Integer>> belowMinPairs() {
        return sortedIntArraysNonEmpty().map(arr -> Tuple.of(arr, arr[0] - 1));
    }

    @Provide
    Arbitrary<Tuple2<Integer[], Integer>> aboveMaxPairs() {
        return sortedIntArraysNonEmpty.map(arr -> Tuple.of(arr, arr[arr.length - 1] + 1));
    }

    @Provide
    Arbitrary<Integer[]> unsortedIntegerArrays() {
        return Arbitraries.integers().between(0, 100)
        .array(Integer[].class)
        .ofMinSize(1).ofMaxSize(30)
        .filter(arr -> {
            Integer[] copy = java.util.Arrays.copyOf(arr, arr.length);
            java.util.Arrays.sort(copy);
            return !java.util.Arrays.equals(arr, copy);
        });
    }
    @Property
    void foundIndexValid(
        @ForAll("presentPairs") Tuple2<Integer[], Integer> data) {
        Integer[] array = data.get1();
        Integer elem = data.get2();
        int index = binarySearch(array, elem);
        assertThat(index, is(lessThan(array.length)));
        assertThat(array[index], is(elem));
    }

    @Property
    void belowMinThrows(
        @ForAll("belowMinPairs") Tuple2<Integer[], Integer> data) {
        Integer[] array = data.get1();
        Integer elem = data.get2();
        assertThrows(
            java.util.NoSuchElementException.class,
            () -> binarySearch(array, elem)
        );
    }
