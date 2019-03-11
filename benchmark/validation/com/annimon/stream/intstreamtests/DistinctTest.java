package com.annimon.stream.intstreamtests;


import com.annimon.stream.Functions;
import com.annimon.stream.IntStream;
import com.annimon.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;


public final class DistinctTest {
    @Test
    public void testDistinct() {
        IntStream.of(1, 2, (-1), 10, 1, 1, (-1), 5).distinct().custom(assertElements(Matchers.arrayContaining(1, 2, (-1), 10, 5)));
    }

    @Test
    public void testDistinctLazy() {
        Integer[] expected = new Integer[]{ 1, 2, 3, 5, -1 };
        List<Integer> input = new ArrayList<Integer>();
        input.addAll(Arrays.asList(1, 1, 2, 3, 5));
        IntStream stream = Stream.of(input).mapToInt(Functions.toInt()).distinct();
        input.addAll(Arrays.asList(3, 2, 1, 1, (-1)));
        List<Integer> actual = stream.boxed().toList();
        Assert.assertThat(actual, Matchers.contains(expected));
    }
}
