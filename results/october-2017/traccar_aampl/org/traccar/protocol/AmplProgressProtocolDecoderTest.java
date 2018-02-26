package org.traccar.protocol;


public class AmplProgressProtocolDecoderTest extends org.traccar.ProtocolTest {
    @org.junit.Test(timeout = 10000)
    public void testDecode() throws java.lang.Exception {
        org.traccar.protocol.ProgressProtocolDecoder decoder = new org.traccar.protocol.ProgressProtocolDecoder(new org.traccar.protocol.ProgressProtocol());
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("progress", ((org.traccar.protocol.ProgressProtocolDecoder)decoder).getProtocolName());
        verifyNothing(decoder, binary(java.nio.ByteOrder.LITTLE_ENDIAN, "020037000100000003003131310f003335343836383035313339303036320f00323530303136333832383531353535010000000100000000000000e6bb97b6"));
        /* verifyPosition(decoder, binary(ByteOrder.LITTLE_ENDIAN,
        "0a009f00700d000076b1345580feaf2720b7e71a0301000000327f39031f15d2b900ffffffffffffffff00ac2600000900040000000000000000000000000000e52a6810c20000000000001c49010000000000000000000000000d00000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000019000500eefff1ff0000000000000000000000000000000016151c91"));
        
        verifyPosition(decoder, binary(ByteOrder.LITTLE_ENDIAN,
        "0a009f00720d00008ab1345580feaf2720b7e71a0301000000327f39031f15d2b900ffffffffffffffff00ac2600000800050000000000000000000000000000e12a6810c10000000000001c49010000000000000000000000000d00000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000019000500eefff1ff00000000000000000000000000000000052a49f2"));
         */
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("progress", ((org.traccar.protocol.ProgressProtocolDecoder)decoder).getProtocolName());
    }
}
