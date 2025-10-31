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

    /* Generators */

    /*
    * Generates sorted arrays of integers between 0 and 100, length 0 to 30
    * Sorting only happens in here and not in the properties.
    */
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

    /*
    * Generates sorted arrays of integers between 0 and 100, length 1 to 30
    * Only keeps arrays with length > 0
    */
    @Provide
    Arbitrary<Integer[]> sortedIntArraysNonEmpty() {
        return sortedIntArrays().filter(arr -> arr.length > 0);
    }

    /*
    * Generates singleton arrays of integers between 0 and 100
    * Single element arrays only
    */
    @Provide
    Arbitrary<Integer[]> singletonArrays() {
        return Arbitraries.integers().between(0, 100)
            .map(v -> new Integer[]{ v });
    }


    /*
    * Generates pairs of (sorted array, element in array)
    * The element is guaranteed to be in the array
    * Uses sortedIntArraysNonEmpty to ensure the array has at least one element
    */
    @Provide
    Arbitrary<Tuple2<Integer[], Integer>> presentPairs() {
        return sortedIntArraysNonEmpty.flatMap(
            arr -> {
                int idx = Arbitraries.integers().between(0, arr.length - 1).sample();
                return Arbitraries.just(Tuple.of(arr, arr[idx]));
            }
        );
    }

    /*
    * Generates pairs of (sorted array, element below minimum)
    * The element is guaranteed to be less than the minimum element in the array
    * Uses sortedIntArraysNonEmpty to ensure the array has at least one element
    */
    @Provide
    Arbitrary<Tuple2<Integer[], Integer>> belowMinPairs() {
        return sortedIntArraysNonEmpty().map(arr -> Tuple.of(arr, arr[0] - 1));
    }

    /*
    * Generates pairs of (sorted array, element above maximum)
    * The element is guaranteed to be greater than the maximum element in the array
    * Uses sortedIntArraysNonEmpty to ensure the array has at least one element
    */
    @Provide
    Arbitrary<Tuple2<Integer[], Integer>> aboveMaxPairs() {
        return sortedIntArraysNonEmpty().map(arr -> Tuple.of(arr, arr[arr.length - 1] + 1));
    }

    /*
    * Generates unsorted arrays of integers between 0 and 100, length 1 to 30
    * Only keeps arrays that are not sorted
    */
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

    /* Property Tests */

    @Property
    @Label("Found element: index in-bounds & value matches")
    void foundIndexValid(
        @ForAll("presentPairs") Tuple2<Integer[], Integer> data) {
        Integer[] array = data.get1();
        Integer elem = data.get2();
        int index = binarySearch(array, elem);
        assertThat(index, is(lessThan(array.length)));
        assertThat(array[index], is(elem));
    }

    @Property
    @Label("Below minimum: NoSuchElementException")
    void belowMinThrows(
        @ForAll("belowMinPairs") Tuple2<Integer[], Integer> data) {
        Integer[] array = data.get1();
        Integer elem = data.get2();
        assertThrows(
            java.util.NoSuchElementException.class,
            () -> binarySearch(array, elem)
        );
    }

    @Property
    @Label("Above maximum: NoSuchElementException")
    void aboveMaxThrows(
        @ForAll("aboveMaxPairs") Tuple2<Integer[], Integer> data) {
        Integer[] array = data.get1();
        Integer elem = data.get2();
        assertThrows(
            java.util.NoSuchElementException.class,
            () -> binarySearch(array, elem)
        );
    }

    @Property
    @Label("Duplicates still return a matching index")
    void duplicatesReturnMatch(@ForAll("sortedIntegerArraysNonEmpty") Integer[] base) {
        int pick = Arbitraries.integers.between(0, base.length - 1).sample();
        Integer val = base[pick];
        List<Integer> list = new ArrayList<>(Arrays.asList(base));
        list.add(val);
        Integer[] arr = list.toArray(new Integer[0]);
        Array.sort(arr);
        int index = binarySearch(arr, val);
        assertEquals(val, arr[index]);
    }

    @Property
    @Label("Search success does not modify array")
    void arrayNotModifiedSuccess(@ForAll("presentPairs") Tuple2<Integer[], Integer> data) {
        Integer[] arr = Arrays.copyOf(data.get1(), data.get1().length);
        Integer[] snapshot = Arrays.copyOf(arr, arr.length);
        Integer elem = data.get2();
        binarySearch(arr, elem);
        assertArrayEquals(snapshot,array);
    }

    @Property
    @Label("Search failure does not modify array")
    void arrayNotModifiedFailure(@ForAll("aboveMaxPairs") Tuple2<Integer[], Integer> data) {
        Integer[] arr = Arrays.copyOf(data.get1(), data.get1().length);
        Integer[] snapshot = Arrays.copyOf(arr, arr.length);
        Integer elem = data.get2();
        assertThrows(NoSuchElementException.class, () -> binarySearch(arr, elem));
        assertArrayEquals(snapshot, arr);
    }

    @Property
    @Label("Singleton array: found at index 0")
    void singletonFound(@ForAll("singletonArrays") Integer[] single) {
        Integer e = single[0];
        int idx = binarySearch(single, e);
        assertEquals(0, idx);
        assertEquals(e, single[idx]);
    }

    @Property
    @Label("Singleton array: not-found throws")
    void singletonNotFound(@ForAll("singletonArrays") Integer[] single) {
        Integer e = single[0];
        assertThrows(NoSuchElementException.class, () -> binarySearch(single, e - 1));
        assertThrows(NoSuchElementException.class, () -> binarySearch(single, e + 1));
    }

    @Property
    @Label("Empty array: IllegalArgumentException")
    void emptyArrayIllegal(@ForAll @IntRange(min = 0, max = 100) int e) {
        Integer[] empty = new Integer[0];
        assertThrows(IllegalArgumentException.class, () -> binarySearch(empty, e));
    }

    @Example
    @Label("Null element: IllegalArgumentException")
    void nullElementIllegal() {
        Integer[] arr = {1, 2, 3};
        assertThrows(IllegalArgumentException.class, () -> binarySearch(arr, null));
    }

    @Example
    @Label("Null array: IllegalArgumentException")
    void nullArrayIllegal() {
        assertThrows(IllegalArgumentException.class, () -> binarySearch(null, 1));
    }

    @Property
    @Label("Unsorted arrays: either throws or returns matching index (underspecified)")
    void unsortedArraysEitherThrowOrCorrect(
            @ForAll("unsortedIntegerArrays") Integer[] unsorted,
            @ForAll @IntRange(min = -10, max = 110) int elem
    ) {
        try {
            int idx = binarySearch(unsorted, elem);
            assertEquals(elem, unsorted[idx]);
        } catch (NoSuchElementException ok) {
        }
    }
}