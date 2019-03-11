package com.alibaba.json.bvt;


import SerializerFeature.UseSingleQuotes;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import junit.framework.TestCase;
import org.junit.Assert;


public class JSONTest_Bytes_1 extends TestCase {
    public void test_bytes() throws Exception {
        Assert.assertEquals("\"abc\"", new String(JSON.toJSONBytes("abc", SerializeConfig.getGlobalInstance())));
        Assert.assertEquals("'abc'", new String(JSON.toJSONBytes("abc", SerializeConfig.getGlobalInstance(), UseSingleQuotes)));
    }

    public void test_bytes_2() throws Exception {
        Assert.assertEquals("\"abc\"", new String(JSON.toJSONBytes("abc")));
        Assert.assertEquals("'abc'", new String(JSON.toJSONBytes("abc", UseSingleQuotes)));
    }
}
