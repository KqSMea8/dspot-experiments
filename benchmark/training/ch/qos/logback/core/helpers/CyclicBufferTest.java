/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.helpers;


import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


public class CyclicBufferTest {
    @Test
    public void smoke() {
        CyclicBuffer<String> cb = new CyclicBuffer<String>(2);
        assertSize(cb, 0);
        cb.add("zero");
        assertSize(cb, 1);
        cb.add("one");
        assertSize(cb, 2);
        cb.add("two");
        assertSize(cb, 2);
        Assert.assertEquals("one", cb.get());
        assertSize(cb, 1);
        Assert.assertEquals("two", cb.get());
        assertSize(cb, 0);
    }

    @Test
    public void cloning() {
        CyclicBuffer<String> cb = new CyclicBuffer<String>(2);
        cb.add("zero");
        cb.add("one");
        CyclicBuffer<String> clone = new CyclicBuffer<String>(cb);
        assertSize(clone, 2);
        cb.clear();
        assertSize(cb, 0);
        List<String> witness = Arrays.asList("zero", "one");
        Assert.assertEquals(witness, clone.asList());
    }
}
