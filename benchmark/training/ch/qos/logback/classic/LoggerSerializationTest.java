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
package ch.qos.logback.classic;


import ch.qos.logback.classic.net.server.HardenedLoggingEventInputStream;
import ch.qos.logback.core.net.HardenedObjectInputStream;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoggerSerializationTest {
    static final String SERIALIZATION_PREFIX = (CoreTestConstants.TEST_INPUT_PREFIX) + "/serialization/";

    // force SLF4J initialization for subsequent Logger readResolce ooperaiton
    Logger unused = LoggerFactory.getLogger(this.getClass());

    LoggerContext lc;

    Logger logger;

    ByteArrayOutputStream bos;

    ObjectOutputStream oos;

    HardenedLoggingEventInputStream hardenedLoggingEventInputStream;

    List<String> whitelist = new ArrayList<String>();

    @Test
    public void basicSerialization() throws IOException, ClassNotFoundException {
        Foo foo = new Foo(logger);
        foo.doFoo();
        Foo fooBack = writeAndRead(foo);
        fooBack.doFoo();
    }

    @Test
    public void deepTreeSerialization() throws IOException {
        // crate a tree of loggers under "aaaaaaaa"
        Logger a = lc.getLogger("aaaaaaaa");
        lc.getLogger("aaaaaaaa.a");
        lc.getLogger("aaaaaaaa.a.a");
        lc.getLogger("aaaaaaaa.a.b");
        lc.getLogger("aaaaaaaa.a.c");
        lc.getLogger("aaaaaaaa.a.d");
        lc.getLogger("aaaaaaaa.b");
        lc.getLogger("aaaaaaaa.b.a");
        lc.getLogger("aaaaaaaa.b.b");
        lc.getLogger("aaaaaaaa.b.c");
        lc.getLogger("aaaaaaaa.b.d");
        lc.getLogger("aaaaaaaa.c");
        lc.getLogger("aaaaaaaa.c.a");
        lc.getLogger("aaaaaaaa.c.b");
        lc.getLogger("aaaaaaaa.c.c");
        lc.getLogger("aaaaaaaa.c.d");
        lc.getLogger("aaaaaaaa.d");
        lc.getLogger("aaaaaaaa.d.a");
        lc.getLogger("aaaaaaaa.d.b");
        lc.getLogger("aaaaaaaa.d.c");
        lc.getLogger("aaaaaaaa.d.d");
        Logger b = lc.getLogger("b");
        writeObject(oos, a);
        oos.close();
        int sizeA = bos.size();
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        writeObject(oos, b);
        oos.close();
        int sizeB = bos.size();
        Assert.assertTrue("serialized logger should be less than 100 bytes", (sizeA < 100));
        // logger tree should not influnce serialization
        Assert.assertTrue(((("serialized loggers should be nearly the same size a:" + sizeA) + ", sizeB:") + sizeB), ((sizeA - sizeB) < 10));
    }

    @Test
    public void testCompatibilityWith_v1_0_11() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(((LoggerSerializationTest.SERIALIZATION_PREFIX) + "logger_v1.0.11.ser"));
        HardenedObjectInputStream ois = new HardenedLoggingEventInputStream(fis);// new String[] {Logger.class.getName(), LoggerRemoteView.class.getName()});

        Logger a = ((Logger) (ois.readObject()));
        ois.close();
        Assert.assertEquals("a", a.getName());
    }

    // interestingly enough, logback 1.0.11 and earlier can also read loggers serialized by 1.0.12.
    // fields not serialized are set to their default values and since the fields are not
    // used, it works out nicely
    @Test
    public void testCompatibilityWith_v1_0_12() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(((LoggerSerializationTest.SERIALIZATION_PREFIX) + "logger_v1.0.12.ser"));
        HardenedObjectInputStream ois = new HardenedObjectInputStream(fis, new String[]{ Logger.class.getName() });
        Logger a = ((Logger) (ois.readObject()));
        ois.close();
        Assert.assertEquals("a", a.getName());
    }
}
