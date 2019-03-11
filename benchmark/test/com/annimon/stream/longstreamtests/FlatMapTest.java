package com.annimon.stream.longstreamtests;


import com.annimon.stream.LongStream;
import com.annimon.stream.function.LongFunction;
import java.util.NoSuchElementException;
import org.hamcrest.Matchers;
import org.junit.Test;


public final class FlatMapTest {
    @Test
    public void testFlatMap() {
        LongFunction<LongStream> twicer = new LongFunction<LongStream>() {
            @Override
            public LongStream apply(long value) {
                return LongStream.of(value, value);
            }
        };
        LongStream.of(10L, 20L, 30L).flatMap(twicer).custom(assertElements(Matchers.arrayContaining(10L, 10L, 20L, 20L, 30L, 30L)));
        LongStream.of(10L, 20L, (-30L)).flatMap(new LongFunction<LongStream>() {
            @Override
            public LongStream apply(long value) {
                if (value < 0)
                    return LongStream.of(value);

                return null;
            }
        }).custom(assertElements(Matchers.arrayContaining((-30L))));
        LongStream.of(10L, 20L, (-30L)).flatMap(new LongFunction<LongStream>() {
            @Override
            public LongStream apply(long value) {
                if (value < 0)
                    return LongStream.empty();

                return LongStream.of(value);
            }
        }).custom(assertElements(Matchers.arrayContaining(10L, 20L)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testFlatMapIterator() {
        LongStream.empty().flatMap(new LongFunction<LongStream>() {
            @Override
            public LongStream apply(long value) {
                return LongStream.of(value);
            }
        }).iterator().nextLong();
    }
}
