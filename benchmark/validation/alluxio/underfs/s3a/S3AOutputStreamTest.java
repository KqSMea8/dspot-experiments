/**
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */
package alluxio.underfs.s3a;


import alluxio.conf.AlluxioConfiguration;
import alluxio.util.ConfigurationUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


/**
 * Unit tests for the {@link S3AOutputStream}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(S3AOutputStream.class)
public class S3AOutputStreamTest {
    private static final String BUCKET_NAME = "testBucket";

    private static final String KEY = "testKey";

    private static AlluxioConfiguration sConf = new alluxio.conf.InstancedConfiguration(ConfigurationUtils.defaults());

    private File mFile;

    private BufferedOutputStream mLocalOutputStream;

    private S3AOutputStream mStream;

    /**
     * Tests to ensure {@link S3AOutputStream#write(int)} calls the underlying output stream.
     */
    @Test
    public void writeByte() throws Exception {
        mStream.write(1);
        mStream.close();
        Mockito.verify(mLocalOutputStream).write(1);
    }

    /**
     * Tests to ensure {@link S3AOutputStream#write(byte[])} calls the underlying output stream.
     */
    @Test
    public void writeByteArray() throws Exception {
        byte[] b = new byte[10];
        mStream.write(b);
        mStream.close();
        Mockito.verify(mLocalOutputStream).write(b, 0, b.length);
    }

    /**
     * Tests to ensure {@link S3AOutputStream#write(byte[], int, int)} calls the underlying
     * output stream.
     */
    @Test
    public void writeByteArrayWithRange() throws Exception {
        byte[] b = new byte[10];
        mStream.write(b, 0, b.length);
        mStream.close();
        Mockito.verify(mLocalOutputStream).write(b, 0, b.length);
    }

    /**
     * Tests to ensure {@link File#delete()} is called when the stream is closed.
     */
    @Test
    public void close() throws Exception {
        mStream.close();
        Mockito.verify(mFile).delete();
    }

    /**
     * Tests to ensure {@link S3AOutputStream#flush()} calls the underlying output stream.
     */
    @Test
    public void flush() throws Exception {
        mStream.flush();
        mStream.close();
        Mockito.verify(mLocalOutputStream).flush();
    }
}
