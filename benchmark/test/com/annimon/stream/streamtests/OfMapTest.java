package com.annimon.stream.streamtests;


import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import static com.annimon.stream.Functions.entryKey;
import static com.annimon.stream.Functions.entryValue;


public final class OfMapTest {
    @Test
    public void testStreamOfMap() {
        final Map<String, Integer> map = new HashMap<String, Integer>(4);
        map.put("This", 1);
        map.put(" is ", 2);
        map.put("a", 3);
        map.put(" test", 4);
        String result = Stream.of(map).sortBy(<String, Integer>entryValue()).map(<String, Integer>entryKey()).collect(Collectors.joining());
        Assert.assertThat(result, Matchers.is("This is a test"));
    }

    @Test(expected = NullPointerException.class)
    public void testStreamOfMapNull() {
        Stream.of(((Map<?, ?>) (null)));
    }
}
